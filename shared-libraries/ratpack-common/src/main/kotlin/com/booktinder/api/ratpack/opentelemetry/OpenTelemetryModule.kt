package com.booktinder.api.ratpack.opentelemetry

import com.booktinder.api.ratpack.OpenTelemetryConfig
import com.booktinder.client.RatpackHttpClient
import com.google.inject.AbstractModule
import com.google.inject.Provides
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.context.propagation.TextMapSetter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.instrumentation.ratpack.v1_7.OpenTelemetryServerHandler
import io.opentelemetry.instrumentation.ratpack.v1_7.RatpackTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.semconv.ResourceAttributes
import ratpack.exec.ExecInitializer
import ratpack.exec.ExecInterceptor
import ratpack.http.client.HttpClient
import ratpack.http.client.RequestSpec
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

open class OpenTelemetryModule : AbstractModule() {

  override fun configure() {
    bind(OpenTelemetryService::class.java)
  }

  @Singleton
  @Provides
  fun ratpackTelemetry(openTelemetry: OpenTelemetry): RatpackTelemetry =
    RatpackTelemetry.create(openTelemetry)

  @Singleton
  @Provides
  fun ratpackServerHandler(RatpackTelemetry: RatpackTelemetry): OpenTelemetryServerHandler =
    RatpackTelemetry.openTelemetryServerHandler

  @Singleton
  @Provides
  fun ratpackExecInterceptor(RatpackTelemetry: RatpackTelemetry): ExecInterceptor =
    RatpackTelemetry.openTelemetryExecInterceptor

  @Singleton
  @Provides
  fun ratpackExecInitializer(ratpackTelemetry: RatpackTelemetry): ExecInitializer =
    ratpackTelemetry.openTelemetryExecInitializer

  @Provides
  @Singleton
  fun providesRatpackHttpClient(ratpackTelemetry: RatpackTelemetry): RatpackHttpClient =
    RatpackHttpClient(ratpackTelemetry.instrumentHttpClient(HttpClient.of { }))

  open fun spanExporter(openTelemetryConfig: OpenTelemetryConfig): SpanExporter {
    return OtlpGrpcSpanExporter.builder()
      .setEndpoint(openTelemetryConfig.endpoint)
      .setTimeout(10, TimeUnit.SECONDS)
      .build()
  }

  @Provides
  @Singleton
  fun providesBatchProcessor(openTelemetryConfig: OpenTelemetryConfig): List<SpanProcessor> =
    listOf<SpanProcessor>(
      BatchSpanProcessor.builder(spanExporter(openTelemetryConfig)).build(),
      AddThreadDetailsSpanProcessor(),
    )

  @Provides
  @Singleton
  fun providesOpenTelemetry(
    spanProcessors: List<@JvmSuppressWildcards SpanProcessor>,
    config: OpenTelemetryConfig,
  ): OpenTelemetry {
    val serviceNameResource: Resource =
      Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, config.application))

    val tracerProviderBuilder = SdkTracerProvider.builder()
    spanProcessors.forEach { tracerProviderBuilder.addSpanProcessor(it) }
    val tracerProvider = tracerProviderBuilder
      .setSampler(HoneycombSkipSampler())
      .setResource(Resource.getDefault().merge(serviceNameResource))
      .build()
    return OpenTelemetrySdk.builder()
      .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
      .setTracerProvider(tracerProvider)
      .buildAndRegisterGlobal()
  }
}

object RequestHeaderSetter : TextMapSetter<RequestSpec> {
  override fun set(carrier: RequestSpec?, key: String, value: String) {
    carrier?.let {
      it.headers { h -> h.set(key, value) }
    }
  }
}
