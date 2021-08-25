package com.core.unitevpn.helper

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.core.unitevpn.inter.NotificationChannelImpl
import com.core.unitevpn.inter.NotificationImpl
import com.core.unitevpn.obj.DefaultNotification
import com.core.unitevpn.obj.DefaultNotificationChannel

object VpnNotificationHelper {

    lateinit var defNotification : NotificationImpl

    lateinit var defNotificationChannel: NotificationChannelImpl

    var pendingClass: Class<FragmentActivity>? = null

    fun initDefault() {
        if (!this::defNotificationChannel.isInitialized) defNotificationChannel = DefaultNotificationChannel()
        if (!this::defNotification.isInitialized) defNotification = DefaultNotification()
    }

    fun getVpnPendingIntent(context: Context): PendingIntent? {
        if (pendingClass == null) return null
        val intent = Intent(context, pendingClass)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

}