package com.core.vpnmodule

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.core.openvpn.provide.OpenVpnImpl
import com.core.unitevpn.UniteVpnInstance
import com.core.unitevpn.UniteVpnManager
import com.core.unitevpn.common.statusToString
import com.core.unitevpn.entity.AutoCombineInfo
import com.core.unitevpn.entity.AutoInfo
import com.core.unitevpn.inter.ByteCountListener
import com.core.unitevpn.inter.VpnStatusListener
import com.core.vpnmodule.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), VpnStatusListener, ByteCountListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uniteVpnInstance = UniteVpnInstance(this)
        UniteVpnManager.addStatusListener(this)
        UniteVpnManager.addByteCountListener(this)

        binding.startBtn.setOnClickListener {
            val connection1 = AutoInfo("测试服务器", "114.114.114.114", "4500", false)
            val connection2 = AutoInfo("测试服务器", "114.114.114.114", "8080", false)
            val connection3 = AutoInfo("测试服务器", "114.114.114.114", "800", true)
            val listOf = listOf(AutoCombineInfo(OpenVpnImpl.TYPE, listOf(connection1, connection2, connection3)))
            lifecycleScope.launch { uniteVpnInstance.autoConnect(listOf) }
        }
        binding.disconnectBtn.setOnClickListener {
            lifecycleScope.launch { uniteVpnInstance.disconnect() }
        }
    }

    override fun onStatusChange(state: Int) {
        binding.status.text = state.statusToString()
        Log.d(this.javaClass.simpleName, "onStatusChange state = ${state.statusToString()}")
    }

    override fun onByteCountChange(speedIn: Long, speedOut: Long, diffIn: Long, diffOut: Long) {
        Log.d(this.javaClass.simpleName, "onByteCountChange speedIn = $speedIn, speedOut = $speedOut, diffIn = $diffIn, diffOut = $diffOut")
        Log.d(this.javaClass.simpleName, "onByteCountChange thread name = ${Thread.currentThread().name}")
    }

}