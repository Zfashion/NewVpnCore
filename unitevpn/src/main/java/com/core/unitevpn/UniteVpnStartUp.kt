package com.core.unitevpn

import android.content.Context
import androidx.startup.Initializer
import com.core.unitevpn.sdk.UniteVpnSdk

class UniteVpnStartUp: Initializer<Unit> {
    override fun create(context: Context) {
        UniteVpnSdk.init(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}