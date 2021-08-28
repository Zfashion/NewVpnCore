package com.core.unitevpn.helper

import com.core.unitevpn.UniteVpnManager
import com.core.unitevpn.base.VpnStatus
import com.core.unitevpn.common.doMainJob
import com.core.unitevpn.common.getDefineJob
import com.core.unitevpn.inter.ByteCountListener
import com.core.unitevpn.inter.VpnStatusListener
import kotlinx.coroutines.Dispatchers


/**
 * 提供vpn当前状态和接口添加
 */
class UniteVpnStatusHelper {

    /**
     * 记录当前Vpn的状态，不允许外部操作更改
     */
    internal var curVpnStatus: VpnStatus = VpnStatus.NOT_CONNECTED
        private set

    private val statusListeners = mutableListOf<VpnStatusListener>()

    private val byteCountListeners = mutableListOf<ByteCountListener>()

    fun addStatusListener(listener: VpnStatusListener) = kotlin.run { statusListeners.add(listener) }

    fun addByteCountListener(listener: ByteCountListener) = kotlin.run { byteCountListeners.add(listener) }

    fun removeStatusListener(listener: VpnStatusListener) = kotlin.run { statusListeners.remove(listener) }

    fun removeByteCountListener(listener: ByteCountListener) = kotlin.run { byteCountListeners.remove(listener) }


    internal fun notifyStatusSetChanged(status: VpnStatus) {
        if (status !== UniteVpnManager.statusHelper.curVpnStatus) {
            curVpnStatus = status
            doMainJob {
                statusListeners.forEach {
                    it.onStatusChange(status)
                }
            }
        }
    }


    internal fun notifyByteCountSetChanged(speedIn: Long, speedOut: Long, diffIn: Long, diffOut: Long) {
        byteCountListeners.forEach {
            it.onByteCountChange(speedIn, speedOut, diffIn, diffOut)
        }
    }

}