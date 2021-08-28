package com.core.unitevpn.inter

import android.content.Context
import com.core.unitevpn.base.Type
import java.lang.ref.WeakReference

interface VpnProvider<T: VpnImpl> {

    fun init(context: Context)

    fun create(): T

    fun getImpl(): T

    fun getType(): Type

}