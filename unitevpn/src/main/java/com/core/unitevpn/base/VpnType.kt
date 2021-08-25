package com.core.unitevpn.base

import java.util.*


sealed class Type {
    class VpnType(type: String): Type() {
        val name : String = type.lowercase(Locale.ENGLISH)
        override fun equals(other: Any?): Boolean = if (other is VpnType) name == other.name else false
        override fun hashCode(): Int = name.hashCode()
        override fun toString(): String = name
    }
}
