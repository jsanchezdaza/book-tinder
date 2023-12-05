package com.booktinder.api.ratpack.metrics

import com.google.inject.Inject
import io.micrometer.prometheus.PrometheusMeterRegistry
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.handling.RequestOutcome
import java.util.concurrent.TimeUnit

class RequestTimingHandler @Inject constructor(
  private val meterRegistry: PrometheusMeterRegistry,
  private val metricsConfig: MetricsConfig,
) : Handler {

  override fun handle(ctx: Context) {
    ctx.onClose { outcome ->
      val statusCode = outcome.response.status.code.toString()
      meterRegistry.timer(
        "http.requests",
        "status",
        statusCode,
        "path",
        findPathGroup(getPath(ctx, outcome)),
        "method",
        outcome.request.method.name.lowercase(),
      )
        .record(outcome.duration.toNanos(), TimeUnit.NANOSECONDS)
      meterRegistry.timer("http.server.requests", "status", statusCode).record(outcome.duration.toNanos(), TimeUnit.NANOSECONDS)
    }

    ctx.next()
  }

  private fun getPath(ctx: Context, outcome: RequestOutcome) =
    if (metricsConfig.usePathBindings) {
      ctx.pathBinding?.description ?: outcome.request.path
    } else {
      outcome.request.path
    }

  private fun findPathGroup(requestPath: String): String {
    var tagName = if (requestPath == "") "root" else requestPath

    for (it in metricsConfig.groups.entries) {
      val regex = it.value.toRegex()
      val match = regex.find(requestPath)
      if (match != null) {
        tagName = it.key

        if (match.groups.isNotEmpty()) {
          match.groups.forEachIndexed { index, matchGroup ->
            tagName = tagName.replace("$$index", matchGroup?.value!!, false)
          }
        }
        break
      }
    }
    return tagName
  }
}
