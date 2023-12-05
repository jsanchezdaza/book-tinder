package com.booktinder.api.ratpack.opentelemetry

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.data.EventData
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor

class OpenTelemetryExtensionsTest : StringSpec({

  val spanExporter = InMemorySpanExporter.create()
  val tracerProvider = SdkTracerProvider.builder()
    .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
    .build()

  val openTelemetry = OpenTelemetrySdk.builder()
    .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
    .setTracerProvider(tracerProvider).build()

  beforeEach {
    spanExporter.reset()
  }

  "add event to current span" {
    val span = openTelemetry.getTracer("abc").spanBuilder("a-span").startSpan()
    span.makeCurrent()
    val firstAttribute = "key1" to "value1"
    val secondAttribute = "key2" to "value2"

    addEvent("event", firstAttribute, secondAttribute)
    span.end()

    val finishedSpan = spanExporter.finishedSpanItems.first()
    val event = finishedSpan.events.first()
    event.findValue(firstAttribute) shouldBe firstAttribute.second
    event.findValue(secondAttribute) shouldBe secondAttribute.second
  }
},)

private fun EventData.findValue(attribute: Pair<String, String>): String? =
  attributes.asMap().entries.find { it.key.key == attribute.first }?.value as String?
