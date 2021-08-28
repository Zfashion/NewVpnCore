package com.core.unitevpn

import androidx.fragment.app.FragmentActivity
import com.core.unitevpn.base.VpnStatus
import com.core.unitevpn.entity.ConnectionInfo
import com.core.unitevpn.helper.UniteVpnNotifyHelper
import com.core.unitevpn.helper.UniteVpnStatusHelper

/**
 * 供外部调用的VpnManager
 */
object UniteVpnManager {

    /**
     * Vpn通知管理类
     */
    val notifyHelper: UniteVpnNotifyHelper by lazy { UniteVpnNotifyHelper() }

    /**
     * Vpn状态管理类
     */
    val statusHelper: UniteVpnStatusHelper by lazy { UniteVpnStatusHelper() }

    /**
     * 记录服务器相关的连接信息
     */
    val connInfoList: MutableList<ConnectionInfo> = mutableListOf()

    /**
     * Vpn当前状态
     */
    val curVpnStatus: VpnStatus by lazy { statusHelper.curVpnStatus }

    /**
     * 外部指定通知要跳转的目标类
     */
    var pendingClass: Class<FragmentActivity>? = null


    internal fun resetConnInfoList() {
        if (connInfoList.isEmpty()) return
        connInfoList.clear()
    }

}