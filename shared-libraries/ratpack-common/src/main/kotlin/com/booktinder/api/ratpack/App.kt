package com.booktinder.api.ratpack

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import ratpack.func.Action
import ratpack.server.RatpackServer
import ratpack.server.RatpackServerSpec
import java.time.Instant
import java.util.TimeZone

interface App {
  val name: String

  fun ratpackStart(definition: Action<RatpackServerSpec>) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val ratpackStart = Instant.now()
    val server = RatpackServer.start(definition)
    val tracer =
      server.registry.get().get(OpenTelemetry::class.java).tracerProvider.tracerBuilder("ratpack-server").build()
    val span = tracer.spanBuilder("ratpack-start")
      .setStartTimestamp(ratpackStart)
      .setAttribute(AttributeKey.stringKey("service"), name)
      .startSpan()
    span.end()
  }
}
