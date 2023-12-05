package com.booktinder.api.ratpack.opentelemetry

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context

fun addEvent(name: String, vararg attributes: Pair<String, String>) {
  Span.current()?.addEvent(
    name,
    attributes.fold(Attributes.builder()) { attributesBuilder, (key, value) ->
      attributesBuilder.put(
        key,
        value,
      )
    }.build(),
  )
}

fun addAttributes(vararg attributes: Pair<String, String>) {
  val span = Span.current()
  attributes.forEach { (key, value) -> span.setAttribute(key, value) }
}

fun spanError(ex: Throwable) {
  val span = Span.current()
  span?.makeCurrent().use {
    span.recordException(ex)
    span.setStatus(StatusCode.ERROR)
  }
}

fun instrument(tracer: Tracer, spanName: String, block: () -> Unit) {
  val parentContext = Context.current()
  val span = tracer.spanBuilder(spanName)
    .setParent(parentContext)
    .startSpan()

  try {
    parentContext.with(span).makeCurrent().use {
      block()
    }
  } catch (ex: Throwable) {
    span.recordException(ex)
    span.setStatus(StatusCode.ERROR)
    throw ex
  } finally {
    span.end()
  }
}
