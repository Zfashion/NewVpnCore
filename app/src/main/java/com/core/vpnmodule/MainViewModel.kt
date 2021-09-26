package com.core.vpnmodule

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.core.openvpn.provide.OpenVpnImpl
import com.core.unitevpn.UniteVpnManager
import com.core.unitevpn.base.Type
import com.core.unitevpn.base.VpnStatus
import com.core.unitevpn.inter.ByteCountListener
import com.core.unitevpn.inter.VpnStatusListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.StringBuilder

class MainViewModel: ViewModel(), VpnStatusListener, ByteCountListener {

    val vpnStatus = MutableLiveData(VpnStatus.NOT_CONNECTED)

    val enable = Transformations.map(vpnStatus) { value -> if (value == null) true else VpnStatus.isIdle(value)}

    val connectionInfoBuilder: MutableLiveData<StringBuilder> = MutableLiveData()

    val server = MutableLiveData<String>()

    val port = MutableLiveData<String>()

    val ikev2LimitPort = arrayOf("4500", "500")
    val openVpnLimitPort = mapOf(Pair("tcp", listOf("8080","443","102")), Pair("udp", listOf("800","110","119")))

    init {
        UniteVpnManager.addStatusListener(this)
        UniteVpnManager.addByteCountListener(this)
    }

    override fun onStatusChange(state: Int) {
        vpnStatus.value = state
    }

    override fun onByteCountChange(speedIn: Long, speedOut: Long, diffIn: Long, diffOut: Long) {
    }

    fun setConnectionInfo(stringBuilder: StringBuilder) {
        connectionInfoBuilder.postValue(stringBuilder)
    }

    override fun onCleared() {
        super.onCleared()
        UniteVpnManager.removeByteCountListener(this)
        UniteVpnManager.removeStatusListener(this)
    }

    fun getStatusString(status: Int): String {
        return when(status) {
            VpnStatus.NOT_CONNECTED -> "未连接"
            VpnStatus.CONNECTED -> "已连接"
            VpnStatus.CONNECTING -> "连接中"
            VpnStatus.DISCONNECTING -> "取消连接中"
            VpnStatus.CONNECT_FAIL -> "连接失败"
            else -> "null"
        }
    }

}