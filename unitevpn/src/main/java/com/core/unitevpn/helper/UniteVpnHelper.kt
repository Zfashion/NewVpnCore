package com.core.unitevpn.helper

import com.core.unitevpn.base.VpnStatus
import com.core.unitevpn.base.VpnStatus.Status
import com.core.unitevpn.common.doMainJob
import com.core.unitevpn.inter.ByteCountListener
import com.core.unitevpn.inter.VpnStatusListener


/**
 * 提供vpn接口添加
 */
class UniteVpnHelper {

    private val statusListeners = mutableListOf<VpnStatusListener>()

    private val byteCountListeners = mutableListOf<ByteCountListener>()

    fun addStatusListener(listener: VpnStatusListener) = kotlin.run { statusListeners.add(listener) }

    fun addByteCountListener(listener: ByteCountListener) = kotlin.run { byteCountListeners.add(listener) }

    fun removeStatusListener(listener: VpnStatusListener) = kotlin.run { statusListeners.remove(listener) }

    fun removeByteCountListener(listener: ByteCountListener) = kotlin.run { byteCountListeners.remove(listener) }

    internal fun notifyStatusSetChanged(@Status status: Int) {
        if (status != VpnStatus.getCurStatus()) {
            VpnStatus.updateStatus(status)

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