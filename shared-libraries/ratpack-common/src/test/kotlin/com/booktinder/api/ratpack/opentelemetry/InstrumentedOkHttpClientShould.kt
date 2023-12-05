package com.booktinder.api.ratpack.opentelemetry

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTelemetry
import io.opentelemetry.instrumentation.ratpack.v1_7.RatpackTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.semconv.SemanticAttributes
import okhttp3.OkHttpClient
import ratpack.exec.Blocking
import ratpack.exec.Promise
import ratpack.registry.Registry
import ratpack.test.embed.EmbeddedApp
import java.net.URI
import java.time.Duration

class InstrumentedOkHttpClientShould : StringSpec(
  {

    val spanExporter = InMemorySpanExporter.create()
    val tracerProvider = SdkTracerProvider.builder()
      .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
      .build()

    val openTelemetry = OpenTelemetrySdk.builder()
      .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
      .setTracerProvider(tracerProvider).build()

    val RatpackTelemetry = RatpackTelemetry.create(openTelemetry)

    beforeTest { spanExporter.reset() }

    "propagate trace with http calls" {
      val okHttpClient = OkHttpClient.Builder().build()
      val call = OkHttpTelemetry.create(openTelemetry).newCallFactory(okHttpClient)

      val otherApp = EmbeddedApp.of { spec ->
        spec.handlers {
          it.get("foo") { ctx -> ctx.render("bar") }
        }
      }

      val app = EmbeddedApp.of { spec ->
        spec.registry { Registry.of(RatpackTelemetry::configureServerRegistry) }
        spec.handlers { chain ->
          chain.get("path-name") { ctx ->
            val request: okhttp3.Request = okhttp3.Request.Builder()
              .url(URI("${otherApp.address}foo").toURL())
              .build()
            Blocking.get {
              call.newCall(request).execute()
            }.then { ctx.render("hello") }
          }
        }
      }

      app.test { httpClient ->
        "hello" shouldBe httpClient.get("path-name").body.text
        val spanData = spanExporter.finishedSpanItems.first { it.name == "GET /path-name" }
        val spanClientData = spanExporter.finishedSpanItems.first { it.name == "GET" }

        spanData.traceId shouldBe spanClientData.traceId

        spanData.kind shouldBe SpanKind.SERVER
        spanClientData.kind shouldBe SpanKind.CLIENT
        val atts = spanClientData.attributes.asMap()
        atts shouldContain Pair(SemanticAttributes.HTTP_ROUTE, null)
        atts.get(SemanticAttributes.HTTP_URL) as String shouldContain "/foo"
        atts shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        atts shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)

        val attributes = spanData.attributes.asMap()
        attributes shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/path-name")
        attributes shouldContain Pair(SemanticAttributes.HTTP_TARGET, "/path-name")
        attributes shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        attributes shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)
      }
    }

    "propagate trace through multiple client calls" {
      val okHttpClient = OkHttpClient.Builder().build()
      val call = OkHttpTelemetry.create(openTelemetry).newCallFactory(okHttpClient)

      val otherApp = EmbeddedApp.of { spec ->
        spec.handlers {
          it.get("foo") { ctx -> ctx.render("bar") }
          it.get("bar") { ctx -> ctx.render("foo") }
        }
      }

      val app = EmbeddedApp.of { spec ->
        spec.registry { Registry.of(RatpackTelemetry::configureServerRegistry) }
        spec.handlers { chain ->
          chain.get("path-name") { ctx ->

            Blocking.get {
              val request: okhttp3.Request = okhttp3.Request.Builder()
                .url(URI("${otherApp.address}foo").toURL())
                .build()
              call.newCall(request).execute()
              val s = Span.current()
              s.setAttribute("foo", "A")
            }.then { }

            Blocking.get {
              val request: okhttp3.Request = okhttp3.Request.Builder()
                .url(URI("${otherApp.address}bar").toURL())
                .build()
              call.newCall(request).execute()
              val s = Span.current()
              s.setAttribute("bar", "B")
            }.then { }

            ctx.render("hello")
          }
        }
      }

      app.test { httpClient ->
        "hello" shouldBe httpClient.get("path-name").body.text
        Thread.sleep(1000L)

        spanExporter.finishedSpanItems shouldHaveSize 3
        val spanData = spanExporter.finishedSpanItems.first { it.name == "GET /path-name" }
        val (spanClientData1, spanClientData2) = spanExporter.finishedSpanItems.filter { it.name == "GET" }
          .toList()

        spanData.traceId shouldBe spanClientData1.traceId
        spanData.traceId shouldBe spanClientData2.traceId

        spanData.kind shouldBe SpanKind.SERVER

        spanClientData1.kind shouldBe SpanKind.CLIENT
        val atts = spanClientData1.attributes.asMap()
        atts shouldContain Pair(SemanticAttributes.HTTP_ROUTE, null)
        atts.get(SemanticAttributes.HTTP_URL) as String shouldContain "/foo"
        atts shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        atts shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)

        spanClientData2.kind shouldBe SpanKind.CLIENT
        val atts2 = spanClientData2.attributes.asMap()
        atts2 shouldContain Pair(SemanticAttributes.HTTP_ROUTE, null)
        atts2.get(SemanticAttributes.HTTP_URL) as String shouldContain "/bar"
        atts2 shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        atts2 shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)

        val attributes = spanData.attributes.asMap()
        attributes shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/path-name")
        attributes shouldContain Pair(SemanticAttributes.HTTP_TARGET, "/path-name")
        attributes shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        attributes shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)
      }
    }

    "propagate trace through exception errors in http client" {
      val okHttpClient = OkHttpClient.Builder().callTimeout(Duration.ofMillis(500L)).build()
      val call = OkHttpTelemetry.create(openTelemetry).newCallFactory(okHttpClient)

      val otherApp = EmbeddedApp.of { spec ->
        spec.handlers {
          it.get("foo") { ctx ->
            Promise.value("bar").defer(Duration.ofSeconds(1L))
              .then { ctx.render("bar") }
          }
        }
      }

      val app = EmbeddedApp.of { spec ->
        spec.registry { Registry.of(RatpackTelemetry::configureServerRegistry) }
        spec.handlers { chain ->
          chain.get("path-name") { ctx ->
            Blocking.get {
              val request: okhttp3.Request = okhttp3.Request.Builder()
                .url(URI("${otherApp.address}foo").toURL())
                .build()
              call.newCall(request).execute()
            }
              .onError { ctx.render("error") }
              .then { ctx.render("hello") }
          }
        }
      }

      app.test { httpClient ->
        "error" shouldBe httpClient.get("path-name").body.text
        Thread.sleep(100L)
        val spanData = spanExporter.finishedSpanItems.first { it.name == "GET /path-name" }
        val spanClientData = spanExporter.finishedSpanItems.first { it.name == "GET" }

        spanData.traceId shouldBe spanClientData.traceId

        spanData.kind shouldBe SpanKind.SERVER
        spanClientData.kind shouldBe SpanKind.CLIENT
        val atts = spanClientData.attributes.asMap()
        atts shouldContain Pair(SemanticAttributes.HTTP_ROUTE, null)
        atts.get(SemanticAttributes.HTTP_URL) as String shouldContain "/foo"
        atts shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        atts shouldNotContainKey SemanticAttributes.HTTP_STATUS_CODE
        spanClientData.status.statusCode shouldBe StatusCode.ERROR
        spanClientData.events.first().name shouldBe "exception"

        val attributes = spanData.attributes.asMap()
        attributes shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/path-name")
        attributes shouldContain Pair(SemanticAttributes.HTTP_TARGET, "/path-name")
        attributes shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        attributes shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)
      }
    }
  },
)
