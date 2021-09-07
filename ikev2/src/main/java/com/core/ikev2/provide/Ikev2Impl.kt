package com.core.ikev2.provide

import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.core.ikev2.data.VpnProfile
import com.core.ikev2.data.VpnProfileDataSource
import com.core.ikev2.data.VpnType
import com.core.ikev2.logic.VpnStateService
import com.core.unitevpn.UniteVpnStatusService
import com.core.unitevpn.base.Type
import com.core.unitevpn.base.VpnStatus
import com.core.unitevpn.common.*
import com.core.unitevpn.entity.AutoInfo
import com.core.unitevpn.inter.VpnImpl
import com.core.unitevpn.utils.VPNLog
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

class Ikev2Impl: VpnImpl, VpnStateService.VpnStateListener, NetTraffics.ByteCountListener {

    companion object {
        val TYPE = Type.VpnType("ikev2")
    }

    private var uniteService: UniteVpnStatusService? = null

    @Volatile
    private var ikev2Binder: VpnStateService? = null

    private var lastState : Int? = null

    private val serviceConnection: Ikev2ServiceConnection by lazy { Ikev2ServiceConnection() }

    private val netTraffics = NetTraffics()

    private val mutex: Mutex = Mutex()

    private var afterConnectJob: CancellableContinuation<Unit>? = null
    private var afterDisconnectJob: CancellableContinuation<Unit>? = null

    private fun isBind(): Boolean {
        return ikev2Binder != null
    }

    override val type: Type
        get() = TYPE

    override suspend fun onCreate(uniteService: UniteVpnStatusService) {
        VPNLog.d("Ikev2Impl >>> onCreate() --> thread = ${Thread.currentThread().name}")
        this.uniteService = uniteService
        netTraffics.addByteCountListener(this)
        bindIkev2Service()
    }

    override suspend fun onDestroy() {
        VPNLog.d("Ikev2Impl >>> onDestroy() --> execute destroy")
        if (afterConnectJob?.isCompleted == false) afterConnectJob?.cancel()
        if (afterDisconnectJob?.isCompleted == false) afterDisconnectJob?.cancel()
        afterConnectJob = null
        afterDisconnectJob = null
        ikev2Binder?.unregisterListener(this)
        netTraffics.removeByteByteCountListener(this)
        unBindIkev2Service()
        uniteService = null
        lastState = null
    }

    override suspend fun connect(conn: List<AutoInfo>) {
        if (isBind()) {
            VPNLog.d("Ikev2Impl >>> connect() --> ikev2Binder != null, connectImpl")
            connectImpl(conn)
        } else {
            VPNLog.d("Ikev2Impl >>> connect() --> ikev2Binder == null, need to bind ikev2Service")
            bindIkev2Service()
            afterConnectByBind(conn)
        }
    }

    override suspend fun disconnect() {
        VPNLog.d("Ikev2Impl >>> disconnect() --> execute disconnect")
        if (afterConnectJob?.isCompleted == false) afterConnectJob?.cancel()
        if (isBind()) {
            ikev2Binder?.disconnect()
        } else {
            bindIkev2Service()
            afterDisconnectByBind()
        }
    }

    override val isActive: Boolean
        get() {
            return if (isBind()) {
                VpnStatus.isActive(getVpnStatusFromService(ikev2Binder!!))
            } else {
                false
            }
        }


    override fun stateChanged() {
        val status = if (isBind()) {
            getVpnStatusFromService(ikev2Binder!!)
        } else {
            VpnStatus.NOT_CONNECTED
        }
        VPNLog.d("Ikev2Impl >>> stateChanged() --> status= ${status.statusToString()}")
        if (status != lastState) {
            lastState = status
            if (status == VpnStatus.CONNECTED) {
                netTraffics.start()
            } else if (status == VpnStatus.DISCONNECTING) {
                netTraffics.stop()
            } else if (status == VpnStatus.CONNECT_FAIL) {
                getDefineJob(Dispatchers.Default) {
                    delay(10)
                    uniteService?.notifyStatusChanged(VpnStatus.NOT_CONNECTED)
                }.start()
            }
            VPNLog.d("Ikev2Impl >>> stateChanged() --> notify status")
            uniteService?.notifyStatusChanged(status)
        }
    }


