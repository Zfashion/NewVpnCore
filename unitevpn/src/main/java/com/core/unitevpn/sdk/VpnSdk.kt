package com.core.unitevpn.sdk

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.core.unitevpn.inter.VpnProvider
import com.core.unitevpn.base.Type
import com.core.unitevpn.helper.VpnNotificationHelper
import com.core.unitevpn.utils.VPNLog
import java.util.*
import kotlin.collections.LinkedHashMap

object VpnSdk {

    @Volatile
    var isInitialized = false

    private val serviceMap: LinkedHashMap<Type, VpnProvider<*>> = LinkedHashMap()


    fun init(context: Context) {
        if (isInitialized) {
            VPNLog.d("VpnSdk Already initialized")
            return
        }
        VPNLog.d("VpnSdk start init")
        val applicationContext = context.applicationContext
        loadVpnProviders(applicationContext)
        initDefNotification(applicationContext)
        isInitialized = true
        VPNLog.d("VpnSdk init finish")
    }

    private fun loadVpnProviders(applicationContext: Context) {
        val loadIterator = ServiceLoader.load(VpnProvider::class.java).iterator()
        if (loadIterator.hasNext().not()) return
        loadIterator.forEach {
            VPNLog.d("init all VpnProvider, name is ${it.getType()}")
            serviceMap[it.getType()] = it
            it.init(applicationContext)
        }
    }

    private fun initDefNotification(context: Context) {
        VpnNotificationHelper.initDefault()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defChannel = VpnNotificationHelper.defNotificationChannel
            val impl = defChannel.impl(context)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(impl)
        }
    }

}