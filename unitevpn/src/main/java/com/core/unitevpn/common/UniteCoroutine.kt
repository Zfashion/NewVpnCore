package com.core.unitevpn.common

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


val UniteScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

@ExperimentalCoroutinesApi
fun getDefineScope(context: CoroutineContext) = UniteScope.newCoroutineContext(context)