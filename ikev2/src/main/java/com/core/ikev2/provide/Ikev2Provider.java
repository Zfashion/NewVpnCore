package com.core.ikev2.provide;

import android.content.Context;
import android.os.Build;
import com.core.ikev2.security.LocalCertificateKeyStoreProvider;
import com.core.ikev2.security.LocalCertificateStore;
import com.core.unitevpn.base.Type;
import com.core.unitevpn.inter.VpnProvider;
import com.core.unitevpn.utils.VPNLog;
import com.google.auto.service.AutoService;
import org.jetbrains.annotations.NotNull;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.security.Security;


@AutoService(VpnProvider.class)
public class Ikev2Provider implements VpnProvider<Ikev2Impl> {

    static {
        Security.addProvider(new LocalCertificateKeyStoreProvider());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {

            System.loadLibrary("strongswan");

            System.loadLibrary("tpmtss");
            System.loadLibrary("tncif");
            System.loadLibrary("tnccs");
            System.loadLibrary("imcv");

            System.loadLibrary("charon");
            System.loadLibrary("ipsec");
        }
        System.loadLibrary("androidbridge");
    }

    private Reference<Ikev2Impl> weakReference;

    @Override
    public void init(@NotNull Context context) {
        VPNLog.d("Ikev2Provider start init");
        LocalCertificateStore.setContext(context);
//        Ikev2CertHelper.storeCertificate(null);
    }

    @Override
    public Ikev2Impl create() {
        Ikev2Impl ikev2 = new Ikev2Impl();
        weakReference = new WeakReference<>(ikev2);
        return ikev2;
    }

    @Override
    public Ikev2Impl getImpl() {
        if (weakReference == null || weakReference.get() == null) {
            return create();
        } else {
            return weakReference.get();
        }
    }

    @NotNull
    @Override
    public Type getType() {
        return Ikev2Impl.Companion.getTYPE();
    }

}
