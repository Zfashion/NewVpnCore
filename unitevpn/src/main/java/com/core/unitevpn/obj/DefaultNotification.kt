package com.core.unitevpn.obj

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import com.core.unitevpn.R
import com.core.unitevpn.UniteVpnManager
import com.core.unitevpn.inter.NotificationImpl
import com.core.unitevpn.base.VpnStatus
import com.core.unitevpn.helper.UniteVpnNotifyHelper
import com.core.unitevpn.utils.NetFormatUtils

class DefaultNotification: NotificationImpl {

    override fun impl(
        context: Context,
        status: VpnStatus,
        speedIn: Long,
        speedOut: Long,
        diffIn: Long,
        diffOut: Long
    ): Notification {
        val title =
            if (status == VpnStatus.CONNECTED) context.getString(getStatusResId(status))
            else context.applicationInfo.loadLabel(context.packageManager)
        val message =
            if (status == VpnStatus.CONNECTED) NetFormatUtils.getNetStat(context, speedIn, diffIn, speedOut, diffOut)
            else context.getString(getStatusResId(status))
        val builder = NotificationCompat.Builder(
            context,
            UniteVpnManager.notifyHelper.defNotificationChannel.getChannelId(context)
        ).apply {
            setSmallIcon(R.drawable.ic_vpn_key)
            setContentTitle(title)
            setContentText(message)
            setContentIntent(UniteVpnManager.notifyHelper.getVpnPendingIntent(context))
        }
        return builder.build()
    }

    private fun getStatusResId(status: VpnStatus): Int {
        return when(status) {
            VpnStatus.CONNECTED -> R.string.status_connected
            VpnStatus.NOT_CONNECTED -> R.string.status_not_connected
            VpnStatus.CONNECTING -> R.string.status_connecting
            VpnStatus.DISCONNECTING -> R.string.status_disconnecting
            VpnStatus.CONNECT_FAIL -> R.string.status_connect_fail
        }
    }
}