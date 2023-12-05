package com.cooltra.zeus.ratpack

import com.cooltra.zeus.api.ratpack.ErrorHandler
import com.cooltra.zeus.api.ratpack.logger
import ratpack.error.ClientErrorHandler
import ratpack.error.ServerErrorHandler
import ratpack.handling.Context

class AcceptanceErrorHandler : ServerErrorHandler, ClientErrorHandler {
  private val log by logger()
  override fun error(ctx: Context, throwable: Throwable) {
    log.error("Error handler exception", throwable)
    val delegate = ctx.get(ErrorHandler::class.java)
    delegate.error(ctx, throwable)
  }

  override fun error(ctx: Context, statusCode: Int) {
    log.error("Client error found with status code $statusCode")
    val delegate = ctx.get(ErrorHandler::class.java)
    delegate.error(ctx, statusCode)
  }
}
