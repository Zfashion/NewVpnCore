//    OpenVPN -- An application to securely tunnel IP networks
//               over a single port, with support for SSL/TLS-based
//               session authentication and key exchange,
//               packet encryption, packet authentication, and
//               packet compression.
//
//    Copyright (C) 2012-2021 OpenVPN Inc.
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU Affero General Public License Version 3
//    as published by the Free Software Foundation.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU Affero General Public License for more details.
//
//    You should have received a copy of the GNU Affero General Public License
//    along with this program in the COPYING file.

#include "test_common.h"

#include <openvpn/client/remotelist.hpp>

using namespace openvpn;

struct PreResolveNotifyIgn : RemoteList::PreResolve::NotifyCallback {
  void pre_resolve_done() override {}
};
struct PreResolveNotifyLog : RemoteList::PreResolve::NotifyCallback {
  explicit PreResolveNotifyLog(const std::string& msg = "")
    : msg_(msg) {}
  void pre_resolve_done() override {
    OPENVPN_LOG(msg_);
  }
  const std::string msg_;
};



TEST(RemoteList, CtorRemoteOverride)
{
  struct TestOverride : public RemoteList::RemoteOverride {
    TestOverride(const std::string& server_host,
		 const std::string& server_port,
		 const Protocol& transport_protocol)
    {
      item_.reset(new RemoteList::Item);
      item_->server_host = server_host;
      item_->server_port = server_port;
      item_->transport_protocol = transport_protocol;
    }
    RemoteList::Item::Ptr get() override {
      return item_;
    }
    RemoteList::Item::Ptr item_;
  };

  TestOverride test_ovr("1.1.1.1", "1111", Protocol(Protocol::UDPv4));
  RemoteList rl(&test_ovr);

  ASSERT_EQ(rl.defined(), true);
  ASSERT_EQ(rl.size(), 1);
  ASSERT_EQ(rl.get_item(0).server_host, "1.1.1.1");
  ASSERT_EQ(rl.get_item(0).server_port, "1111");
  ASSERT_EQ(rl.get_item(0).transport_protocol, Protocol(Protocol::UDPv4));
}



TEST(RemoteList, CtorSingleHost)
{
  RemoteList rl("1.1.1.1", "1111", Protocol(Protocol::TCPv6), "");
  ASSERT_EQ(rl.defined(), true);
  ASSERT_EQ(rl.size(), 1);
  ASSERT_EQ(rl.get_item(0).server_host, "1.1.1.1");
  ASSERT_EQ(rl.get_item(0).server_port, "1111");
  ASSERT_EQ(rl.get_item(0).transport_protocol, Protocol(Protocol::TCPv6));
}
TEST(RemoteList, CtorSingleHostBadPort)
{
  JY_EXPECT_THROW(
    RemoteList("1.1.1.1", "99999", Protocol(Protocol::TCPv6), "CtorSingleHostBadPort"),
    HostPort::host_port_error, "CtorSingleHostBadPort"
  );
}


