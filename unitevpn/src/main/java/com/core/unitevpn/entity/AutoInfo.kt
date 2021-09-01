package com.core.unitevpn.entity

data class AutoInfo(
    var profileName: String,
    var server: String,
    var port: String,
    var useUdp: Boolean,
    var timeOut: Int = 15
) {
    override fun toString(): String {
        return "profileName= $profileName, server= $server, port= $port, useUdp= $useUdp, timeOut= $timeOut"
    }
}
