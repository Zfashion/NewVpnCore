package com.core.unitevpn.base

/**
 * VPN状态枚举
 */
enum class VpnStatus {
    NOT_CONNECTED,
    CONNECTED,
    CONNECTING,
    DISCONNECTING,
    CONNECT_FAIL;

    val isIdle: Boolean
        get() { return (this == NOT_CONNECTED || this == CONNECT_FAIL) }

    val isConnected: Boolean
        get() { return this == CONNECTED }

    val isConnecting: Boolean
        get() { return this == CONNECTING }

    val isDisconnecting: Boolean
        get() { return this == DISCONNECTING }

    val isActive: Boolean
        get() { return (this == CONNECTED || this == CONNECTING) }

}