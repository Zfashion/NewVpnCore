package com.core.unitevpn

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.core.unitevpn.base.Type
import com.core.unitevpn.base.VpnStatus
import com.core.unitevpn.base.VpnStatus.Status
import com.core.unitevpn.common.doMainJob
import com.core.unitevpn.common.getDefineJob
import com.core.unitevpn.common.statusToString
import com.core.unitevpn.common.withInvoke
import com.core.unitevpn.entity.AutoCombineInfo
import com.core.unitevpn.entity.AutoInfo
import com.core.unitevpn.inter.VpnImpl
import com.core.unitevpn.sdk.UniteVpnSdk
import com.core.unitevpn.utils.VPNLog
import kotlinx.coroutines.Dispatchers
import java.util.*


/**
 * 统一管理Vpn状态的Service
 */
class UniteVpnStatusService : Service() {

    companion object {
        private const val NOTIFY_ID = 101
        private const val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"

        fun bindVpnStatusService(context: Context, conn: ServiceConnection) {
            val intent = Intent(context, UniteVpnStatusService::class.java)
            context.bindService(intent, conn, BIND_AUTO_CREATE)
        }

        fun startForeground(context: Context) {
            val intent = Intent(context, UniteVpnStatusService::class.java)
            intent.action = ACTION_START_FOREGROUND_SERVICE
            ContextCompat.startForegroundService(context, intent)
        }
    }

    inner class UniteVpnBinder: Binder() {
        fun getService() = this@UniteVpnStatusService

        fun isBind() = uniteVpnBinder != null

        fun autoConnect(list: List<AutoCombineInfo>) = this@UniteVpnStatusService.autoConnect(list)

        fun disconnect() = this@UniteVpnStatusService.disconnect()
    }

    var uniteVpnBinder: UniteVpnBinder? = null
    private lateinit var notificationManager: NotificationManagerCompat
    private val connectList = LinkedList<AutoCombineInfo>()

    @Volatile
    private lateinit var vpnImpl: VpnImpl


    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationManagerCompat.from(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return super.onStartCommand(intent, flags, startId)
        if (intent.action == ACTION_START_FOREGROUND_SERVICE) {
            //展示通知
            val showNotification = showNotification(VpnStatus.getCurStatus())
            startForeground(NOTIFY_ID, showNotification)
            return START_NOT_STICKY
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        val binder = UniteVpnBinder()
        uniteVpnBinder = binder
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        doMainJob {
            vpnImpl.onDestroy()
        }
    }

    private fun showNotification(@Status status: Int): Notification {
        val notification =
            UniteVpnManager.notifyHelper.defNotification.impl(this, status)
        notificationManager.notify(NOTIFY_ID, notification)
        return notification
    }

    private fun showNotification(@Status status: Int, speedIn: Long, speedOut: Long, diffIn: Long, diffOut: Long): Notification {
        val notification =
            UniteVpnManager.notifyHelper.defNotification.impl(this, status, speedIn, speedOut, diffIn, diffOut)
        notificationManager.notify(NOTIFY_ID, notification)
        return notification
    }


    private fun autoConnect(list: List<AutoCombineInfo>) {
        getDefineJob(Dispatchers.Default) {
            VPNLog.d("UniteVpnStatusService >>> autoConnect() --> start auto connect")
            jobPrepare()
            connectList.addAll(list)
            if (connectList.isNullOrEmpty().not()) {
                executeConnect()
            }
        }.start()
    }

    private fun disconnect() = getDefineJob(Dispatchers.Default) {
        vpnImpl.disconnect()
    }


    private suspend fun jobPrepare() {
        //清除上次自动连接配置的服务器集合
//        if (connectList.isNotEmpty()) connectList.clear()

        //清除上次记录的服务器连接信息内容
        UniteVpnManager.resetConnInfoList()
    }

    private suspend fun executeConnect() = getDefineJob(Dispatchers.Default) {
        val autoInfo = connectList.poll()
        autoInfo?.let {
            checkType(it.type)
            VPNLog.d("UniteVpnStatusService >>> executeConnect() --> finally vpnImpl type is ${vpnImpl.type}")
            executeRealConnect(it.conn)
        }
    }

    private suspend fun executeRealConnect(conn: List<AutoInfo>) {
        UniteVpnManager.notifyStatus(VpnStatus.CONNECTING)
        vpnImpl.connect(conn)
    }



    private suspend fun checkType(type: Type) {
        VPNLog.d("UniteVpnStatusService >>> checkType() --> forward type is $type")
        if (this::vpnImpl.isInitialized && vpnImpl.type == type) {
            VPNLog.d("UniteVpnStatusService >>> checkType() --> ${vpnImpl.type} isInitialized")
            /*if (vpnImpl.isActive) {
                VPNLog.d("UniteVpnStatusService >>> checkType() --> ${vpnImpl.type} isActive")
                disconnect()
            }*/
            return
        } else {
            VPNLog.d("UniteVpnStatusService >>> checkType() --> need to create new vpnImpl: $type")
            createVpnImpl(type).join()
        }
        VPNLog.d("UniteVpnStatusService >>> checkType() --> finish")
    }


    private suspend fun createVpnImpl(type: Type) = getDefineJob(Dispatchers.Default) {
        //已实例化的VPNImpl需检查Vpn状态是否需要断开，再销毁
        if (this::vpnImpl.isInitialized) {
            VPNLog.d("UniteVpnStatusService >>> createVpnImpl() --> ${vpnImpl.type} isInitialized")
            if (vpnImpl.isActive) {
                VPNLog.d("UniteVpnStatusService >>> createVpnImpl() --> ${vpnImpl.type} isActive, need to disconnect")
                disconnect().join()
            }
            VPNLog.d("UniteVpnStatusService >>> createVpnImpl() --> ${vpnImpl.type} destroy")
            vpnImpl.onDestroy()
        }
        //创建新的协议并初始化
        vpnImpl = UniteVpnSdk.getVpnImplByType(type)
        vpnImpl.onCreate(this)
        VPNLog.d("UniteVpnStatusService >>> createVpnImpl() --> finally create ${vpnImpl.type}")
    }



    fun notifyStatusChanged(@Status status: Int) {
        VPNLog.d("UniteVpnStatusService >>> notifyStatusChanged() --> status = ${status.statusToString()}")
        if (status == VpnStatus.CONNECT_FAIL && connectList.isNullOrEmpty().not()) {
            getDefineJob(Dispatchers.Default) {
                executeConnect()
            }.start()
            return
        }
        /*if (lastStatus != null && lastStatus == VpnStatus.CONNECT_FAIL && status == VpnStatus.NOT_CONNECTED) {
            lastStatus = status
            return
        }*/
        UniteVpnManager.notifyStatus(status)
        showNotification(status)
        VPNLog.d("UniteVpnStatusService >>> notifyStatusChanged() --> end")
    }

    fun notifyByteCountChanged(speedIn: Long, speedOut: Long, diffIn: Long, diffOut: Long) {
        VPNLog.d("UniteVpnStatusService >>> notifyByteCountChanged() --> speedIn = $speedIn, speedOut = $speedOut, diffIn = $diffIn, diffOut = $diffOut")
        UniteVpnManager.notifyByteCount(speedIn, speedOut, diffIn, diffOut)
        showNotification(VpnStatus.getCurStatus(), speedIn, speedOut, diffIn, diffOut)
    }

    fun adjustConnectListHasNext(): Boolean {
        return connectList.isNullOrEmpty().not()
    }

}