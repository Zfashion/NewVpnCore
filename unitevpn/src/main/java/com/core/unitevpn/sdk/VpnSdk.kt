package com.core.unitevpn.sdk

import android.content.Context
import com.core.unitevpn.utils.VPNLog

object VpnSdk {

    @Volatile
    var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) {
            VPNLog.d("VpnSdk Already initialized")
            return
        }
        VPNLog.d("VpnSdk start init")

    }

}