package com.booktinder.api.ratpack

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.concurrent.shouldCompleteWithin
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldInclude
import io.kotest.matchers.string.shouldStartWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ratpack.error.ServerErrorHandler
import ratpack.exec.ExecController
import ratpack.exec.Execution
import ratpack.exec.Promise
import ratpack.guice.Guice
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.http.Status
import ratpack.registry.Registry
import ratpack.service.Service
import ratpack.service.StartEvent
import ratpack.test.embed.EmbeddedApp
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

object UsedThreads {
  val values: MutableList<Thread> = mutableListOf()

  fun addCurrent() = values.add(Thread.currentThread())

  fun clear() = values.clear()
}

suspend fun aBlockingOp() = blocking {
  UsedThreads.addCurrent()
  usingAnotherDispatcher()
  // should return to a blocking thread
  UsedThreads.addCurrent()
  "blocking io"
}

suspend fun usingAnotherDispatcher() = withContext(Dispatchers.IO) { UsedThreads.addCurrent() }

class IntermediateSuspendableShould : StringSpec({
  beforeTest { UsedThreads.clear() }

  "use intermediate handlers with coroutine next" {
    val app = EmbeddedApp.of { spec ->
      spec.registry(Registry.of { it.add(IntermediateCoroutineHandler()) })
      spec.handlers { chain ->
        chain.all(IntermediateCoroutineHandler::class.java)
        chain.get { ctx ->
          UsedThreads.addCurrent()
          ctx.suspendable {
            UsedThreads.addCurrent()
            val foo = checkNotNull(coroutineContext[IntermediateCoroutineHandler.FooContextElement]) {
              "could not find foo in coroutine context"
            }
            ctx.response.send(foo.value.name)
          }
        }
      }
    }

    app.test {
      val response = it.get()
      response.status shouldBe Status.OK
      response.body.text shouldBe "foo"
    }

    UsedThreads.values[0].name shouldStartWith "ratpack-compute"
    UsedThreads.values[1].name shouldStartWith "ratpack-compute"
  }
},)

class SuspendableShould : StringSpec({

  beforeTest { UsedThreads.clear() }

  beforeTest { UsedThreads.clear() }
  "always use the same compute thread after switching to other pools" {
    val app = EmbeddedApp.of { spec ->
      spec.handlers { chain ->
        chain.get {
          UsedThreads.addCurrent()
          suspendable {
            UsedThreads.addCurrent()
            val io = aBlockingOp()
            // compute thread
            UsedThreads.addCurrent()
            delay(10)
            // compute thread
            UsedThreads.addCurrent()
            it.response.send(io)
          }
        }
      }
    }

    app.test {
      val response = it.get()
      response.status shouldBe Status.OK
      response.body.text shouldBe "blocking io"
    }

    UsedThreads.values[0].name shouldStartWith "ratpack-compute"
    UsedThreads.values[1].name shouldStartWith "ratpack-compute"
    UsedThreads.values[2].name shouldStartWith "ratpack-blocking"
    UsedThreads.values[3].name shouldStartWith "DefaultDispatcher-worker"
    UsedThreads.values[4].name shouldStartWith "ratpack-blocking"
    UsedThreads.values[5].name shouldStartWith "ratpack-compute"
    UsedThreads.values[6].name shouldStartWith "ratpack-compute"
  }

  "exception in coroutine is propagated to ratpack Error Handler" {
    val app = EmbeddedApp.of { spec ->
      spec.registry(
        Guice.registry { bindings ->
          bindings.bind(DummyErrorHandler::class.java)
        },
      )
      spec.handlers { chain ->
        chain.get {
          suspendable {
            val a = Promise.value("ok").yield()
            Promise.error<IllegalArgumentException>(IllegalArgumentException()).yield()
            it.response.send(a)
          }
        }
      }
    }

    app.test { it.get().status shouldBe Status.BAD_REQUEST }
  }
},)

