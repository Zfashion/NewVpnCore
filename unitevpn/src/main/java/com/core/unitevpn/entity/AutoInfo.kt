package com.core.unitevpn.entity

data class AutoInfo(
    var profileName: String,
    var server: String,
    var port: String,
    var useUdp: Boolean,
    var timeOut: Int = 15
)
