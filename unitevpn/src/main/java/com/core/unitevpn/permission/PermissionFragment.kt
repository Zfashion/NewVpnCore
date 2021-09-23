package com.core.unitevpn.permission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


/**
 * 参考类似RxPermission的做法，将权限申请都放进Fragment，请求结果告知外部的调用者，类似于一个桥梁
 */
class PermissionFragment: Fragment() {

    companion object {

        suspend inline fun <T: FragmentActivity> T.requestPermission(intent: Intent?) = suspendCancellableCoroutine<Boolean> {
            if (intent != null) {
                newInstance(intent, this, object : PermissionCallback{
                    override fun onResult(hasPermission: Boolean) {
                        it.resume(hasPermission)
                    }
                })
            } else {
                it.resume(true)
            }
        }

        fun newInstance(intent: Intent?, activity: FragmentActivity, callback: PermissionCallback) {
            val permissionFragment = PermissionFragment()
            permissionFragment.setPermissionCallback(callback)
            permissionFragment.requestIntent = intent

            activity.supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, permissionFragment)
                .commitAllowingStateLoss()
        }
    }

    private var requestIntent: Intent? = null

    private val vpnPermissionResultLauncher = registerForActivityResult(VpnPermissionResultContract()) {}



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission()
    }

    private fun requestPermission() {
        if (requestIntent != null) vpnPermissionResultLauncher.launch(Unit)
        else handlePermissionResult(true)
    }

    private fun handlePermissionResult(b: Boolean) {
        permissionCallback?.onResult(b)
        dismiss()
    }

    private fun dismiss() {
        parentFragmentManager.beginTransaction()
            .remove(this)
            .commitAllowingStateLoss()
    }


    interface PermissionCallback {
        fun onResult(hasPermission: Boolean)
    }

    private var permissionCallback: PermissionCallback? = null

    private fun setPermissionCallback(callback: PermissionCallback) { permissionCallback = callback }




    inner class VpnPermissionResultContract : ActivityResultContract<Unit, Unit>() {
        override fun createIntent(context: Context, input: Unit?): Intent {
            return requestIntent!!
        }

        override fun parseResult(resultCode: Int, intent: Intent?) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                handlePermissionResult(true)
            } else {
                handlePermissionResult(false)
            }
        }
    }

}