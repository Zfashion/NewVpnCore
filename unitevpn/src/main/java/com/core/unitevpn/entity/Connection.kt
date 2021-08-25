package com.core.unitevpn.entity

data class Connection(
    var name: String,
    var server: String,
    var port: String,
    var useUdp: Boolean,
    var timeOut: Int = 15
)
