package com.core.unitevpn.inter

import com.core.unitevpn.base.Type
import com.core.unitevpn.entity.Connection

interface VpnImpl {

    val type: Type
    fun prepare()
    fun unBind()
    fun connect()
    fun disconnect()
    fun setConnection(connections: List<Connection>)


}