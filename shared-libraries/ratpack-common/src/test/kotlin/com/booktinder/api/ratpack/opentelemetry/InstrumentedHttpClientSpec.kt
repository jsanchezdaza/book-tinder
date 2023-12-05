package com.booktinder.api.ratpack.opentelemetry

import com.booktinder.api.ratpack.suspendable
import com.booktinder.api.ratpack.yield
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.instrumentation.ratpack.v1_7.RatpackTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.semconv.SemanticAttributes
import ratpack.exec.Promise
import ratpack.guice.Guice
import ratpack.http.client.HttpClient
import ratpack.test.embed.EmbeddedApp
import java.net.URI
import java.time.Duration

class InstrumentedHttpClientSpec : StringSpec(
  {

    val spanExporter = InMemorySpanExporter.create()
    val tracerProvider = SdkTracerProvider.builder()
      .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
      .addSpanProcessor(AddThreadDetailsSpanProcessor())
      .build()

    val openTelemetry = OpenTelemetrySdk.builder()
      .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
      .setTracerProvider(tracerProvider).build()

    val RatpackTelemetry = RatpackTelemetry.create(openTelemetry)

    beforeTest { spanExporter.reset() }

    "propagate trace with http calls" {
      val otherApp = EmbeddedApp.of { spec ->
        spec.registry(
          Guice.registry { bindings ->
            RatpackTelemetry.configureServerRegistry(bindings)
          },
        )

        spec.handlers {
          it.get("foo") { ctx -> ctx.render("bar") }
        }
      }

      val app = EmbeddedApp.of { spec ->
        spec.registry(
          Guice.registry { bindings ->
            RatpackTelemetry.configureServerRegistry(bindings)
            bindings.bindInstance(HttpClient::class.java, RatpackTelemetry.instrumentHttpClient(HttpClient.of { }))
          },
        )

        spec.handlers { chain ->
          chain.get("path-name") { ctx ->
            ctx.get(HttpClient::class.java).get(URI("${otherApp.address}foo"))
              .then {
                ctx.render("hello")
              }
          }
        }
      }

      app.test { httpClient ->
        "hello" shouldBe httpClient.get("path-name").body.text
        Thread.sleep(100L)

        val spanData = spanExporter.finishedSpanItems.first { it.name == "GET /path-name" }
        val spanClientData =
          spanExporter.finishedSpanItems.first { it.name == "GET" && it.kind == SpanKind.CLIENT }
        val spanDataApi = spanExporter.finishedSpanItems.first { it.name == "GET /foo" && it.kind == SpanKind.SERVER }

        spanData.traceId shouldBe spanClientData.traceId
        spanData.traceId shouldBe spanDataApi.traceId

        spanData.kind shouldBe SpanKind.SERVER
        spanClientData.kind shouldBe SpanKind.CLIENT
        val atts = spanClientData.attributes.asMap()
        atts shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/foo")
        atts shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        atts shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)

        val attributes = spanData.attributes.asMap()
        attributes shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/path-name")
        attributes shouldContain Pair(SemanticAttributes.HTTP_TARGET, "/path-name")
        attributes shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        attributes shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)

        val attsApi = spanDataApi.attributes.asMap()
        attsApi shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/foo")
        attsApi shouldContain Pair(SemanticAttributes.HTTP_TARGET, "/foo")
        attsApi shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        attsApi shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)
      }
    }

    "propagate trace with http calls when coroutines handlers" {
      val otherApp = EmbeddedApp.of { spec ->
        spec.registry(
          Guice.registry { bindings ->
            RatpackTelemetry.configureServerRegistry(bindings)
          },
        )

        spec.handlers {
          it.get("foo") { ctx -> suspendable { ctx.render("bar") } }
        }
      }

      val app = EmbeddedApp.of { spec ->
        spec.registry(
          Guice.registry { bindings ->
            RatpackTelemetry.configureServerRegistry(bindings)
            bindings.bindInstance(HttpClient::class.java, RatpackTelemetry.instrumentHttpClient(HttpClient.of { }))
          },
        )

        spec.handlers { chain ->
          chain.get("path-name") { ctx ->
            suspendable {
              val response = ctx.get(HttpClient::class.java).get(URI("${otherApp.address}foo")).yield()
              ctx.render("hello${response.body.text}")
            }
          }
        }
      }

      app.test { httpClient ->
        "hellobar" shouldBe httpClient.get("path-name").body.text
        Thread.sleep(100L)

        val spanData = spanExporter.finishedSpanItems.first { it.name == "GET /path-name" }
        val spanClientData =
          spanExporter.finishedSpanItems.first { it.name == "GET" && it.kind == SpanKind.CLIENT }
        val spanDataApi = spanExporter.finishedSpanItems.first { it.name == "GET /foo" && it.kind == SpanKind.SERVER }

        spanData.traceId shouldBe spanClientData.traceId
        spanData.traceId shouldBe spanDataApi.traceId

        spanData.kind shouldBe SpanKind.SERVER
        spanClientData.kind shouldBe SpanKind.CLIENT
        val atts = spanClientData.attributes.asMap()
        atts shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/foo")
        atts shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        atts shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)

        val attributes = spanData.attributes.asMap()
        attributes shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/path-name")
        attributes shouldContain Pair(SemanticAttributes.HTTP_TARGET, "/path-name")
        attributes shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        attributes shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)

        val attsApi = spanDataApi.attributes.asMap()
        attsApi shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/foo")
        attsApi shouldContain Pair(SemanticAttributes.HTTP_TARGET, "/foo")
        attsApi shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        attsApi shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)
      }
    }

    "add spans for multiple concurrent client calls" {
      val otherApp = EmbeddedApp.of { spec ->
        spec.handlers {
          it.get("foo") { ctx -> ctx.render("bar") }
          it.get("bar") { ctx -> ctx.render("foo") }
        }
      }

      val app = EmbeddedApp.of { spec ->
        spec.registry(
          Guice.registry { bindings ->
            RatpackTelemetry.configureServerRegistry(bindings)
            bindings.bindInstance(HttpClient::class.java, RatpackTelemetry.instrumentHttpClient(HttpClient.of { }))
          },
        )

        spec.handlers { chain ->
          chain.get("path-name") { ctx ->
            val instrumentedHttpClient = ctx.get(HttpClient::class.java)
            instrumentedHttpClient.get(URI("${otherApp.address}foo")).then {}
            instrumentedHttpClient.get(URI("${otherApp.address}bar")).then {}
            ctx.render("hello")
          }
        }
      }

      app.test { httpClient ->
        "hello" shouldBe httpClient.get("path-name").body.text
        Thread.sleep(500)
        spanExporter.finishedSpanItems shouldHaveSize 3
        val spanData = spanExporter.finishedSpanItems.first { spanData -> spanData.name == "GET /path-name" }
        val spanClientData1 = spanExporter.finishedSpanItems.first { s -> s.name == "GET" }
        val spanClientData2 = spanExporter.finishedSpanItems.last { s -> s.name == "GET" }

        spanData.traceId shouldBe spanClientData1.traceId
        spanData.traceId shouldBe spanClientData1.traceId
        spanData.traceId shouldBe spanClientData2.traceId

        spanData.kind shouldBe SpanKind.SERVER

        spanClientData1.kind shouldBe SpanKind.CLIENT
        val atts = spanClientData1.attributes.asMap()
        atts shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/foo")
        atts shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        atts shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)

        spanClientData2.kind shouldBe SpanKind.CLIENT
        val atts2 = spanClientData2.attributes.asMap()
        atts2 shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/bar")
        atts2 shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        atts2 shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)

        val attributes = spanData.attributes.asMap()
        attributes shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/path-name")
        attributes shouldContain Pair(SemanticAttributes.HTTP_TARGET, "/path-name")
        attributes shouldContain Pair(SemanticAttributes.HTTP_METHOD, "GET")
        attributes shouldContain Pair(SemanticAttributes.HTTP_STATUS_CODE, 200L)
      }
    }

    "handling exception errors in http client" {
      val otherApp = EmbeddedApp.of { spec ->
        spec.handlers {
          it.get("foo") { ctx ->
            Promise.value("bar").defer(Duration.ofSeconds(1L))
              .then { ctx.render("bar") }
          }
        }
      }

      val app = EmbeddedApp.of { spec ->
        spec.registry(
          Guice.registry { bindings ->
            RatpackTelemetry.configureServerRegistry(bindings)
            bindings.bindInstance(
              HttpClient::class.java,
              RatpackTelemetry.instrumentHttpClient(
                // create read timeout exception
                HttpClient.of { spec -> spec.readTimeout(Duration.ofMillis(10)) },
              ),
            )
          },
        )

        spec.handlers { chain ->
          chain.get("path-name") { ctx ->
            ctx.get(HttpClient::class.java).get(URI("${otherApp.address}foo"))
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
        atts shouldContain Pair(SemanticAttributes.HTTP_ROUTE, "/foo")
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
