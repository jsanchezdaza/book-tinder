package com.cooltra.zeus.ratpack

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import ratpack.exec.Execution.current
import ratpack.exec.Promise
import ratpack.test.exec.ExecHarness

fun <T> yieldSingle(action: suspend CoroutineScope.() -> T): T {
  return ExecHarness.yieldSingle {
    Promise.async { downstream ->
      CoroutineScope(current().eventLoop.asCoroutineDispatcher()).launch(start = UNDISPATCHED) {
        try {
          downstream.success(action(this))
        } catch (t: Throwable) {
          downstream.error(t)
        }
      }
    }
  }.valueOrThrow
}

fun executeSingle(action: suspend CoroutineScope.() -> Unit) {
  try {
    yieldSingle(action)
  } catch (_: Exception) {
  }
}
