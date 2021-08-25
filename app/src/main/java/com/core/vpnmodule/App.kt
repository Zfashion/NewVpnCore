package com.core.vpnmodule

import android.app.Application
import com.core.ikev2.provide.Ikev2CertHelper
import com.core.openvpn.provide.OpenCertHelper
import com.core.unitevpn.utils.VPNLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {

    init {
        VPNLog.d("App init")


    }

    override fun onCreate() {
        super.onCreate()

        VPNLog.d("App onCreate")

        CoroutineScope(Dispatchers.Default).launch {
            Ikev2CertHelper.storeCertificate(
                "-----BEGIN CERTIFICATE-----\n" +
                        "MIIDMjCCAhqgAwIBAgIIKpFZgS9OCzIwDQYJKoZIhvcNAQELBQAwNzEMMAoGA1UE\n" +
                        "BhMDQ09NMREwDwYDVQQKEwhTdXBlclZwbjEUMBIGA1UEAxMLU1VQRVJWUE4gQ0Ew\n" +
                        "HhcNMjAwNzEwMDM0NzUxWhcNMzAwNzA4MDM0NzUxWjA3MQwwCgYDVQQGEwNDT00x\n" +
                        "ETAPBgNVBAoTCFN1cGVyVnBuMRQwEgYDVQQDEwtTVVBFUlZQTiBDQTCCASIwDQYJ\n" +
                        "KoZIhvcNAQEBBQADggEPADCCAQoCggEBAKtF+gpL3dynt3ruKzV9zqtSPptHLY19\n" +
                        "qd+tdjeMm916rUNY+vNeA84I7LqAPJ9EBePo1jsw43IAkL3U26dnG3iza4BxwgE6\n" +
                        "zuGtWMnmFsVfSgveGR09kj6A2WEExl9yY6Q9I19P+FM1BJHL19ui1WlqU4Trhxo8\n" +
                        "l227KZymY0EIgTOsDkZUaPhGn952D4I/wz6yLiwarWQR6hfk+ypZU0YsOdBBG0Yb\n" +
                        "sG2Nafwz1ARAPCMv1WSL1uw6+1qIL93nq2LPROhCKjEdqBahw948bB7gRB9ywTAW\n" +
                        "VRdcQw2NO0Ra/oUmR1bNCSkrdO58jKNqDf0dcWnWCV4XSjl3+WcAh4kCAwEAAaNC\n" +
                        "MEAwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMCAQYwHQYDVR0OBBYEFARp\n" +
                        "CoTq6NsihQ2oTiyqQ9dC74XnMA0GCSqGSIb3DQEBCwUAA4IBAQCIJ9xx/RutgXfH\n" +
                        "SEWr3G57VDmthn9iD/GcLemO8XKWz2+IceDjY6YVXHC3BnZhHLT2oKmYuQw7exkV\n" +
                        "hbvA8oQKEGmpq+JO+htxqWQitWkJK11DvVJ0HvnCXEoWCiHP7OvFABlaAmPU/SAs\n" +
                        "oUJTO3/iqDUQL0CnJNkav8A1rfq9DBLOUOxMElOqFlzvqR+sQsGjG3PqquBjsl2q\n" +
                        "FWen1wJ5sjMjkF2AnO+oNPvqnO/DwEHStUYFE9JdSJdnrItjmzON5kqRP4ePvGFy\n" +
                        "0sc/fOxahpv5Rsj7LVXrjDhaz38erqcA2lHakdgyeOJc+NysTQOBpGnmo745ZaX6\n" +
                        "83T/6qyQ\n" +
                        "-----END CERTIFICATE-----"
            )

            OpenCertHelper.setupCert(
                """
                    client
                    <connection>
                    remote 104.149.193.146 443 tcp
                    connect-timeout  20
                    connect-retry 0
                    </connection>
                    
                    <connection>
                    remote 104.149.198.102 102 tcp
                    connect-timeout  20
                    connect-retry 0
                    </connection>
                    
                    connect-retry 1
                    connect-retry-max 1
                    resolv-retry 60
                    
                    proto udp
                    explicit-exit-notify
                    dev tun
                    resolv-retry infinite
                    nobind
                    persist-key
                    persist-tun
                    remote-cert-tls server
                    verify-x509-name server_R2Puof1cldz4yD3X name
                    auth SHA256
                    auth-nocache
                    cipher AES-128-GCM
                    tls-client
                    tls-version-min 1.2
                    tls-cipher TLS-ECDHE-ECDSA-WITH-AES-128-GCM-SHA256
                    ignore-unknown-option block-outside-dns
                    setenv opt block-outside-dns # Prevent Windows 10 DNS leak
                    verb 3
                    <ca>
                    -----BEGIN CERTIFICATE-----
                    MIIBwjCCAWegAwIBAgIJAP0ltPtENv28MAoGCCqGSM49BAMCMB4xHDAaBgNVBAMM
                    E2NuX0hDRlVBSGl2MXYwa21maFgwHhcNMjAwNzA3MDkzNjE2WhcNMzAwNzA1MDkz
                    NjE2WjAeMRwwGgYDVQQDDBNjbl9IQ0ZVQUhpdjF2MGttZmhYMFkwEwYHKoZIzj0C
                    AQYIKoZIzj0DAQcDQgAEx9SqNezAmIfvXVw13znifyUKAOZuBZnhRpbDPm+GV45m
                    KYe/dnCht7V9golgDcei/Ta1jUbSuBjkLS2maYbkOKOBjTCBijAdBgNVHQ4EFgQU
                    f3GIVaFt7BfXzOCXKCPLAoyDWqAwTgYDVR0jBEcwRYAUf3GIVaFt7BfXzOCXKCPL
                    AoyDWqChIqQgMB4xHDAaBgNVBAMME2NuX0hDRlVBSGl2MXYwa21maFiCCQD9JbT7
                    RDb9vDAMBgNVHRMEBTADAQH/MAsGA1UdDwQEAwIBBjAKBggqhkjOPQQDAgNJADBG
                    AiEAgcWuULZi1q1gDMuqsKwYMM34/vobsxMNPBnE+PV4DhgCIQDqeH1Hmf8lwAkG
                    jfJnPtfQb1R74P5x5acKkVlDKYTLFw==
                    -----END CERTIFICATE-----
                    </ca>
                    <cert>
                    -----BEGIN CERTIFICATE-----
                    MIIBzTCCAXOgAwIBAgIRAKygpyJCMm6+W/W+E4ScHwYwCgYIKoZIzj0EAwIwHjEc
                    MBoGA1UEAwwTY25fSENGVUFIaXYxdjBrbWZoWDAeFw0yMDA3MDcwOTM2MjRaFw0y
                    MjEwMTAwOTM2MjRaMBAxDjAMBgNVBAMMBW15dnBuMFkwEwYHKoZIzj0CAQYIKoZI
                    zj0DAQcDQgAEZeLkGxYj+XtWsV5sOm41MOt2jIDhP7c+UjhN6Wlh3uzcxKOB9IfF
                    KfO4SuiiTDfG3hc2dvn7E/5+lUtjwmD6RqOBnzCBnDAJBgNVHRMEAjAAMB0GA1Ud
                    DgQWBBQQGvXKd8H7mXKs/ejsLLj9eLAmrDBOBgNVHSMERzBFgBR/cYhVoW3sF9fM
                    4JcoI8sCjINaoKEipCAwHjEcMBoGA1UEAwwTY25fSENGVUFIaXYxdjBrbWZoWIIJ
                    AP0ltPtENv28MBMGA1UdJQQMMAoGCCsGAQUFBwMCMAsGA1UdDwQEAwIHgDAKBggq
                    hkjOPQQDAgNIADBFAiBfV2Q5/8gMv3375oxn6zgeXGD+7q1kB/CPYytQfkwtEwIh
                    AKhxhvx6UROngiMKW+eWFYYgsNiSIpTZ0TnVruv70AMp
                    -----END CERTIFICATE-----
                    </cert>
                    <key>
                    -----BEGIN PRIVATE KEY-----
                    MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgEJFfkVJlZBoEfO8v
                    G19wo8TRpOzsHJHyfdmP9pQKFqShRANCAARl4uQbFiP5e1axXmw6bjUw63aMgOE/
                    tz5SOE3paWHe7NzEo4H0h8Up87hK6KJMN8beFzZ2+fsT/n6VS2PCYPpG
                    -----END PRIVATE KEY-----
                    </key>
                    <tls-crypt>
                    #
                    # 2048 bit OpenVPN static key
                    #
                    -----BEGIN OpenVPN Static key V1-----
                    f500690acf5d3bf2992b7f4adfe755f4
                    c816bfde4062f3c700564418933eccb2
                    a80d4043d1b719e7cd7c04a979f78a2f
                    1ea222990f835be5c383e2e919cfbf2d
                    abea066a5d82efb59372627c150522e5
                    cef0ea030d789d3f0c86520d8ebbed44
                    286e3f79804a023594a4e677131179c1
                    a339fdbd9270a12fbee2e7c6b79a0f02
                    7c3d2e93d2cfe5fbb166265b86f7a0c9
                    cdc8da0fa926e4ea2897ae2e70298583
                    50b1ce781ab482554930f9c2e9cc6328
                    c6933af654e2956cd34e2b279380af74
                    209e579b896c34da89804acb7ed339d4
                    4d230c177ef87434eb6594b48c4b6f0d
                    9280f5aea7e138fb98ff6fca5b2f0e6a
                    0fb7722e15256f91d89c89cb21e986de
                    -----END OpenVPN Static key V1-----
                    </tls-crypt>
                    
                    """.trimIndent())
        }
    }

}