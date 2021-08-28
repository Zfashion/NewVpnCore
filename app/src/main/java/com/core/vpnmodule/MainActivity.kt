package com.core.vpnmodule

import android.content.*
import android.net.VpnService
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.core.openvpn.core.*
import com.core.openvpn.core.VpnStatus.StateListener
import com.core.openvpn.provide.OpenVpnImpl
import com.core.unitevpn.UniteVpnInstance
import com.core.unitevpn.entity.AutoCombineInfo
import com.core.unitevpn.entity.Connection
import com.core.unitevpn.permission.PermissionFragment.Companion.requestPermission
import com.core.vpnmodule.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.StringReader

class MainActivity : AppCompatActivity(), StateListener, VpnStatus.ByteCountListener {

    private val openVpnProfile = """
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
        
        """.trimIndent()

    private lateinit var binding: ActivityMainBinding

    private var mService: IOpenVPNServiceInternal? = null

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = IOpenVPNServiceInternal.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mService = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uniteVpnInstance = UniteVpnInstance(this)

        binding.startBtn.setOnClickListener {
            val connection1 = Connection("测试服务器", "217.138.255.222", "443", false)
            val listOf = listOf(AutoCombineInfo(OpenVpnImpl.TYPE, listOf(connection1)))
            lifecycleScope.launch { uniteVpnInstance.autoConnect(listOf) }
        }
        binding.disconnectBtn.setOnClickListener {
            disconnectVpn()
        }
        VpnStatus.addStateListener(this)
        VpnStatus.addByteCountListener(this)

    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, OpenVPNService::class.java)
        intent.action = OpenVPNService.START_SERVICE
        bindService(intent, mConnection, BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        unbindService(mConnection)
    }

    private fun disconnectVpn() {
        if (mService != null) {
            try {
                mService!!.stopVPN(false)
            } catch (e: RemoteException) {
                VpnStatus.logException(e)
            }
        }
    }

    private suspend fun launchVpn() {
        val intent = VpnService.prepare(this)
        val agreePermission = this.requestPermission(intent)
        if (agreePermission) {
            val configParser = ConfigParser()
            try {
                configParser.parseConfig(StringReader(openVpnProfile))
                val vpnProfile = configParser.convertProfile()
                val instance: ProfileManager = ProfileManager.getInstance(this)
                instance.addProfile(vpnProfile)
                instance.saveProfileList(this)
                instance.saveProfile(this, vpnProfile)
                VPNLaunchHelper.startOpenVpn(vpnProfile, baseContext)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (configParseError: ConfigParser.ConfigParseError) {
                configParseError.printStackTrace()
            }
        } else {
            VpnStatus.updateStateString(
                "USER_VPN_PERMISSION_CANCELLED",
                "",
                R.string.state_user_vpn_permission_cancelled,
                ConnectionStatus.LEVEL_NOTCONNECTED
            )
        }
    }

    override fun updateState(
        state: String?,
        logmessage: String?,
        localizedResId: Int,
        level: ConnectionStatus?,
        Intent: Intent?
    ) {
        Log.i("Vpn-Module", "state: $state, level: ${level.toString()}")
        binding.status.text = level.toString()
    }

    override fun setConnectedVPN(uuid: String?) {
    }

    override fun updateByteCount(`in`: Long, out: Long, diffIn: Long, diffOut: Long) {
        Log.i("Vpn-Module", "in: ${`in`}, out: $out, diffIn：$diffIn, diffOut：$diffOut")
    }


}