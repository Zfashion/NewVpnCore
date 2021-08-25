package com.core.unitevpn.`interface`

import java.util.*

sealed class VpnType

class OpenVpn: VpnType() {
    val type: String = this.javaClass.simpleName.lowercase(Locale.ENGLISH)
}
