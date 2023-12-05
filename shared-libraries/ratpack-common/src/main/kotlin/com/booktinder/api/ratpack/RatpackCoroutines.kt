package com.booktinder.api.ratpack

import io.opentelemetry.context.Context
import io.opentelemetry.context.Scope
import io.opentelemetry.extension.kotlin.asContextElement
import io.opentelemetry.extension.kotlin.getOpenTelemetryContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import ratpack.exec.ExecController
import ratpack.exec.Execution
import ratpack.exec.Promise
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resumeWithException

/**
 * Blocking I/O dispatcher. If inside Ratpack it will return Ratpack blocking IO
 * thread pool, otherwise it will return "standard" coroutines IO dispatcher.
 *
 * @see [ratpack.exec.Blocking]
 */
private val blocking: CoroutineDispatcher
  get() = ExecController.current()
    .map<CoroutineDispatcher> { it.blockingExecutor.asCoroutineDispatcher() }
    .orElse(Dispatchers.IO)

/**
 * Invoke an asynchronous I/O block of code inside a suspend function,
 * i.e. in a coroutine context. The code invoked should be the smallest
 * possible, and include only the I/O invocation, reducing to the minimum
 * the CPU-related code.
 */
suspend fun <T> blocking(block: suspend CoroutineScope.() -> T): T =
  withContext(blocking(), block)

fun blocking(): CoroutineDispatcher = blocking

/**
 * Executes the given block in a coroutine scope. This function can be seen as a bridge between Ratpack and Kotlin coroutines.
 *
 * The implementation ensures to always use the same compute thread during all your execution,
 * honoring one of Ratpack's guarantees. It prevents additional context switches between compute threads, while allowing
 * you to use other suspend functions or blocking {} blocks.
 */
@Suppress("EXPERIMENTAL_API_USAGE")
fun <T> suspendable(spec: suspend CoroutineScope.() -> T) {
  val dispatcher = Execution.current().eventLoop.asCoroutineDispatcher()

  Promise.async { downstream ->
    CoroutineScope(dispatcher + Context.current().asContextElement()).launch(start = CoroutineStart.UNDISPATCHED) {
      try {
        downstream.success(spec(this))
      } catch (t: Throwable) {
        downstream.error(t)
      }
    }
  }.then { }
}

/**
 * Executes the given block in a coroutine scope.
 *
 * The implementation ensures to always use the same coroutine scope
 */
fun <T> ratpack.handling.Context.suspendable(spec: suspend CoroutineScope.() -> T) {
  val scope = maybeGet(CoroutineScope::class.java).orElseGet {
    val dispatcher = Execution.current().eventLoop.asCoroutineDispatcher()
    CoroutineScope(dispatcher)
  }.also { this.request.add(it) }

  val cc = maybeGet(CoroutineContext::class.java).orElseGet { EmptyCoroutineContext }
  Promise.async { downstream ->
    scope.launch(context = cc + Context.current().asContextElement(), start = CoroutineStart.UNDISPATCHED) {
      try {
        downstream.success(spec(this))
      } catch (t: Throwable) {
        downstream.error(t)
      }
    }
  }.then { }
}

/**
 * Gets the value of the given Promise in a coroutine. It is intended to be used when you have a [Promise]
 * in a suspending function. Example: When implementing a grpc service using Kotlin stubs (which uses coroutines)
 * and you need to use Ratpack's HTTP client
 *
 * When using this function, if you need further transformation of the result, use [Promise.map] and return the result
 * you need. For example, this is good:
 *
 * val response = httpClient
 *   .get(url)
 *   .map { it.body.text }
 *   .yield()
 *
 *
 * But the following may cause issues if the internal response buffer is already released (which is most likely to happen):
 *
 * val response = httpClient
 *   .get(url)
 *   .yield()
 *
 * val text = response.body.text //this can throw exception
 *
 */
suspend fun <T> Promise<T>.yield(): T = suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
  val attachPromise = {
    this
      .onError { cont.resumeWithException(it) }
      .then { cont.resume(it, null) }
  }

  if (Execution.currentOpt().isPresent) {
    Execution.current().add(Context::class.java, cont.context.getOpenTelemetryContext())
    attachPromise()
  } else {
    Execution.fork().start {
      val otelCtx = cont.context.getOpenTelemetryContext()
      it.add(Context::class.java, otelCtx)
      val scope = otelCtx.makeCurrent()
      it.add(Scope::class.java, scope)
      attachPromise()
    }
  }
}
