package com.core.ikev2.provide

import com.core.unitevpn.base.Type
import com.core.unitevpn.entity.Connection
import com.core.unitevpn.inter.VpnImpl

class Ikev2Impl: VpnImpl {

    companion object {
        val TYPE = Type.VpnType("ikev2")
    }
    
    override val type: Type
        get() = TYPE

    override fun onCreate() {
        
    }

    override fun onDestroy() {
        
    }

    override fun connect(conn: List<Connection>) {
        
    }

    override fun disconnect() {
        
    }

    override fun setConnection(connections: List<Connection>) {
        
    }

    override val isActive: Boolean
        get() = false

}