package com.core.unitevpn.obj

import android.app.Notification
import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.core.unitevpn.R
import com.core.unitevpn.UniteVpnManager
import com.core.unitevpn.inter.NotificationImpl
import com.core.unitevpn.base.VpnStatus
import com.core.unitevpn.base.VpnStatus.Status
import com.core.unitevpn.utils.NetFormatUtils

class DefaultNotification: NotificationImpl {

    companion object {

        @JvmField
        var notificationIcon = 0

        @JvmField
        var connectedSmallIcon = 0

        @JvmField
        var unconnectedSmallIcon = 0
    }

    override fun impl(
        context: Context,
        status: Int,
        speedIn: Long,
        speedOut: Long,
        diffIn: Long,
        diffOut: Long
    ): Notification {
        if (notificationIcon == 0) throw NotificationIconResourceException("No Notification Icon set!!!")
        val builder = NotificationCompat.Builder(context,
            UniteVpnManager.notifyHelper.defNotificationChannel.getChannelId(context)
        )
        val iconBitmap = BitmapFactory.decodeResource(context.resources, notificationIcon)
        val title =
            if (status == VpnStatus.CONNECTED) context.getString(getStatusResId(status))
            else context.applicationInfo.loadLabel(context.packageManager)
        val message =
            if (status == VpnStatus.CONNECTED) NetFormatUtils.getNetStat(context, speedIn, diffIn, speedOut, diffOut)
            else context.getString(getStatusResId(status))
        if (status == VpnStatus.CONNECTED) {
            builder.setSmallIcon(if (connectedSmallIcon == 0) R.drawable.ic_vpn_key else connectedSmallIcon)
        } else {
            builder.setSmallIcon(if (unconnectedSmallIcon == 0) R.drawable.ic_vpn_key else unconnectedSmallIcon)
        }
        builder.apply {
            setLargeIcon(iconBitmap)
            setContentTitle(title)
            setContentText(message)
            val vpnPendingIntent = UniteVpnManager.notifyHelper.getVpnPendingIntent(context)
            if (vpnPendingIntent != null) setContentIntent(vpnPendingIntent)
        }
        return builder.build()
    }

    private fun getStatusResId(@Status status: Int): Int {
        return when (status) {
            VpnStatus.CONNECTED -> R.string.status_connected
            VpnStatus.NOT_CONNECTED -> R.string.status_not_connected
            VpnStatus.CONNECTING -> R.string.status_connecting
            VpnStatus.DISCONNECTING -> R.string.status_disconnecting
            VpnStatus.CONNECT_FAIL -> R.string.status_connect_fail
            else -> status
        }
    }

    internal class NotificationIconResourceException(message: String) : RuntimeException(message)
}