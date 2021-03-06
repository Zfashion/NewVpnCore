package com.core.unitevpn

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.net.VpnService
import android.os.IBinder
import androidx.fragment.app.FragmentActivity
import com.core.unitevpn.base.VpnStatus
import com.core.unitevpn.entity.AutoCombineInfo
import com.core.unitevpn.permission.PermissionFragment.Companion.requestPermission
import com.core.unitevpn.utils.VPNLog


/**
 * 对外提供vpn的连接和断开操作
 */
class UniteVpnInstance(private val context: Context) {

    companion object {
        private const val BIND_TO_CONNECT = "BIND_TO_CONNECT"
        private const val BIND_TO_DISCONNECT = "BIND_TO_DISCONNECT"
    }

    private val serviceConnection: UniteVpnServiceConnection by lazy { UniteVpnServiceConnection() }

    private var binder: UniteVpnStatusService.UniteVpnBinder? = null

    private var autoCombineList: List<AutoCombineInfo>? = null

    private var bindToConnect: Boolean = false
    private var bindToDisConnect: Boolean = false

    /**
     * 执行自动连接
     */
    suspend fun autoConnect(list: List<AutoCombineInfo>) {
        if (checkPermission().not()) return
        if (VpnStatus.isIdle()) {
            autoCombineList = list
            if (checkBind(BIND_TO_CONNECT)) binder?.autoConnect(list)
        }
    }

    /**
     * 断开连接
     */
    suspend fun disconnect() {
        if (VpnStatus.isDisconnecting() || VpnStatus.isIdle()) {
            return
        }
        UniteVpnManager.notifyStatus(VpnStatus.DISCONNECTING)
        if (checkBind(BIND_TO_DISCONNECT)) binder?.disconnect()
    }


    private suspend fun checkBind(order: String): Boolean {
        return if (binder != null) {
            true
        } else {
            if (order == BIND_TO_CONNECT) bindToConnect = true
            else if (order == BIND_TO_DISCONNECT) bindToDisConnect = true
            bindService()
            false
        }
    }


    /**
     * 请求vpn权限
     */
    private suspend fun checkPermission(): Boolean {
        val prepare = VpnService.prepare(context)
        return if (prepare != null) {
            if (context is FragmentActivity) {
                context.requestPermission(prepare)
            } else {
                context.startActivity(prepare)
                false
            }
        } else {
            true
        }
    }

    private suspend fun bindService() {
        UniteVpnStatusService.bindVpnStatusService(context, serviceConnection)
    }

    private suspend fun unBindService() {
    }

    private inner class UniteVpnServiceConnection: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            VPNLog.d("UniteVpnServiceConnection >>> onServiceConnected() --> service bind success!")
            binder = service as UniteVpnStatusService.UniteVpnBinder
            //服务绑定成功，开启前台服务
            UniteVpnStatusService.startForeground(context)
            if (bindToConnect) {
                if (autoCombineList == null) {
                    VPNLog.d("UniteVpnInstance >>> onServiceConnected() --> autoCombineList is null, Please check if method autoConnect() parameter is empty")
                } else {
                    binder?.autoConnect(autoCombineList!!)
                }
                bindToConnect = false
            }
            if (bindToDisConnect) {
                binder?.disconnect()
                bindToDisConnect = false
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            VPNLog.d("UniteVpnServiceConnection >>> onServiceDisconnected() --> service unbind!")
            binder = null
        }

    }

}