class PromiseYieldShould : StringSpec({
  beforeTest { UsedThreads.clear() }

  "a handler can get the result from a promise in a coroutine scope" {
    val app = EmbeddedApp.of { spec ->
      spec.handlers { chain ->
        chain.get { suspendable { it.response.send(barYield()) } }
      }
    }

    app.test { it.text shouldBe "bar" }
  }

  "a handler can get the result from a promise composition in a coroutine scope" {
    val app = EmbeddedApp.of { spec ->
      spec.handlers { chain ->
        chain.get {
          suspendable {
            val value = Promise.value("foo")
              .flatMap { Promise.value("${it}bar") }
              .map { "${it}baz" }.yield()
            it.response.send(value)
          }
        }
      }
    }

    app.test { it.text shouldBe "foobarbaz" }
  }

  "a handler can get the result from composing multiple promises in a coroutine scope" {
    val app = EmbeddedApp.of { spec ->
      spec.handlers { chain ->
        chain.get {
          suspendable {
            val a = Promise.value("foo").yield()
            val b = Promise.value("bar").yield()
            it.response.send("$a$b")
          }
        }
      }
    }

    app.test { it.text shouldBe "foobar" }
  }

  "a handler with chain of handlers cannot handle different coroutines" {
    val app = EmbeddedApp.of { spec ->
      spec.handlers { chain ->
        chain.get {
          suspendable {
            val value = Promise.value("foo").yield()
            it.request.add(value)
            it.next()
          }
        }.get {
          suspendable {
            val previous = it.get(String::class.java)
            val another = Promise.value("bar").yield()
            it.response.send("$previous$another")
          }
        }
      }
    }

    app.test { it.text shouldInclude "IllegalStateException" }
  }

  "a handler with chain of handlers with different promises" {
    val app = EmbeddedApp.of { spec ->
      spec.handlers { chain ->
        chain.get { ctx ->
          Promise.value("foo").then {
            ctx.request.add(it)
            ctx.next()
          }
        }.get { ctx ->
          val previous = ctx.get(String::class.java)
          Promise.value("bar").then { another ->
            ctx.response.send("$previous$another")
          }
        }
      }
    }

    app.test { it.text shouldBe "foobar" }
  }

  "a handler with chain of handlers with coroutines and promises" {
    val app = EmbeddedApp.of { spec ->
      spec.handlers { chain ->
        chain.get { ctx ->
          Promise.value("foo").then {
            ctx.request.add(it)
            ctx.next()
          }
        }.get { ctx ->
          suspendable {
            val previous = ctx.get(String::class.java)
            val another = Promise.value("bar").yield()
            ctx.response.send("$previous$another")
          }
        }
      }
    }

    app.test { it.text shouldBe "foobar" }
  }

  "a handler that throws an exception should behave accordingly in a coroutine scope" {
    val app = EmbeddedApp.of { spec ->
      spec.handlers { chain ->
        chain.get { suspendable { failureYield() } }
      }
    }

    app.test { it.text shouldInclude "NullPointerException" }
  }

  @Suppress("BlockingMethodInNonBlockingContext")
  "a service can get the result from a promise in a coroutine scope" {
    val latch = CountDownLatch(1)

    val app = EmbeddedApp.of { spec ->
      spec.registryOf { r -> r.add(BarService(latch)) }
        .handlers { chain -> chain.get { ctx -> ctx.response.send("foo") } }
    }

    app.address

    shouldCompleteWithin(5, TimeUnit.SECONDS) { latch.await() }
  }

  "a service can get the result from a promise in a coroutine scope from ratpack blocking executor" {
    val latch = CountDownLatch(1)

    val app = EmbeddedApp.of { spec ->
      spec.registryOf { r -> r.add(BlockingAsyncService(latch)) }
        .handlers { chain -> chain.get { ctx -> ctx.response.send("foo") } }
    }

    app.address

    shouldCompleteWithin(5, TimeUnit.SECONDS) { latch.await() }
    UsedThreads.values[0].name shouldStartWith "ratpack-compute"
    UsedThreads.values[1].name shouldStartWith "ratpack-blocking"
  }

  "a service with fork executor can get the result from a promise in a coroutine scope" {
    val latch = CountDownLatch(1)

    val app = EmbeddedApp.of { spec ->
      spec.registryOf { r -> r.add(ForkService(latch)) }
        .handlers { chain -> chain.get { ctx -> ctx.response.send("foo") } }
    }

    app.address

    shouldCompleteWithin(5, TimeUnit.SECONDS) { latch.await() }
    UsedThreads.values[0].name shouldStartWith "ratpack-compute"
    UsedThreads.values[1].name shouldStartWith "ratpack-blocking"
  }
},)

class BarService(private val latch: CountDownLatch) : Service {

  override fun onStart(event: StartEvent) = suspendable {
    barYield() shouldBe "bar"
    latch.countDown()
  }
}

class BlockingAsyncService(private val latch: CountDownLatch) : Service {

  override fun onStart(event: StartEvent) {
    val dispatcher = ExecController.require().blockingExecutor.asCoroutineDispatcher()
    UsedThreads.addCurrent()
    CoroutineScope(dispatcher).launch {
      UsedThreads.addCurrent()
      barYield() shouldBe "bar"
      latch.countDown()
    }
  }
}

class ForkService(private val latch: CountDownLatch) : Service {

  override fun onStart(event: StartEvent) {
    Execution.fork().start {
      val dispatcher = it.controller.blockingExecutor.asCoroutineDispatcher()
      UsedThreads.addCurrent()
      CoroutineScope(dispatcher).launch {
        UsedThreads.addCurrent()
        barYield() shouldBe "bar"
        latch.countDown()
      }
    }
  }
}

class DummyErrorHandler : ServerErrorHandler {
  override fun error(ctx: Context, throwable: Throwable) {
    when (throwable) {
      is IllegalArgumentException -> ctx.response.status(Status.BAD_REQUEST).send()
      else -> ctx.response.status(Status.INTERNAL_SERVER_ERROR).send()
    }
  }
}

suspend fun barYield(): String = Promise.value("bar").yield()

suspend fun failureYield(): Throwable = Promise.error<Throwable?>(NullPointerException()).yield()

class IntermediateCoroutineHandler : Handler {
  override fun handle(context: Context) = context.suspendable {
    val foo = Foo("foo")
    context.request.add(foo.asContextElement())
    context.next()
  }

  data class Foo(val name: String)

  private fun Foo.asContextElement(): CoroutineContext {
    return FooContextElement(this)
  }

  class FooContextElement(val value: Foo) : AbstractCoroutineContextElement(FooContextElement) {
    companion object Key : CoroutineContext.Key<FooContextElement>
  }
}
