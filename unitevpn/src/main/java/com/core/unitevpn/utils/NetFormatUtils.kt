package com.core.unitevpn.utils

import android.content.Context
import com.core.unitevpn.R
import kotlin.math.ln
import kotlin.math.pow

object NetFormatUtils {

    fun getNetStat(context: Context, `in`: Long, diffIn: Long, out: Long, diffOut: Long): String {
        return context.getString(
            R.string.unite_statusline_bytecount,
            humanReadableByteCount(`in`, false, context),
            humanReadableByteCount(diffIn / 2, true, context),
            humanReadableByteCount(out, false, context),
            humanReadableByteCount(diffOut / 2, true, context)
        )
    }

    private fun humanReadableByteCount(bytes: Long, speed: Boolean, context: Context): String {
        var newBytes = bytes
        if (speed) newBytes *= 8
        val unit = if (speed) 1000 else 1024
        val exp = 0.coerceAtLeast((ln(newBytes.toDouble()) / ln(unit.toDouble())).toInt().coerceAtMost(3))
        val bytesUnit = (newBytes / unit.toDouble().pow(exp.toDouble())).toFloat()
        return if (speed) when (exp) {
            0 -> context.getString(R.string.unite_bits_per_second, bytesUnit)
            1 -> context.getString(R.string.unite_kbits_per_second, bytesUnit)
            2 -> context.getString(R.string.unite_mbits_per_second, bytesUnit)
            else -> context.getString(R.string.unite_gbits_per_second, bytesUnit)
        } else when (exp) {
            0 -> context.getString(R.string.unite_volume_byte, bytesUnit)
            1 -> context.getString(R.string.unite_volume_kbyte, bytesUnit)
            2 -> context.getString(R.string.unite_volume_mbyte, bytesUnit)
            else -> context.getString(R.string.unite_volume_gbyte, bytesUnit)
        }
    }
    
}