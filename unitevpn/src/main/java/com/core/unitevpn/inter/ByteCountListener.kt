package com.core.unitevpn.inter


/**
 * Vpn流量监听接口
 */
interface ByteCountListener {

    fun onByteCountChange(speedIn: Long, speedOut: Long, diffIn: Long, diffOut: Long)

}