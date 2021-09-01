package com.core.ikev2.provide

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Ikev2CertHelper {

    @JvmStatic
    var userName: String? = null
    @JvmStatic
    var password: String? = null
    @JvmStatic
    var remoteId: String? = null

    /**
     * 必须在Ikev2Provider初始化之后再存储证书
     */
    @JvmStatic
    suspend fun storeCertificate(cert: String?) = withContext(Dispatchers.IO) {
        cert?.let {
            Ikev2CertificateUtils.storeCertificate(it)
        }
    }

}