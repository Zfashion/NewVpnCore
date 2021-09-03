package com.core.vpnmodule

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.core.ikev2.provide.Ikev2Impl
import com.core.openvpn.provide.OpenVpnImpl
import com.core.unitevpn.UniteVpnInstance
import com.core.unitevpn.UniteVpnManager
import com.core.unitevpn.entity.AutoCombineInfo
import com.core.unitevpn.entity.AutoInfo
import com.core.vpnmodule.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

//    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val uniteVpnInstance = UniteVpnInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main) //dataBinding
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        /*binding = ActivityMainBinding.inflate(layoutInflater) //viewBinding
        setContentView(binding.root)*/

        binding.startBtn.onClickStart(lifecycleScope) {
            connectVpn()
        }

        binding.disconnectBtn.onClickStart(lifecycleScope) {
            disconnect()
        }

        viewModel.server.value = "104.149.197.158"
        viewModel.port.value = "8080"
    }

    private suspend fun disconnect() {
        uniteVpnInstance.disconnect()
    }

    private var clickAgain = false
    private suspend fun connectVpn() {
        if (viewModel.server.value.isNullOrEmpty() || viewModel.port.value.isNullOrEmpty()) {
            Toast.makeText(this, "先填服务器信息", Toast.LENGTH_SHORT).show()
            return
        }
        if (UniteVpnManager.isActive || clickAgain) {
            if (clickAgain.not()) Toast.makeText(this, "再点击一次就断开", Toast.LENGTH_SHORT).show()
            clickAgain = if (clickAgain) {
                disconnect()
                false
            } else {
                true
            }
            return
        }

        //暂时只有Auto协议
        val ports = viewModel.port.value
        val portList = ports!!.split(",").map {
            it.trim()
            it
        }.toMutableList()

        val ikev2Connection = portList.map {
            AutoInfo(IKEV_NAME, viewModel.server.value!!, it, false)
        }
        val openConnection = mutableListOf<AutoInfo>()
        portList.forEachIndexed { index, s ->
            if (index % 2 == 0) {
                openConnection.add(AutoInfo(OPEN_NAME, viewModel.server.value!!, s, false)) //tcp
            } else {
                openConnection.add(AutoInfo(OPEN_NAME, viewModel.server.value!!, s, true))  //udp
            }
        }
        if (ikev2Connection.isEmpty() && openConnection.isEmpty()) {
            Toast.makeText(this, "port不匹配", Toast.LENGTH_SHORT).show()
            return
        }
        val listOf = listOf(
            AutoCombineInfo(OpenVpnImpl.TYPE, openConnection),
            AutoCombineInfo(Ikev2Impl.TYPE, ikev2Connection)
        )
        uniteVpnInstance.autoConnect(listOf)
    }

    companion object {
        private const val IKEV_NAME = "ikev测试服务器"
        private const val OPEN_NAME = "open测试服务器"
    }

}