package com.core.ikev2.provide

import com.core.unitevpn.UniteVpnStatusService
import com.core.unitevpn.base.Type
import com.core.unitevpn.entity.AutoInfo
import com.core.unitevpn.inter.VpnImpl

class Ikev2Impl: VpnImpl {

    companion object {
        val TYPE = Type.VpnType("ikev2")
    }
    
    override val type: Type
        get() = TYPE

    override suspend fun onCreate(uniteService: UniteVpnStatusService) {
        
    }

    override suspend fun onDestroy() {
        
    }

    override suspend fun connect(conn: List<AutoInfo>) {
        
    }

    override suspend fun disconnect() {
        
    }

    override val isActive: Boolean
        get() = false

}