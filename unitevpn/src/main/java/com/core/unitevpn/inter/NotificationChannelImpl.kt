package com.core.unitevpn.inter

import android.app.NotificationChannel
import android.content.Context

interface NotificationChannelImpl {

    fun getChannelId(context: Context): String

    fun getChannelName(context: Context): String

    fun impl(context: Context): NotificationChannel

}