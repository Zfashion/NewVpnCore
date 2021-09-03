package com.core.vpnmodule

import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay


/**
 * 点击事件会在主线程中处理
 * 并且会阻止重复点击，避免重复执行
 */
@ObsoleteCoroutinesApi
inline fun View.onClickStart(scope: CoroutineScope, crossinline action: suspend (View) -> Unit) {
    /**
     * 定义协程的一个消费者模式
     */
    val eventActor = scope.actor<View>(Dispatchers.Main.immediate) {
        //这里注意，协程<channel>若没有数据，会处于<挂起>状态。直到有数过来才会执行
        for (event in channel) {
            action.invoke(event)
            //非阻塞延时1000毫秒，才可接收下一个事件
            delay(1000)
        }
    }
    setOnClickListener {
        /**
         * 发送输出,若消费者,没有消费等待数据,发送数据就会失败
         */
        eventActor.trySend(it)
    }
}