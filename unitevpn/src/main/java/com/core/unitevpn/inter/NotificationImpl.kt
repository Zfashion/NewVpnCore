package com.core.unitevpn.inter

import android.app.Notification
import android.content.Context
import com.core.unitevpn.base.VpnStatus

interface NotificationImpl {

    fun impl(context: Context, status: VpnStatus, speedIn: Long, speedOut: Long, diffIn: Long, diffOut: Long): Notification

}