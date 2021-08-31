package com.core.unitevpn.entity

import com.core.unitevpn.base.Type

data class AutoCombineInfo(
    val type: Type.VpnType,
    val conn: List<AutoInfo>
)
