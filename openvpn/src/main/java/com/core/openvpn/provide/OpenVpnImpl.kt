package com.core.openvpn.provide

import com.core.unitevpn.base.Type
import com.core.unitevpn.entity.Connection
import com.core.unitevpn.inter.VpnImpl

class OpenVpnImpl: VpnImpl {

    companion object {
        val TYPE = Type.VpnType("openvpn3")
    }

    override val type: Type
        get() = TYPE

    override fun onCreate() {
        
    }

    override fun onDestroy() {
        
    }

    override fun connect() {
        
    }

    override fun disconnect() {
        
    }

    override fun setConnection(connections: List<Connection>) {
        
    }

    override val isActive: Boolean
        get() = false
}