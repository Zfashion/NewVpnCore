package com.core.openvpn.provide;

import android.content.Context;
import android.os.Build;

import com.core.openvpn.api.AppRestrictions;
import com.core.openvpn.core.PRNGFixes;
import com.core.openvpn.core.StatusListener;
import com.core.unitevpn.base.Type;
import com.core.unitevpn.inter.VpnProvider;
import com.core.unitevpn.utils.VPNLog;
import com.google.auto.service.AutoService;

import org.jetbrains.annotations.NotNull;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

@AutoService(VpnProvider.class)
public class OpenVpnProvider implements VpnProvider<OpenVpnImpl> {

    private Reference<OpenVpnImpl> weakReference;

    @Override
    public void init(@NotNull Context context) {
        VPNLog.d("OpenVpnProvider start init");
        PRNGFixes.apply();
        StatusListener listener = new StatusListener();
        listener.init(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AppRestrictions.getInstance(context).checkRestrictions(context);
        }
    }

    @Override
    public OpenVpnImpl create() {
        OpenVpnImpl openVpn = new OpenVpnImpl();
        weakReference = new WeakReference<>(openVpn);
        return openVpn;
    }

    @Override
    public OpenVpnImpl getImpl() {
        if (weakReference == null || weakReference.get() == null) {
            return create();
        } else {
            return weakReference.get();
        }
    }

    @NotNull
    @Override
    public Type getType() {
        return OpenVpnImpl.Companion.getTYPE();
    }
}