TEST(RemoteList, CtorRemoteList)
{
  OptionList cfg;
  cfg.parse_from_config(
    "proto tcp6\n"
    "remote 0.default.invalid\n"
    "port 9999\n"
    "remote 1.domain.invalid 1111 udp\n"
    "<connection>\n"
    "  remote 2.domain.invalid\n"
    "  port 8888\n"
    "</connection>\n"
    "<connection>\n"
    "  proto udp6\n"
    "  remote 3:f00d:4::1\n"
    "</connection>\n"
    , nullptr);
  cfg.update_map();

  RemoteList rl(cfg, "", 0, nullptr);
  ASSERT_EQ(rl.defined(), true);
  ASSERT_EQ(rl.size(), 4);
  ASSERT_EQ(rl.get_item(0).server_host, "0.default.invalid");
  ASSERT_EQ(rl.get_item(0).server_port, "9999");
  ASSERT_EQ(rl.get_item(0).transport_protocol, Protocol(Protocol::TCPv6));
  ASSERT_EQ(rl.get_item(1).server_host, "1.domain.invalid");
  ASSERT_EQ(rl.get_item(1).server_port, "1111");
  ASSERT_EQ(rl.get_item(1).transport_protocol, Protocol(Protocol::UDP));
  ASSERT_EQ(rl.get_item(2).server_host, "2.domain.invalid");
  ASSERT_EQ(rl.get_item(2).server_port, "8888");
  ASSERT_EQ(rl.get_item(2).transport_protocol, Protocol(Protocol::TCPv6));
  ASSERT_EQ(rl.get_item(3).server_host, "3:f00d:4::1");
  ASSERT_EQ(rl.get_item(3).server_port, "9999");
  ASSERT_EQ(rl.get_item(3).transport_protocol, Protocol(Protocol::UDPv6));
}
TEST(RemoteList, CtorRemoteListConnBlockOnly)
{
  OptionList cfg;
  cfg.parse_from_config(
    "remote 1.noblock.invalid 1111 udp\n"
    "<connection>\n"
    "  remote 2.block.invalid\n"
    "</connection>\n"
    , nullptr);
  cfg.update_map();

  RemoteList rl(cfg, "", RemoteList::CONN_BLOCK_ONLY, nullptr);
  ASSERT_EQ(rl.defined(), true);
  ASSERT_EQ(rl.size(), 1);
  ASSERT_EQ(rl.get_item(0).server_host, "2.block.invalid");
}
TEST(RemoteList, CtorRemoteListEmpty)
{
  OptionList cfg;
  cfg.parse_from_config("", nullptr);
  cfg.update_map();

  ASSERT_THROW(RemoteList(cfg, "", 0, nullptr), option_error);
  RemoteList rl(cfg, "", RemoteList::ALLOW_EMPTY, nullptr);
}
TEST(RemoteList, CtorRemoteListConnBlockFactory)
{
  struct TestConnBlock : public RemoteList::ConnBlock
  {
    void new_item(const RemoteList::Item& item) override
      {
	OPENVPN_LOG("TestConnBlock: " << item.to_string());
      }
  };
  struct TestConnBlockFactory : public RemoteList::ConnBlockFactory
  {
    RemoteList::ConnBlock::Ptr new_conn_block(const OptionList::Ptr& opt) override
      {
	if (opt->exists("block-option"))
	  return RemoteList::ConnBlock::Ptr(new TestConnBlock());
	return RemoteList::ConnBlock::Ptr();
      }
  };

  OptionList cfg;
  cfg.parse_from_config(
    "<block>\n"
    "  remote block.invalid\n"
    "  block-option\n"
    "</block>\n"
    "<block>\n"
    "  remote block.invalid\n"
    "  unknown-block-option\n"
    "</block>\n"
    , nullptr);
  cfg.update_map();
  TestConnBlockFactory tcbf;

  testLog->startCollecting();
  RemoteList rl1(cfg, "block", 0, &tcbf);
  std::string output1(testLog->stopCollecting());
  ASSERT_NE(output1.find("TestConnBlock"), std::string::npos);
  ASSERT_EQ(rl1.size(), 2);

  testLog->startCollecting();
  RemoteList rl2(cfg, "block", RemoteList::CONN_BLOCK_OMIT_UNDEF, &tcbf);
  std::string output2(testLog->stopCollecting());
  ASSERT_NE(output2.find("TestConnBlock"), std::string::npos);
  ASSERT_EQ(rl2.size(), 1);
}
TEST(RemoteList, CtorRemoteListWarnUnsupported)
{
  OptionList cfg;
  cfg.parse_from_config(
    "<connection>\n"
    "  remote block.invalid\n"
    "  http-proxy\n"
    "  http-proxy-option\n"
    "  http-proxy-user-pass\n"
    "</connection>\n"
    , nullptr);
  cfg.update_map();

  testLog->startCollecting();
  RemoteList rl(cfg, "", RemoteList::WARN_UNSUPPORTED, nullptr);
  std::string output(testLog->stopCollecting());

  ASSERT_NE(output.find(" http-proxy "), std::string::npos);
  ASSERT_NE(output.find(" http-proxy-option "), std::string::npos);
  ASSERT_NE(output.find(" http-proxy-user-pass "), std::string::npos);
}
TEST(RemoteList, CtorRemoteListBlockLimit)
{
  OptionList cfg;
  cfg.parse_from_config(
    "<connection>\n"
    "  remote block.invalid\n"
    "  directive-with-a-way-too-long-name-to-be-accepted-by-the-block-parser\n"
    "</connection>\n"
    , nullptr);
  cfg.update_map();

  JY_EXPECT_THROW(RemoteList(cfg, "", 0, nullptr), option_error, "connection_block");
}


