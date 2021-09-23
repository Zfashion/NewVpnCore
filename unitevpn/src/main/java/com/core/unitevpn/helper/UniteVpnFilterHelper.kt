package com.core.unitevpn.helper

import android.content.Context
import android.content.SharedPreferences

class UniteVpnFilterHelper {

    companion object {
        private val VPN_FILTER_SP_NAME = "VPN_FILTER_SP_NAME"
        private val KEY_ALLOWED_APP_LIST = "KEY_ALLOWED_APP_LIST"
        private val KEY_BLACK_APP_LIST = "KEY_BLACK_APP_LIST"
    }

    private fun getSharePreference(context: Context): SharedPreferences {
        return context.getSharedPreferences(VPN_FILTER_SP_NAME, Context.MODE_PRIVATE)
    }

    fun getAllowedAppList(context: Context): Set<String> {
        return getSharePreference(context).getStringSet(KEY_ALLOWED_APP_LIST, HashSet<String>())!!
    }

    fun setAllowAppList(context: Context, value: Set<String>) {
        getSharePreference(context).edit().putStringSet(KEY_ALLOWED_APP_LIST, value).apply()
    }

    fun getBlackList(context: Context): Set<String> {
        return getSharePreference(context).getStringSet(KEY_BLACK_APP_LIST, HashSet<String>())!!
    }

    fun setBlackList(context: Context, value: Set<String>) {
        getSharePreference(context).edit().putStringSet(KEY_BLACK_APP_LIST, value).apply()
    }

}