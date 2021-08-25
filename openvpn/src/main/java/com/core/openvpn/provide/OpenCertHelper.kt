package com.core.openvpn.provide

import com.core.openvpn.VpnProfile
import com.core.openvpn.core.ConfigParser
import com.core.unitevpn.utils.VPNLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStreamReader

object OpenCertHelper {

    var profile: VpnProfile? = null
    private set

    @JvmStatic
    suspend fun setupCert(cert: String) = withContext(Dispatchers.IO) {
        val configParser = ConfigParser()
        val inputStreamReader = InputStreamReader(cert.byteInputStream())

        try {
            inputStreamReader.use {
                configParser.parseConfig(it)
                profile = configParser.convertProfile()
            }
            VPNLog.d("OpenVpn store cert success")
        } catch (e: IOException) {
            e.printStackTrace()
            VpnProfile("Profile")
            VPNLog.d("OpenVpn store cert fail")
        } catch (configParseError: ConfigParser.ConfigParseError) {
            configParseError.printStackTrace()
            VpnProfile("Profile")
            VPNLog.d("OpenVpn store cert fail")
        }
    }

}