    /**
     * 绑定ikev2的服务
     */
    private suspend fun bindIkev2Service() = mutex.withLock {
        if (isBind()) return@withLock
        uniteService?.let {
            VPNLog.d("Ikev2Impl >>> bindIkev2Service() --> start to bind")
            val intent = Intent(it, VpnStateService::class.java)
            it.bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    /**
     * 解绑ikev2服务
     */
    private suspend fun unBindIkev2Service() = mutex.withLock {
        if (isBind()) {
            uniteService?.unbindService(serviceConnection)
            ikev2Binder = null
        }
    }


    private fun connectImpl(conn: List<AutoInfo>) {
        val profileList = mutableListOf<VpnProfile>()
        val userName = Ikev2CertHelper.userName
        val password = Ikev2CertHelper.password
        val remoteId = Ikev2CertHelper.remoteId
        VPNLog.d("Ikev2Impl >>> connectImpl --> userName= $userName, password= $password, remoteId= $remoteId")
        conn.forEachIndexed { index, value ->
            VPNLog.d("Ikev2Impl >>> connectImpl --> connections item= $value, index= $index")
            val vpnProfile = VpnProfile()
            vpnProfile.id = index.toLong()
            vpnProfile.flags = 0
            vpnProfile.vpnType = VpnType.IKEV2_EAP
            vpnProfile.username = userName
            vpnProfile.password = password
            vpnProfile.remoteId = remoteId
            vpnProfile.name = value.profileName
            vpnProfile.gateway = value.server
            vpnProfile.port = value.port.toInt()
            profileList.add(vpnProfile)
        }
        val instance = VpnProfileDataSource.getInstance()
        instance.clearProfiles()
        instance.addNewProfiles(profileList)
        val bundle = Bundle()
        bundle.putString(VpnProfileDataSource.KEY_USERNAME, userName)
        bundle.putString(VpnProfileDataSource.KEY_PASSWORD, password)
        ikev2Binder?.startConnect(bundle, true)
    }



    private suspend fun afterConnectByBind(conn: List<AutoInfo>) = suspendCancellableCoroutine<Unit> {
        while (it.isActive) {
            VPNLog.d("Ikev2Impl >>> afterConnectByBind() --> await bind service than to connect")
            if (isBind()) {
                VPNLog.d("Ikev2Impl >>> afterConnectByBind() --> ikev2Binder not null, can to connect")
                connectImpl(conn)
                it.resume(Unit)
                break
            }
        }
        afterConnectJob = it
    }

    private suspend fun afterDisconnectByBind() = suspendCancellableCoroutine<Unit> {
        while (it.isActive) {
            VPNLog.d("Ikev2Impl >>> afterDisconnectByBind() --> await bind service than to disconnect")
            if (isBind()) {
                VPNLog.d("Ikev2Impl >>> afterDisconnectByBind() --> ikev2Binder not null, can to disconnect")
                getDefineJob(it.context) { disconnect() }
                it.resume(Unit)
                break
            }
        }
        afterDisconnectJob = it
    }

    private inner class Ikev2ServiceConnection: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            VPNLog.d("Ikev2ServiceConnection >>> onServiceConnected() --> service bind success")
            val binder = (service as VpnStateService.LocalBinder).service
            ikev2Binder = binder
            binder.registerListener(this@Ikev2Impl)
            lastState = getVpnStatusFromService(binder)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            VPNLog.d("Ikev2ServiceConnection >>> onServiceConnected() --> service bind fail")
            ikev2Binder?.unregisterListener(this@Ikev2Impl)
            ikev2Binder = null
        }
    }

    private fun getVpnStatusFromService(binder: VpnStateService): Int {
        VPNLog.d("Ikev2Impl >>> getVpnStatusFromService() --> errorState= ${binder.errorState.name}, state= ${binder.state.name}")
        return if (binder.errorState != VpnStateService.ErrorState.NO_ERROR) {
            VpnStatus.CONNECT_FAIL
        } else {
            when (binder.state) {
                VpnStateService.State.CONNECTED -> VpnStatus.CONNECTED
                VpnStateService.State.CONNECTING -> VpnStatus.CONNECTING
                VpnStateService.State.DISABLED -> VpnStatus.NOT_CONNECTED
                VpnStateService.State.DISCONNECTING -> VpnStatus.DISCONNECTING
                else -> VpnStatus.NOT_CONNECTED
            }
        }
    }

    override fun updateByteCount(speedIn: Long, speedOut: Long, diffIn: Long, diffOut: Long) {
        uniteService?.notifyByteCountChanged(speedIn, speedOut, diffIn, diffOut)
    }

}