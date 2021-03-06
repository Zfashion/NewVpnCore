package com.core.unitevpn.sdk

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.core.unitevpn.UniteVpnManager
import com.core.unitevpn.inter.VpnProvider
import com.core.unitevpn.base.Type
import com.core.unitevpn.inter.VpnImpl
import com.core.unitevpn.utils.VPNLog
import java.util.*
import kotlin.collections.LinkedHashMap

object UniteVpnSdk {

    internal var isInitialized = false

    internal val serviceMap: LinkedHashMap<Type, VpnProvider<*>> = LinkedHashMap()


    internal fun init(context: Context) {
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

    private fun getProviderByType(type: Type): VpnProvider<*> {
        return serviceMap[type]
            ?: throw NoSuchElementException("ServiceLoader doesn't load $type provider")
    }

    internal fun getVpnImplByType(type: Type): VpnImpl {
        val provider = getProviderByType(type)
        return provider.getImpl()
    }

}