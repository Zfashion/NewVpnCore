package com.core.unitevpn.utils;

import android.util.Log;

import androidx.annotation.IntDef;

import com.core.unitevpn.BuildConfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class VPNLog {

    private static final String TAG = "VPNLog";

    private static final int LOG_VERBOSE = 0;
    private static final int LOG_DEBUG = 1;
    private static final int LOG_INFO = 2;
    private static final int LOG_WARN = 3;
    private static final int LOG_ERROR = 4;

    @IntDef(value = {LOG_VERBOSE, LOG_DEBUG, LOG_INFO, LOG_WARN, LOG_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    @interface LogLevel{}

    public static void v(String msg) { log(LOG_VERBOSE, msg); }
    public static void d(String msg) { log(LOG_DEBUG, msg); }
    public static void i(String msg) { log(LOG_INFO, msg); }
    public static void w(String msg) { log(LOG_WARN, msg); }
    public static void e(String msg) { log(LOG_ERROR, msg); }

    private static void log(@LogLevel int level, String msg) {
        if (!BuildConfig.DEBUG) return;
        switch (level) {
            case LOG_VERBOSE:
                Log.v(TAG, msg);
                break;
            case LOG_DEBUG:
                Log.d(TAG, msg);
                break;
            case LOG_INFO:
                Log.i(TAG, msg);
                break;
            case LOG_WARN:
                Log.w(TAG, msg);
                break;
            case LOG_ERROR:
                Log.e(TAG, msg);
                break;
        }
    }

}