TEST(RemoteList, RemoteListPreResolve)
{
  OptionList cfg;
  cfg.parse_from_config(
    "remote 1.1.1.1 1111 udp\n"
    "remote 2:cafe::1 2222 tcp\n"
    "remote 3.domain.tld 3333 udp4\n"
    "remote 3.domain.tld 33333 udp\n"
    "remote 4.domain.tld 4444 udp6\n"
    "remote 5.noresolve.tld 5555 udp4\n"
    , nullptr);
  cfg.update_map();

  RemoteList::Ptr rl(new RemoteList(cfg, "", 0, nullptr));
  rl->set_enable_cache(true);

  RandomAPI::Ptr rng(new MTRand(3735928559));
  rl->set_random(rng);

  openvpn_io::io_context ioctx;
  SessionStats::Ptr stats(new SessionStats());
  FakeAsyncResolvable<
      RemoteList::PreResolve,
      openvpn_io::io_context&,
      const RemoteList::Ptr&,
      const SessionStats::Ptr&>
    fake_preres(ioctx, rl, stats);

  fake_preres.set_results("1.1.1.1", "1111", { {"1.1.1.1", 1111} });
  fake_preres.set_results("2:cafe::1", "2222", { {"2:cafe::1", 2222} });
  fake_preres.set_results("3.domain.tld", "3333", { {"3.3.3.3", 3333}, {"3::3", 3333} });
  fake_preres.set_results("4.domain.tld", "4444", { {"4.4.4.4", 4444}, {"4::4", 4444} });

  PreResolveNotifyLog logmsg("<<<RemoteListPreResolve>>>");
  testLog->startCollecting();
  fake_preres.start(&logmsg);
  std::string output(testLog->stopCollecting());
  ASSERT_NE(output.find("<<<RemoteListPreResolve>>>"), std::string::npos);

  ASSERT_EQ(5, rl->size())
    << "Unexpected remote list item count" << std::endl
    << output;

  ASSERT_EQ(rl->get_item(0).res_addr_list_defined(), true);
  ASSERT_EQ(rl->get_item(0).res_addr_list->size(), 1);
  ASSERT_EQ(rl->get_item(0).res_addr_list->at(0)->to_string(), "1.1.1.1");
  ASSERT_EQ(rl->get_item(1).res_addr_list_defined(), true);
  ASSERT_EQ(rl->get_item(1).res_addr_list->size(), 1);
  ASSERT_EQ(rl->get_item(1).res_addr_list->at(0)->to_string(), "2:cafe::1");
  ASSERT_EQ(rl->get_item(2).res_addr_list_defined(), true);
  ASSERT_EQ(rl->get_item(2).res_addr_list->size(), 1);
  ASSERT_EQ(rl->get_item(2).res_addr_list->at(0)->to_string(), "3.3.3.3");
  ASSERT_EQ(rl->get_item(3).res_addr_list_defined(), true);
  ASSERT_EQ(rl->get_item(3).res_addr_list->size(), 2);
  ASSERT_EQ(rl->get_item(3).res_addr_list->at(0)->to_string(), "3.3.3.3");
  ASSERT_EQ(rl->get_item(3).res_addr_list->at(1)->to_string(), "3::3");
  ASSERT_EQ(rl->get_item(3).actual_host(), rl->get_item(2).actual_host());
  ASSERT_EQ(rl->get_item(4).res_addr_list_defined(), true);
  ASSERT_EQ(rl->get_item(4).res_addr_list->size(), 1);
  ASSERT_EQ(rl->get_item(4).res_addr_list->at(0)->to_string(), "4::4");

  // in case it gets randomized before the other 3.domain.tld
  fake_preres.set_results("3.domain.tld", "33333", { {"3.3.3.3", 33333}, {"3::3", 33333} });
  rl->reset_cache();
  rl->randomize();

  PreResolveNotifyIgn ignore;
  testLog->startCollecting();
  fake_preres.start(&ignore);
  output = testLog->stopCollecting();

  ASSERT_EQ(5, rl->size())
    << "Unexpected remote list item count" << std::endl
    << output;

  for (size_t i=0; i < rl->size(); ++i)
    {
      ASSERT_EQ(rl->get_item(i).res_addr_list_defined(), true);
      if (rl->get_item(i).server_host[0] == '1')
	{
	  ASSERT_EQ(rl->get_item(i).res_addr_list->size(), 1);
	  ASSERT_EQ(rl->get_item(i).res_addr_list->at(0)->to_string(), "1.1.1.1");
	}
      else if (rl->get_item(i).server_host[0] == '2')
	{
	  ASSERT_EQ(rl->get_item(i).res_addr_list->size(), 1);
	  ASSERT_EQ(rl->get_item(i).res_addr_list->at(0)->to_string(), "2:cafe::1");
	}
      else if (rl->get_item(i).server_host[0] == '3')
	{
	  if (rl->get_item(i).transport_protocol.is_ipv4())
	    {
	      ASSERT_EQ(rl->get_item(i).res_addr_list->size(), 1);
	      ASSERT_EQ(rl->get_item(i).res_addr_list->at(0)->to_string(), "3.3.3.3");
	    }
	  else
	    {
	      ASSERT_EQ(rl->get_item(i).res_addr_list->size(), 2);
	    }
	}
      else if (rl->get_item(i).server_host[0] == '4')
	{
	  ASSERT_EQ(rl->get_item(i).res_addr_list->size(), 1);
	  ASSERT_EQ(rl->get_item(i).res_addr_list->at(0)->to_string(), "4::4");
	}
    }

  for (size_t i=0; i < rl->size(); ++i)
    {
      for (size_t j=0; j < rl->get_item(i).res_addr_list->size(); ++j)
	{
	  std::string host;
	  std::string port;
	  Protocol proto;
	  ASSERT_EQ(rl->endpoint_available(&host, &port, &proto), true);
	  ASSERT_EQ(rl->get_item(i).actual_host(), host);
	  ASSERT_EQ(rl->get_item(i).server_port, port);
	  if (rl->current_transport_protocol().is_ipv4()
	  ||  rl->current_transport_protocol().is_ipv6()) {
	    ASSERT_EQ(rl->current_transport_protocol(), proto);
	  }

	  auto ep1 = fake_preres.init_endpoint();
	  auto ep2 = fake_preres.init_endpoint();
	  rl->get_endpoint(ep1);
	  rl->get_item(i).get_endpoint(ep2, j);
	  ASSERT_EQ(ep1, ep2);

	  rl->next();
	}
    }
}



