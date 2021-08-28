package com.core.unitevpn.sdk

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.core.unitevpn.UniteVpnManager
import com.core.unitevpn.inter.VpnProvider
import com.core.unitevpn.base.Type
import com.core.unitevpn.utils.VPNLog
import java.util.*
import kotlin.collections.LinkedHashMap

object UniteVpnSdk {

    @Volatile
    var isInitialized = false

    internal val serviceMap: LinkedHashMap<Type, VpnProvider<*>> = LinkedHashMap()


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
        UniteVpnManager.notifyHelper.initDefault()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defChannel = UniteVpnManager.notifyHelper.defNotificationChannel
            val impl = defChannel.impl(context)
            NotificationManagerCompat.from(context).createNotificationChannel(impl)
//            context.getSystemService(NotificationManager::class.java).createNotificationChannel(impl)
        }
    }

}