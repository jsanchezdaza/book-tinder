package com.booktinder.api.ratpack.opentelemetry

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.SpanKind.CLIENT
import io.opentelemetry.api.trace.SpanKind.SERVER
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.trace.data.LinkData
import io.opentelemetry.sdk.trace.samplers.Sampler
import io.opentelemetry.sdk.trace.samplers.SamplingDecision
import io.opentelemetry.sdk.trace.samplers.SamplingDecision.DROP
import io.opentelemetry.sdk.trace.samplers.SamplingDecision.RECORD_ONLY
import io.opentelemetry.sdk.trace.samplers.SamplingResult
import io.opentelemetry.semconv.SemanticAttributes.CODE_FUNCTION
import io.opentelemetry.semconv.SemanticAttributes.DB_OPERATION
import io.opentelemetry.semconv.SemanticAttributes.DB_SQL_TABLE
import io.opentelemetry.semconv.SemanticAttributes.HTTP_METHOD
import io.opentelemetry.semconv.SemanticAttributes.HTTP_TARGET

class HoneycombSkipSampler : Sampler {
  override fun shouldSample(
    parentContext: Context,
    traceId: String,
    name: String,
    spanKind: SpanKind,
    attributes: Attributes,
    parentLinks: List<LinkData?>,
  ): SamplingResult {
    if (Span.fromContext(parentContext).spanContext.isValid && !Span.fromContext(parentContext).spanContext.traceFlags.isSampled) {
      return SamplingResult.create(DROP)
    }

    if (skipCDCQuery("payment_events", spanKind, attributes)) return SamplingResult.create(RECORD_ONLY)
    if (skipCDCQuery("rental_events", spanKind, attributes)) return SamplingResult.create(RECORD_ONLY)
    if (skipCDCQuery("vehicle_operational_events", spanKind, attributes)) return SamplingResult.create(RECORD_ONLY)
    if (skipCDCQuery("invoice_events", spanKind, attributes)) return SamplingResult.create(RECORD_ONLY)
    if (skipCDCQuery(
        "vehicle_telematics_status_events",
        spanKind,
        attributes,
      )
    ) {
      return SamplingResult.recordOnly()
    }

    if (spanKind === SpanKind.INTERNAL && attributes.get(CODE_FUNCTION) == "getConnection") {
      return SamplingResult.drop()
    }

    if (traceIsTelematicsData(spanKind, attributes)) {
      return SamplingResult.recordOnly()
    }

    return SamplingResult.create(SamplingDecision.RECORD_AND_SAMPLE)
  }

  private fun traceIsTelematicsData(
    spanKind: SpanKind,
    attributes: Attributes,
  ): Boolean {
    return spanKind === SERVER &&
      attributes.get(HTTP_TARGET)?.matches("/vehicles/.*/telematics-data".toRegex()) ?: false &&
      attributes.get(HTTP_METHOD) == "POST"
  }

  private fun skipCDCQuery(
    name: String,
    spanKind: SpanKind,
    attributes: Attributes,
  ): Boolean {
    return spanKind === CLIENT &&
      attributes.get(DB_SQL_TABLE) == name &&
      attributes.get(DB_OPERATION) == "SELECT"
  }

  override fun getDescription(): String = "PostgresConnectionSampler"
}
