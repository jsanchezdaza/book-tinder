package com.booktinder.api.ratpack.opentelemetry

import com.booktinder.api.ratpack.blocking
import com.booktinder.api.ratpack.suspendable
import com.booktinder.api.ratpack.yield
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.instrumentation.ratpack.v1_7.RatpackTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.semconv.SemanticAttributes
import ratpack.exec.Blocking
import ratpack.exec.Promise
import ratpack.registry.Registry
import ratpack.test.embed.EmbeddedApp

class OpenTelemetryHandlerShould : StringSpec({

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

  "add span at handlers" {
    val app = EmbeddedApp.of { spec ->
      spec.registry { Registry.of(RatpackTelemetry::configureServerRegistry) }
      spec.handlers { chain ->
        chain.get("path-name") { ctx -> ctx.render("hello") }
      }
    }

    app.test { httpClient ->
      "hello" shouldBe httpClient.get("path-name").body.text
      Thread.sleep(1_000)
      val spanData = spanExporter.finishedSpanItems.first { spanData -> spanData.name == "GET /path-name" }
      spanData.kind shouldBe SpanKind.SERVER

      val attributes = spanData.attributes.asMap()
      attributes shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/path-name")
      attributes shouldContain Pair(SemanticAttributes.HTTP_TARGET, "/path-name")
      attributes shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
      attributes shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)
    }
  }

  "propagate trace with instrumented async operations" {
    val app = EmbeddedApp.of { spec ->
      spec.registry { Registry.of(RatpackTelemetry::configureServerRegistry) }
      spec.handlers { chain ->
        chain.get("path-name") { ctx ->
          ctx.render("hello")
          Blocking.op {
            val span = openTelemetry.getTracer("abc").spanBuilder("a-span").startSpan()
            span.makeCurrent().use {
              span.addEvent("something")
              Thread.sleep(200L)
              span.addEvent("after something")
              span.end()
            }
          }.then()
        }
      }
    }

    app.test { httpClient ->
      "hello" shouldBe httpClient.get("path-name").body.text

      Thread.sleep(1_000)
      val spanData = spanExporter.finishedSpanItems.first { it.name == "GET /path-name" }
      val spanDataChild = spanExporter.finishedSpanItems.first { it.name == "a-span" }

      spanData.kind shouldBe SpanKind.SERVER
      spanData.traceId shouldBe spanDataChild.traceId
      spanDataChild.parentSpanId shouldBe spanData.spanId

      val attributes = spanData.attributes.asMap()
      attributes shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/path-name")
      attributes shouldContain Pair(SemanticAttributes.HTTP_TARGET, "/path-name")
      attributes shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
      attributes shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)
    }
  }

  "propagate trace with instrumented async operations in suspendable handlers" {
    val app = EmbeddedApp.of { spec ->
      spec.registry { Registry.of(RatpackTelemetry::configureServerRegistry) }
      spec.handlers { chain ->
        chain.get("path-name") { ctx ->
          suspendable {
            ctx.render("hello")
            Promise.async { downstream ->
              val span = openTelemetry.getTracer("abc").spanBuilder("a-span").startSpan()
              span.makeCurrent().use {
                span.addEvent("something")
                Thread.sleep(200L)
                span.addEvent("after something")
                span.end()
              }
              downstream.success(Unit)
            }.yield()
          }
        }
      }
    }

    app.test { httpClient ->
      "hello" shouldBe httpClient.get("path-name").body.text

      Thread.sleep(1_000)
      val spanData = spanExporter.finishedSpanItems.first { it.name == "GET /path-name" }
      val spanDataChild = spanExporter.finishedSpanItems.first { it.name == "a-span" }

      spanData.kind shouldBe SpanKind.SERVER
      spanData.traceId shouldBe spanDataChild.traceId
      spanDataChild.parentSpanId shouldBe spanData.spanId

      val attributes = spanData.attributes.asMap()
      attributes shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/path-name")
      attributes shouldContain Pair(SemanticAttributes.HTTP_TARGET, "/path-name")
      attributes shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
      attributes shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)
    }
  }

  "propagate trace with instrumented async blocking operations in suspendable handlers" {
    val app = EmbeddedApp.of { spec ->
      spec.registry { Registry.of(RatpackTelemetry::configureServerRegistry) }
      spec.handlers { chain ->
        chain.get("path-name") { ctx ->
          suspendable {
            ctx.render("hello")
            blocking {
              val span = openTelemetry.getTracer("abc").spanBuilder("a-span").startSpan()
              span.makeCurrent().use {
                span.addEvent("something")
                Thread.sleep(200L)
                span.addEvent("after something")
                span.end()
              }
            }
          }
        }
      }
    }

    app.test { httpClient ->
      "hello" shouldBe httpClient.get("path-name").body.text

      Thread.sleep(1_000)
      val spanData = spanExporter.finishedSpanItems.first { it.name == "GET /path-name" }
      val spanDataChild = spanExporter.finishedSpanItems.first { it.name == "a-span" }

      spanData.kind shouldBe SpanKind.SERVER
      spanData.traceId shouldBe spanDataChild.traceId
      spanDataChild.parentSpanId shouldBe spanData.spanId

      val attributes = spanData.attributes.asMap()
      attributes shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/path-name")
      attributes shouldContain Pair(SemanticAttributes.HTTP_TARGET, "/path-name")
      attributes shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
      attributes shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)
    }
  }

  "propagate trace with instrumented async concurrent operations" {
    val app = EmbeddedApp.of { spec ->
      spec.registry { Registry.of(RatpackTelemetry::configureServerRegistry) }
      spec.handlers { chain ->
        chain.get("another-path") { ctx ->
          ctx.render("another")
          Blocking.op {
            val span = openTelemetry.getTracer("abc").spanBuilder("another-span").startSpan()
            span.makeCurrent().use {
              span.addEvent("something")
              Thread.sleep(100L)
              span.addEvent("after something")
              span.end()
            }
          }.then()
        }
        chain.get("path-name") { ctx ->
          ctx.render("hello")
          Blocking.op {
            val span = openTelemetry.getTracer("abc").spanBuilder("a-span").startSpan()
            span.makeCurrent().use {
              span.addEvent("something")
              Thread.sleep(100L)
              span.addEvent("after something")
              span.end()
            }
          }.then()
        }
      }
    }

    app.test { httpClient ->
      "hello" shouldBe httpClient.get("path-name").body.text
      "another" shouldBe httpClient.get("another-path").body.text

      Thread.sleep(1_000)
      val spanData = spanExporter.finishedSpanItems.first { it.name == "GET /path-name" }
      val spanDataChild = spanExporter.finishedSpanItems.first { it.name == "a-span" }

      val spanData2 = spanExporter.finishedSpanItems.first { it.name == "GET /another-path" }
      val spanDataChild2 = spanExporter.finishedSpanItems.first { it.name == "another-span" }

      spanData.kind shouldBe SpanKind.SERVER
      spanData.traceId shouldBe spanDataChild.traceId
      spanDataChild.parentSpanId shouldBe spanData.spanId

      spanData2.kind shouldBe SpanKind.SERVER
      spanData2.traceId shouldBe spanDataChild2.traceId
      spanDataChild2.parentSpanId shouldBe spanData2.spanId

      val attributes = spanData.attributes.asMap()
      attributes shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/path-name")
      attributes shouldContain Pair(SemanticAttributes.HTTP_TARGET, "/path-name")
      attributes shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
      attributes shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)
    }
  }
},)
