package com.core.unitevpn.obj

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.core.unitevpn.R
import com.core.unitevpn.inter.NotificationChannelImpl

class DefaultNotificationChannel: NotificationChannelImpl {
    override fun getChannelId(context: Context): String = "${context.packageName}.VPN_STATE_NOTIFICATION.CHANNEL"

    override fun getChannelName(context: Context): String = "${context.applicationInfo.loadLabel(context.packageManager)} Notification Status"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun impl(context: Context): NotificationChannel {
        return NotificationChannel(getChannelId(context), getChannelName(context), NotificationManager.IMPORTANCE_LOW).apply {
            enableLights(false)
            enableVibration(false)
            description = context.getString(R.string.notification_channel_description)
//            lockscreenVisibility = Notification.VISIBILITY_SECRET
        }
    }
}