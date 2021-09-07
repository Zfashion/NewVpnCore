package com.core.openvpn.provide

import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.core.openvpn.core.*
import com.core.unitevpn.UniteVpnStatusService
import com.core.unitevpn.base.Type
import com.core.unitevpn.common.statusToString
import com.core.unitevpn.entity.AutoInfo
import com.core.unitevpn.inter.VpnImpl
import com.core.unitevpn.utils.VPNLog
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class OpenVpnImpl: VpnImpl, VpnStatus.StateListener, VpnStatus.ByteCountListener {

    companion object {
        val TYPE = Type.VpnType("openvpn3")
    }

    @Volatile
    private var openVpnBinder: IOpenVPNServiceInternal? = null

    private var uniteService: UniteVpnStatusService? = null

    private val serviceConnection: OpenVpnServiceConnection by lazy { OpenVpnServiceConnection() }

    private val profile by lazy { OpenCertHelper.profile }

//    private var lastState: Int? = null

    private var afterDisconnectJob: CancellableContinuation<Unit>? = null

    private fun isBind(): Boolean {
        return openVpnBinder != null
    }

    private val mutex: Mutex = Mutex()

    override val type: Type
        get() = TYPE

    override suspend fun onCreate(uniteService: UniteVpnStatusService) {
        VPNLog.d("OpenVpnImpl >>> onCreate() --> thread = ${Thread.currentThread().name}")
        this.uniteService = uniteService
        //OpenVpnService的启动
        VpnStatus.addStateListener(this)
        VpnStatus.addByteCountListener(this)
        bindOpenVpnService()
    }

    override suspend fun onDestroy() {
        VPNLog.d("OpenVpnImpl >>> onDestroy() --> execute destroy")
        if (afterDisconnectJob?.isCompleted == false) afterDisconnectJob?.cancel()
        afterDisconnectJob = null
        unBindOpenVpnService()
        uniteService = null
        VpnStatus.removeStateListener(this)
        VpnStatus.removeByteCountListener(this)
//        lastState = null
    }

    override suspend fun connect(conn: List<AutoInfo>) {
        if (profile == null) {
            VPNLog.d("OpenVpnImpl >>> connect() --> OpenVpnCertProfile == null, Unable to perform connection")
            notifyStatusChanged(com.core.unitevpn.base.VpnStatus.CONNECT_FAIL)
        } else {
            VPNLog.d("OpenVpnImpl >>> connect() --> OpenVpnCertProfile not null, start to perform connection")
            val startProfile = profile!!
            val toTypedArray = conn.map {
                val connection = Connection()
                connection.mServerName = it.server
                connection.mServerPort = it.port
                connection.mUseUdp = it.useUdp
                connection.mConnectTimeout = it.timeOut
                connection
            }.toTypedArray()
            if (!conn.isNullOrEmpty()) {
                startProfile.mName = conn[0].profileName
            }
            startProfile.mConnections = toTypedArray

            uniteService?.let {
                val instance = ProfileManager.getInstance(it.applicationContext)
                instance.addProfile(startProfile)
                instance.saveProfileList(it.applicationContext)
                instance.saveProfile(it.applicationContext, startProfile)
                VPNLaunchHelper.startOpenVpn(startProfile, it)
            }
        }
    }

    override suspend fun disconnect() {
        VPNLog.d("OpenVpnImpl >>> disconnect() --> execute disconnect")
        if (isBind()) {
            disconnectImpl()
        } else {
            bindOpenVpnService()
            afterDisconnectByBind()
        }
    }


    override val isActive: Boolean
//        get() = lastState == com.core.unitevpn.base.VpnStatus.CONNECTED || lastState == com.core.unitevpn.base.VpnStatus.CONNECTING
        get() = com.core.unitevpn.base.VpnStatus.getCurStatus() == com.core.unitevpn.base.VpnStatus.CONNECTED || com.core.unitevpn.base.VpnStatus.getCurStatus() == com.core.unitevpn.base.VpnStatus.CONNECTING


    override fun updateState(
        state: String?,
        logmessage: String?,
        localizedResId: Int,
        level: ConnectionStatus?,
        Intent: Intent?
    ) {
        var vpnStatus = getVpnStatusFromLevel(level)
        VPNLog.d("OpenVpnImpl >>> updateState() --> finally state string = $state,  vpnStatus = ${vpnStatus?.statusToString() ?: "null"}")
        if (vpnStatus != null && com.core.unitevpn.base.VpnStatus.getCurStatus() != vpnStatus) {
            if (com.core.unitevpn.base.VpnStatus.getCurStatus() == com.core.unitevpn.base.VpnStatus.CONNECTING && vpnStatus == com.core.unitevpn.base.VpnStatus.NOT_CONNECTED) {
                vpnStatus = com.core.unitevpn.base.VpnStatus.CONNECT_FAIL
            }
            notifyStatusChanged(vpnStatus)
        }
    }

    override fun setConnectedVPN(uuid: String?) = Unit

    override fun updateByteCount(`in`: Long, out: Long, diffIn: Long, diffOut: Long) {
        uniteService?.notifyByteCountChanged(`in`, out, diffIn, diffOut)
    }

    /**
     * 绑定openVpn的服务
     */
    private suspend fun bindOpenVpnService() = mutex.withLock {
        if (isBind()) return@withLock
        uniteService?.let {
            val intent = Intent(it, OpenVPNService::class.java)
            intent.action = OpenVPNService.START_SERVICE
            it.bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    /**
     * 解绑openVpn的服务
     */
    private fun unBindOpenVpnService() {
        if (isBind()) {
            uniteService?.unbindService(serviceConnection)
            openVpnBinder = null
        }
    }

    private suspend fun afterDisconnectByBind() = suspendCancellableCoroutine<Unit> {
        while (it.isActive) {
            if (isBind()) {
                disconnectImpl()
                break
            }
        }
        afterDisconnectJob = it
    }

    private fun disconnectImpl() {
//        notifyStatusChanged(com.core.unitevpn.base.VpnStatus.DISCONNECTING)
        try {
            openVpnBinder?.stopVPN(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getVpnStatusFromLevel(level: ConnectionStatus?): Int? {
        VPNLog.d("OpenVpnImpl >>> getVpnStatusFromLevel() --> open vpn status level: ${level?.name}")
        return when(level) {
            ConnectionStatus.LEVEL_CONNECTED -> com.core.unitevpn.base.VpnStatus.CONNECTED
            ConnectionStatus.LEVEL_NOTCONNECTED -> com.core.unitevpn.base.VpnStatus.NOT_CONNECTED
            ConnectionStatus.LEVEL_AUTH_FAILED, ConnectionStatus.UNKNOWN_LEVEL -> com.core.unitevpn.base.VpnStatus.CONNECT_FAIL

            ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED,
            ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET,
            ConnectionStatus.LEVEL_START -> com.core.unitevpn.base.VpnStatus.CONNECTING
            else -> null
        }
    }

    private fun notifyStatusChanged(state: Int) {
//        lastState = state
        VPNLog.d("OpenVpnImpl >>> notifyStatusChanged() --> notify status")
        uniteService?.notifyStatusChanged(state)
    }

    private inner class OpenVpnServiceConnection: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            openVpnBinder = IOpenVPNServiceInternal.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            openVpnBinder = null
        }

    }

}