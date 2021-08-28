package com.core.unitevpn

import androidx.fragment.app.FragmentActivity
import com.core.unitevpn.base.VpnStatus.Status
import com.core.unitevpn.entity.ConnectionInfo
import com.core.unitevpn.helper.UniteVpnNotifyHelper
import com.core.unitevpn.helper.UniteVpnHelper

/**
 * 供外部调用的VpnManager
 */
object UniteVpnManager {

    /**
     * Vpn通知管理类
     */
    val notifyHelper: UniteVpnNotifyHelper by lazy { UniteVpnNotifyHelper() }

    /**
     * Vpn接口管理类
     */
    private val vpnHelper: UniteVpnHelper by lazy { UniteVpnHelper() }

    /**
     * 记录服务器相关的连接信息
     */
    var connInfoList: MutableList<ConnectionInfo> = mutableListOf()
        private set

    /**
     * 外部指定通知要跳转的目标类
     */
    var pendingClass: Class<FragmentActivity>? = null


    internal fun resetConnInfoList() {
        if (connInfoList.isEmpty()) return
        connInfoList.clear()
    }

    internal fun notifyStatus(@Status status: Int) {
        vpnHelper.notifyStatusSetChanged(status)
    }

    internal fun notifyByteCount(speedIn: Long, speedOut: Long, diffIn: Long, diffOut: Long) {
        vpnHelper.notifyByteCountSetChanged(speedIn, speedOut, diffIn, diffOut)
    }

}