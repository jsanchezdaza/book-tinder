package com.cooltra.zeus.ratpack

import com.cooltra.zeus.api.ratpack.opentelemetry.AddThreadDetailsSpanProcessor
import com.google.inject.AbstractModule
import com.google.inject.Provides
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.ReadWriteSpan
import io.opentelemetry.sdk.trace.ReadableSpan
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.semconv.SemanticAttributes
import io.opentelemetry.semconv.SemanticAttributes.DB_STATEMENT
import javax.inject.Singleton

class ImpositionsOpenTelemetry : AbstractModule() {

  @Provides
  @Singleton
  fun spanExporter(): SpanExporter = InMemorySpanExporter.create()

  @Provides
  @Singleton
  @JvmSuppressWildcards
  fun providesBatchProcessor(spanExporter: SpanExporter): List<SpanProcessor> =
    listOf<SpanProcessor>(
      SimpleSpanProcessor.create(spanExporter),
      AddThreadDetailsSpanProcessor(),
      FailBlockComputeThreadSpanProcessor(),
    )
}

class FailBlockComputeThreadSpanProcessor : SpanProcessor {
  override fun onStart(context: Context, span: ReadWriteSpan) {
    span.getAttribute(SemanticAttributes.THREAD_NAME)?.let { threadName ->
      if (span.getAttribute(SemanticAttributes.DB_OPERATION) != null && threadName.contains("compute")) {
        throw IllegalStateException("Blocking compute thread in db operation with query ${span.getAttribute(DB_STATEMENT)} in $span")
      }
    }
  }

  override fun isStartRequired() = true

  override fun onEnd(span: ReadableSpan) {}

  override fun isEndRequired() = false

  override fun shutdown(): CompletableResultCode = CompletableResultCode.ofSuccess()

  override fun forceFlush(): CompletableResultCode = CompletableResultCode.ofSuccess()
}