TEST(RemoteList, RemoteRandomHostname)
{
  OptionList cfg;
  cfg.parse_from_config(
    "remote-random-hostname\n"
    "remote 1.1.1.1\n"
    "remote 2.domain.invalid\n"
    "<connection>\n"
    "  remote 3.domain.invalid\n"
    "</connection>\n"
    "<connection>\n"
    "  remote 4:cafe::1\n"
    "</connection>\n"
    , nullptr);
  cfg.update_map();

  RandomAPI::Ptr rng(new FakeSecureRand(0xf7));
  RemoteList rl(cfg, "", 0, nullptr, rng);

  ASSERT_EQ(rl.size(), 4);
  ASSERT_EQ(rl.get_item(0).actual_host(), "1.1.1.1");
  ASSERT_EQ(rl.get_item(1).actual_host(), "f7f8f9fafbfc.2.domain.invalid");
  ASSERT_EQ(rl.get_item(2).actual_host(), "fdfeff000102.3.domain.invalid");
  ASSERT_EQ(rl.get_item(3).actual_host(), "4:cafe::1");
  rl.next();
  ASSERT_EQ(rl.current_server_host(), "030405060708.2.domain.invalid");
  rl.next();
  ASSERT_EQ(rl.current_server_host(), "090a0b0c0d0e.3.domain.invalid");

  ASSERT_EQ(rl.get_enable_cache(), false);
  rl.set_enable_cache(true);
  ASSERT_EQ(rl.get_enable_cache(), true);
  rl.next();
  rl.next();

  rl.next();
  ASSERT_EQ(rl.current_server_host(), "030405060708.2.domain.invalid");
  rl.next();
  ASSERT_EQ(rl.current_server_host(), "090a0b0c0d0e.3.domain.invalid");
}
TEST(RemoteList, RemoteRandomHostnameNoPRNG)
{
  OptionList cfg;
  cfg.parse_from_config(
    "remote-random-hostname\n"
    "remote domain.invalid\n"
    , nullptr);
  cfg.update_map();

  ASSERT_THROW(RemoteList(cfg, "", 0, nullptr), RemoteList::remote_list_error);
}


TEST(RemoteList, OverrideFunctions)
{
  OptionList cfg;
  cfg.parse_from_config(
    "remote-random-hostname\n"
    "remote config.host.invalid 1111 udp6\n"
    "remote config.host.invalid 1111 tcp\n"
    "remote config.host.invalid 1111 tls4\n"
    , nullptr);
  cfg.update_map();

  RandomAPI::Ptr rng(new FakeSecureRand(0xf7));
  RemoteList rl(cfg, "", 0, nullptr, rng);
  ASSERT_EQ(rl.size(), 3);

  rl.set_proto_version_override(IP::Addr::Version::V6);
  for (size_t i=0; i < rl.size(); ++i)
    ASSERT_TRUE(rl.get_item(i).transport_protocol.is_ipv6());

  rl.set_proto_version_override(IP::Addr::Version::V4);
  for (size_t i=0; i < rl.size(); ++i)
    ASSERT_TRUE(rl.get_item(i).transport_protocol.is_ipv4());

  rl.handle_proto_override(Protocol(Protocol::UDPv4), true);
  ASSERT_EQ(rl.size(), 1);
  ASSERT_EQ(rl.current_transport_protocol(), Protocol(Protocol::TCPv4));

  rl.set_port_override("4711");
  ASSERT_EQ(rl.size(), 1);
  ASSERT_EQ(rl.get_item(0).server_port, "4711");

  rl.set_server_override("override.host.invalid");
  ASSERT_EQ(rl.size(), 1);
  ASSERT_EQ(rl.current_server_host(), "override.host.invalid");
}


