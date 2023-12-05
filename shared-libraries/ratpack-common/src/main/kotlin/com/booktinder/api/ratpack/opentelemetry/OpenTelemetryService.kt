package com.booktinder.api.ratpack.opentelemetry

import com.booktinder.api.ratpack.logger
import io.opentelemetry.sdk.trace.SpanProcessor
import ratpack.service.Service
import ratpack.service.StopEvent
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OpenTelemetryService @Inject constructor(
  private val spanProcessor: List<@JvmSuppressWildcards SpanProcessor>,
) : Service {

  private val log by logger()

  override fun onStop(event: StopEvent) {
    log.info("Shutting down Span Processor...")
    spanProcessor.forEach { it.shutdown().join(10, TimeUnit.SECONDS) }
    log.info("Shut down Span Processor")
  }
}
