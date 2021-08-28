package com.core.unitevpn.inter

import android.app.Notification
import android.content.Context
import com.core.unitevpn.base.VpnStatus.Status

interface NotificationImpl {

    fun impl(context: Context, @Status status: Int, speedIn: Long, speedOut: Long, diffIn: Long, diffOut: Long): Notification

    fun impl(context: Context, @Status status: Int): Notification = impl(context, status, 0, 0, 0, 0)

}