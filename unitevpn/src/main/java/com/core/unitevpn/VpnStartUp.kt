package com.core.unitevpn

import android.content.Context
import androidx.startup.Initializer
import com.core.unitevpn.sdk.VpnSdk

class VpnStartUp: Initializer<Unit> {
    override fun create(context: Context) {
        VpnSdk.init(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}