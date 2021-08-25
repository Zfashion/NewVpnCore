package com.core.ikev2.provide

import com.core.ikev2.logic.TrustedCertificateManager
import com.core.unitevpn.utils.VPNLog
import java.io.ByteArrayInputStream
import java.io.IOException
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

object CertificateUtils {

    fun storeCertificate(cert: String) {
        val certificate = parseCertificate(cert.toByteArray())
        certificate?.let {
            try {
                val store = KeyStore.getInstance("LocalCertificateStore")
                store.load(null, null)
                store.setCertificateEntry(null, it)
                TrustedCertificateManager.getInstance().reset()
                VPNLog.d("Ikev2 store cert success")
            } catch (e: Exception) {
                e.printStackTrace()
                VPNLog.d("Ikev2 store cert error: ${e.message}")
            }
        }
    }

    private fun parseCertificate(bytes: ByteArray): X509Certificate? {
        var certificate: X509Certificate? = null
        try {
            ByteArrayInputStream(bytes).use { `in` ->
                val factory = CertificateFactory.getInstance("X.509")
                certificate = factory.generateCertificate(`in`) as X509Certificate
            }
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return certificate
    }

}