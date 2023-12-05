package com.booktinder.api.ratpack.metrics

import io.micrometer.prometheus.PrometheusMeterRegistry
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.http.Status.OK
import javax.inject.Inject

class MetricsHandler @Inject constructor(private val metricsRegistry: PrometheusMeterRegistry) : Handler {
  override fun handle(ctx: Context) = ctx.response.status(OK).send(metricsRegistry.scrape())
}
