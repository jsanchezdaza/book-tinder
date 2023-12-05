package com.booktinder.api.ratpack.opentelemetry

import io.opentelemetry.context.Context
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.ReadWriteSpan
import io.opentelemetry.sdk.trace.ReadableSpan
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.semconv.SemanticAttributes.THREAD_ID
import io.opentelemetry.semconv.SemanticAttributes.THREAD_NAME

class AddThreadDetailsSpanProcessor : SpanProcessor {
  override fun onStart(context: Context, span: ReadWriteSpan) {
    val currentThread = Thread.currentThread()
    span.setAttribute(THREAD_ID, currentThread.id)
    span.setAttribute(THREAD_NAME, currentThread.name)
  }

  override fun isStartRequired() = true

  override fun onEnd(span: ReadableSpan) {}

  override fun isEndRequired() = false

  override fun shutdown(): CompletableResultCode = CompletableResultCode.ofSuccess()

  override fun forceFlush(): CompletableResultCode = CompletableResultCode.ofSuccess()
}
