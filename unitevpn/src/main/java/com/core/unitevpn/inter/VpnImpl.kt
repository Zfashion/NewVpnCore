package com.core.unitevpn.inter

import com.core.unitevpn.UniteVpnStatusService
import com.core.unitevpn.base.Type
import com.core.unitevpn.entity.AutoInfo

interface VpnImpl {

    val type: Type
    suspend fun onCreate(uniteService: UniteVpnStatusService)
    suspend fun onDestroy()
    suspend fun connect(conn: List<AutoInfo>)
    suspend fun disconnect()
    val isActive: Boolean

}