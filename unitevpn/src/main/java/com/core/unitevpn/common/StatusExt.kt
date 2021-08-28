package com.core.unitevpn.common

import com.core.unitevpn.base.VpnStatus


fun Int.statusToString(): String {
    return when {
        this == VpnStatus.NOT_CONNECTED -> "NOT_CONNECTED"
        this == VpnStatus.CONNECTED -> "CONNECTED"
        this == VpnStatus.CONNECTING -> "CONNECTING"
        this == VpnStatus.DISCONNECTING -> "DISCONNECTING"
        this == VpnStatus.CONNECT_FAIL -> "CONNECT_FAIL"
        else -> this.toString()
    }
}