package com.core.unitevpn.inter

import com.core.unitevpn.base.VpnStatus.Status


/**
 * vpn状态监听接口
 */
interface VpnStatusListener {

    fun onStatusChange(@Status state: Int)

}