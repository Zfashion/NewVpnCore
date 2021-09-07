package com.core.unitevpn.base;

import androidx.annotation.IntDef;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import kotlin.jvm.Volatile;

public class VpnStatus {

    public static final int NOT_CONNECTED = 99;
    public static final int CONNECTED = 100;
    public static final int CONNECTING = 101;
    public static final int DISCONNECTING = 102;
    public static final int CONNECT_FAIL = 103;

    @IntDef({NOT_CONNECTED, CONNECTED, CONNECTING, DISCONNECTING, CONNECT_FAIL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status{ }

    @Volatile
    private static @Status int mCurStatus = NOT_CONNECTED;

    public static void updateStatus(@Status int newStatus) {
        mCurStatus = newStatus;
    }

    public static int getCurStatus() {
        return mCurStatus;
    }

    @NotNull
    public static Boolean isIdle() {
        return mCurStatus == NOT_CONNECTED || mCurStatus == CONNECT_FAIL;
    }

    @NotNull
    public static Boolean isIdle(int status) {
        return status == NOT_CONNECTED || status == CONNECT_FAIL;
    }

    @NotNull
    public static Boolean isConnected() {
        return mCurStatus == CONNECTED;
    }

    @NotNull
    public static Boolean isNotConnected() {
        return mCurStatus == NOT_CONNECTED;
    }

    @NotNull
    public static Boolean isDisconnecting() {
        return mCurStatus == DISCONNECTING;
    }

    @NotNull
    public static Boolean isActive() {
        return mCurStatus == CONNECTED || mCurStatus == CONNECTING;
    }

    @NotNull
    public static Boolean isActive(int status) {
        return status == CONNECTED || status == CONNECTING;
    }

    @NotNull
    public static Boolean isStatusToReport() {
        return mCurStatus == CONNECTED || mCurStatus == CONNECT_FAIL;
    }

}
