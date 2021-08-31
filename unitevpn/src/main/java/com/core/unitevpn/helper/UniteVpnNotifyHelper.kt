package com.core.unitevpn.helper

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.core.unitevpn.UniteVpnManager
import com.core.unitevpn.inter.NotificationChannelImpl
import com.core.unitevpn.inter.NotificationImpl
import com.core.unitevpn.obj.DefaultNotification
import com.core.unitevpn.obj.DefaultNotificationChannel


/**
 *  Vpn的通知管理类，不对外开放，如需操作可通过#UniteVpnManager
 */
class UniteVpnNotifyHelper {

    internal lateinit var defNotification : NotificationImpl

    internal lateinit var defNotificationChannel: NotificationChannelImpl

    internal fun initDefault() {
        if (!this::defNotificationChannel.isInitialized) defNotificationChannel = DefaultNotificationChannel()
        if (!this::defNotification.isInitialized) defNotification = DefaultNotification()
    }

    fun getVpnPendingIntent(context: Context): PendingIntent? {
        val pendingClass = UniteVpnManager.pendingClass ?: return null
        val intent = Intent(context, pendingClass)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

}