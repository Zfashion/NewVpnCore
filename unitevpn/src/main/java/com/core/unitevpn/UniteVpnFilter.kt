package com.core.unitevpn

import android.content.Context
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import androidx.annotation.RequiresApi
import com.core.unitevpn.utils.VPNLog

class UniteVpnFilter {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun apply(context: Context, builder: VpnService.Builder) {
        VPNLog.d("UniteVpnFilter >>> apply() --> start filter")
        val allowedAppList = UniteVpnManager.filterHelper.getAllowedAppList(context)
        val blackList = UniteVpnManager.filterHelper.getBlackList(context)

        if (allowedAppList.isNullOrEmpty() && blackList.isNullOrEmpty()) {
            VPNLog.e("UniteVpnFilter >>> apply() --> need to add allowedAppList and blackList!!!")
            return
        }

        if (allowedAppList.isNullOrEmpty()) {
            blackList.forEach {
                try {
                    builder.addDisallowedApplication(it)
                } catch (e: PackageManager.NameNotFoundException) {
                    VPNLog.e("UniteVpnFilter >>> apply() --> add black list fail, message= ${e.message}")
                }
            }
        } else {
            val hashSetOf = hashSetOf<String>()
            hashSetOf.add(context.packageName)
            allowedAppList.forEach {
                if (blackList.contains(it).not()) {
                    hashSetOf.add(it)
                }
            }
            hashSetOf.forEach {
                try {
                    builder.addAllowedApplication(it)
                } catch (e: PackageManager.NameNotFoundException) {
                    VPNLog.e("UniteVpnFilter >>> apply() --> add allowed list fail, message= ${e.message}")
                }
            }
        }
        VPNLog.d("UniteVpnFilter >>> apply() --> filter end")
    }

}