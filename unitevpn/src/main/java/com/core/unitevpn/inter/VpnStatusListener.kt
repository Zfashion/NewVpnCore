package com.core.unitevpn.inter

import com.core.unitevpn.base.VpnStatus


/**
 * vpn状态监听接口
 */
interface VpnStatusListener {

    fun onStatusChange(state: VpnStatus)

}