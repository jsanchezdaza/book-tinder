package com.booktinder.api.ratpack

import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.http.Request
import ratpack.http.Status

class PreFlightRequestHandler : Handler {
  override fun handle(ctx: Context) {
    if (ctx.request.isPreFlight()) {
      ctx.response.headers.set("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,PATCH")
      ctx.request.headers["Access-Control-Request-Headers"]?.let {
        ctx.response.headers.set(
          "Access-Control-Allow-Headers",
          it,
        )
      }
      ctx.response.status(Status.NO_CONTENT).send()
    } else {
      ctx.next()
    }
  }

  private fun Request.isPreFlight(): Boolean =
    this.method.isOptions && this.headers.contains("Access-Control-Request-Method")
}
