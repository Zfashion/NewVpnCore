package com.core.unitevpn

import com.core.unitevpn.base.VpnStatus
import com.core.unitevpn.base.VpnStatus.Status
import com.core.unitevpn.entity.ConnectionInfo
import com.core.unitevpn.helper.UniteVpnFilterHelper
import com.core.unitevpn.helper.UniteVpnNotifyHelper
import com.core.unitevpn.helper.UniteVpnHelper
import com.core.unitevpn.inter.ByteCountListener
import com.core.unitevpn.inter.VpnStatusListener

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
     * vpn过滤类
     */
    val filterHelper: UniteVpnFilterHelper by lazy { UniteVpnFilterHelper() }

    /**
     * 记录服务器相关的连接信息
     */
    var connInfoList: MutableList<ConnectionInfo> = mutableListOf()
        private set

    /**
     * 外部指定通知要跳转的目标类
     */
    var pendingClass: Class<*>? = null

    /**
     * 供外部使用，返回vpn状态是否处于活动状态
     */
    val isActive: Boolean
        get() = VpnStatus.isActive()

    /**
     * 供外部使用，返回当前Vpn的状态
     */
    val vpnStatus : Int
        get() = VpnStatus.getCurStatus()


    fun addStatusListener(l: VpnStatusListener) = vpnHelper.addStatusListener(l)
    fun addByteCountListener(l: ByteCountListener) = vpnHelper.addByteCountListener(l)
    fun removeStatusListener(l: VpnStatusListener) = vpnHelper.removeStatusListener(l)
    fun removeByteCountListener(l: ByteCountListener) = vpnHelper.removeByteCountListener(l)


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