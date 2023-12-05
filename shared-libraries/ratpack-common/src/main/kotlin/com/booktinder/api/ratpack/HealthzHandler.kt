package com.booktinder.api.ratpack

import kotlinx.coroutines.delay
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.http.Status
import ratpack.jackson.Jackson
import java.lang.management.ManagementFactory

class HealthzHandler(
  dummy: () -> Unit,
  version: String? = null,
) : Handler {

  private val version: String = version ?: dummy.javaClass.`package`?.implementationVersion ?: "dev"

  override fun handle(ctx: Context) = ctx.render(Jackson.json(Response(uptime(), version)))

  private fun uptime(): Long = ManagementFactory.getRuntimeMXBean().uptime

  private data class Response(val up_since: Long, val version: String)
}

class ConnectionTestHandler : Handler {

  override fun handle(ctx: Context) = suspendable {
    delay(5_000)
    ctx.response.status(Status.OK).send()
  }
}
