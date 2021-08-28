package com.core.unitevpn.common

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


val UniteScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

inline fun doMainJob(crossinline block: suspend () -> Unit) = UniteScope.launch(Dispatchers.Main.immediate) { block.invoke() }

inline fun getDefineJob(context: CoroutineContext, crossinline block: suspend () -> Unit) = UniteScope.launch(context) { block.invoke() }

inline fun <T> getDefineAsync(context: CoroutineContext, crossinline block: suspend () -> T) = UniteScope.async(context) { block.invoke() }

suspend inline fun <T> withInvoke(context: CoroutineContext, crossinline block: suspend () -> T) =
    withContext(UniteScope.coroutineContext + context) { block.invoke() }