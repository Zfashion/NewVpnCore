package com.core.unitevpn.`interface`

import android.content.Context
import com.core.unitevpn.base.Type

interface VpnInitialize {

    fun init(context: Context)

    fun getType(): Type

}