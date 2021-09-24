package com.core.unitevpn.entity

import com.core.unitevpn.base.Type

data class ConnectionInfo(
    val server: String,
    val port: String,
    val type: Type.VpnType,
    val isUseUdp: Boolean,
    val time: Long,
    val isSuccess: Boolean
) {
    override fun toString(): String {
        return "server= $server, port= $port, type= ${type.name}, isUseUdp= $isUseUdp, time= $time, isSuccess= $isSuccess"
    }
}
