package com.booktinder.api.ratpack.opentelemetry

import com.booktinder.api.ratpack.suspendable
import com.booktinder.api.ratpack.yield
import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.extension.kotlin.asContextElement
import io.opentelemetry.instrumentation.ratpack.v1_7.RatpackTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ratpack.exec.ExecController
import ratpack.exec.Execution
import ratpack.guice.Guice
import ratpack.http.client.HttpClient
import ratpack.service.Service
import ratpack.service.StartEvent
import ratpack.test.embed.EmbeddedApp
import java.net.URI
import java.util.concurrent.CountDownLatch
import kotlin.time.Duration.Companion.seconds

class OpenTelemetryRatpackServiceShould : StringSpec(
  {

    val spanExporter = InMemorySpanExporter.create()
    val tracerProvider = SdkTracerProvider.builder()
      .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
      .build()

    val openTelemetry = OpenTelemetrySdk.builder()
      .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
      .setTracerProvider(tracerProvider).build()

    val RatpackTelemetry = RatpackTelemetry.create(openTelemetry)

    beforeEach {
      spanExporter.reset()
    }

    "propagate http trace in ratpack service" {
      val latch = CountDownLatch(1)

      val otherApp = EmbeddedApp.of { spec ->
        spec.handlers {
          it.get("foo") { ctx -> ctx.render("bar") }
        }
      }

      val app = EmbeddedApp.of { spec ->
        spec.registry(
          Guice.registry { bindings ->
            RatpackTelemetry.configureServerRegistry(bindings)
            bindings.bindInstance(HttpClient::class.java, RatpackTelemetry.instrumentHttpClient(HttpClient.of { }))
            bindings.bindInstance(BarService(latch, "${otherApp.address}foo", openTelemetry))
          },
        )
        spec.handlers { chain ->
          chain.get("hello") { ctx -> ctx.render("hi") }
        }
      }

      app.address
      app.test { _ ->
        latch.await()
        val spanData = spanExporter.finishedSpanItems.first { it.name == "a-span" }
        val trace = spanExporter.finishedSpanItems.filter { it.traceId == spanData.traceId }

        trace.size shouldBe 3
      }
    }

    "propagate http trace in ratpack coroutine service" {
      val latch = CountDownLatch(1)

      val otherApp = EmbeddedApp.of { spec ->
        spec.handlers {
          it.get("foo") { ctx -> ctx.render("bar") }
        }
      }

      val app = EmbeddedApp.of { spec ->
        spec.registry(
          Guice.registry { bindings ->
            RatpackTelemetry.configureServerRegistry(bindings)
            bindings.bindInstance(HttpClient::class.java, RatpackTelemetry.instrumentHttpClient(HttpClient.of { }))
            bindings.bindInstance(BarCoroutineService(latch, "${otherApp.address}foo", openTelemetry))
          },
        )
        spec.handlers { chain ->
          chain.get("hello") { ctx -> ctx.render("hi") }
        }
      }

      app.address
      app.test { _ ->
        latch.await()
        val spanData = spanExporter.finishedSpanItems.first { it.name == "a-span" }
        val trace = spanExporter.finishedSpanItems.filter { it.traceId == spanData.traceId }

        trace.size shouldBe 3
      }
    }
    "propagate http trace in ratpack service with coroutines" {
      val latch = CountDownLatch(1)

      val otherApp = EmbeddedApp.of { spec ->
        spec.handlers {
          it.get("foo") { ctx -> ctx.render("bar") }
        }
      }

      val app = EmbeddedApp.of { spec ->
        spec.registry(
          Guice.registry { bindings ->
            RatpackTelemetry.configureServerRegistry(bindings)
            bindings.bindInstance(HttpClient::class.java, RatpackTelemetry.instrumentHttpClient(HttpClient.of { }))
            bindings.bindInstance(BarWithCoroutineService(latch, "${otherApp.address}foo", openTelemetry))
          },
        )
        spec.handlers { chain ->
          chain.get("hello") { ctx -> ctx.render("hi") }
        }
      }

      app.address
      app.test { _ -> latch.await() }
      eventually(1.seconds) {
        val spanData = spanExporter.finishedSpanItems.first { it.name == "a-span" }
        val trace = spanExporter.finishedSpanItems.filter { it.traceId == spanData.traceId }

        trace.size shouldBe 3
      }
    }
  },
)

class BarService(
  private val latch: CountDownLatch,
  private val url: String,
  opentelemetry: OpenTelemetry,
) : Service {

  private val tracer = opentelemetry.tracerProvider.tracerBuilder("testing").build()
  override fun onStart(event: StartEvent) {
    val parentContext = Context.current()
    val span = tracer.spanBuilder("a-span")
      .setParent(parentContext)
      .startSpan()

    val otelContext = parentContext.with(span)
    otelContext.makeCurrent().use {
      Execution.current().add(Context::class.java, otelContext)
      val httpClient = event.registry.get(HttpClient::class.java)
      httpClient.get(URI(url))
        .flatMap { httpClient.get(URI(url)) }
        .then {
          span.end()
          latch.countDown()
        }
    }
  }
}

class BarWithCoroutineService(
  private val latch: CountDownLatch,
  val url: String,
  opentelemetry: OpenTelemetry,
) :
  Service {

  val tracer = opentelemetry.tracerProvider.tracerBuilder("testing").build()
  override fun onStart(event: StartEvent) {
    val parentContext = Context.current()
    val span = tracer.spanBuilder("a-span")
      .setParent(parentContext)
      .startSpan()
    CoroutineScope(ExecController.require().blockingExecutor.asCoroutineDispatcher() + parentContext.asContextElement()).launch(
      start = CoroutineStart.UNDISPATCHED,
    ) {
      val otelContext = parentContext.with(span)
      withContext(otelContext.asContextElement()) {
        otelContext.makeCurrent().use {
          val httpClient = event.registry.get(HttpClient::class.java)
          httpClient.get(URI(url)).yield()
          httpClient.get(URI(url)).yield()
          span.end()
          latch.countDown()
        }
      }
    }
  }
}

class BarCoroutineService(private val latch: CountDownLatch, val url: String, val opentelemetry: OpenTelemetry) :
  Service {

  val tracer = opentelemetry.tracerProvider.tracerBuilder("testing").build()
  override fun onStart(event: StartEvent) = suspendable {
    val parentContext = Context.current()
    val span = tracer.spanBuilder("a-span")
      .setParent(parentContext)
      .startSpan()
    val otelContext = parentContext.with(span)
    withContext(otelContext.asContextElement()) {
      otelContext.makeCurrent().use {
        val httpClient = event.registry.get(HttpClient::class.java)
        httpClient.get(URI(url)).yield()
        httpClient.get(URI(url)).yield()
        span.end()
        latch.countDown()
      }
    }
  }
}
