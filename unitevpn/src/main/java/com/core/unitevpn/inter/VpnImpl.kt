package com.core.unitevpn.inter

import com.core.unitevpn.base.Type
import com.core.unitevpn.entity.Connection

interface VpnImpl {

    val type: Type
    fun onCreate()
    fun onDestroy()
    fun connect(conn: List<Connection>)
    fun disconnect()
    fun setConnection(connections: List<Connection>)
    val isActive: Boolean

}