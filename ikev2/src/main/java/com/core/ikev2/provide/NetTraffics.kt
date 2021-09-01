package com.core.ikev2.provide

import android.net.TrafficStats
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import java.util.*

class NetTraffics {

    private var preTxBytes: Long = 0
    private var preRxBytes: Long = 0
    private var startTxBytes: Long = 0
    private var startRxBytes: Long = 0
    private var mHandler: Handler? = null
    private var thread: HandlerThread? = null
    private val runTraffics = Runnable { process() }
    private var byteCountListeners: Vector<ByteCountListener>? = null
    private var isRunning = false
    private var isNeedStop = false

    interface ByteCountListener {
        fun updateByteCount(speedIn: Long, speedOut: Long, diffIn: Long, diffOut: Long)
    }

    init {
        thread = HandlerThread(this::class.java.simpleName)
        thread!!.start()
        mHandler = Handler(thread!!.looper)
        byteCountListeners = Vector()
    }

    private fun process() {
        try {
            val rx = TrafficStats.getTotalRxBytes()
            val tx = TrafficStats.getTotalTxBytes()

            // 与第一次计算的流量额（总流量）
            val `in` = rx - startRxBytes
            val out = tx - startTxBytes

            // 与上一次的流量差（网速）
            val diffin = rx - preRxBytes
            val diffout = tx - preTxBytes
            preRxBytes = rx
            preTxBytes = tx
            update(`in`, out, diffin, diffout)
            SystemClock.sleep(2000)
            mHandler?.removeCallbacksAndMessages(null)
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            if (!isNeedStop) {
                mHandler?.post(runTraffics)
            }
        }
    }

    private val getIsRunning: Boolean
        get() = isRunning


    fun start() {
        if (getIsRunning) {
            return
        }
        startRxBytes = TrafficStats.getTotalRxBytes()
        startTxBytes = TrafficStats.getTotalTxBytes()
        preRxBytes = startRxBytes
        preTxBytes = startTxBytes
        isRunning = true
        isNeedStop = false
        mHandler?.post(runTraffics)
    }

    fun stop() {
        if (!getIsRunning) {
            return
        }
        isNeedStop = true
        isRunning = false
        mHandler?.removeCallbacksAndMessages(null)
    }

    private fun update(`in`: Long, out: Long, diffin: Long, diffout: Long) {
        byteCountListeners?.let {
            for (l in it) {
                try {
                    l.updateByteCount(`in`, out, diffin, diffout)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /*fun getTotalTx(): Long {
        return TrafficStats.getTotalTxBytes() - preTxBytes
    }

    fun getTotalRx(): Long {
        return TrafficStats.getTotalRxBytes() - preRxBytes
    }*/

    fun addByteCountListener(listener: ByteCountListener?) {
        synchronized(this) {
            if (listener != null && byteCountListeners?.contains(listener) == false) {
                byteCountListeners?.add(listener)
            }
        }
    }

    fun removeByteByteCountListener(listener: ByteCountListener?) {
        synchronized(this) {
            if (listener != null) {
                byteCountListeners?.remove(listener)
            }
        }
    }

}