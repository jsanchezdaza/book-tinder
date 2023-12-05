package com.booktinder.api.ratpack

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import ratpack.http.Status
import ratpack.test.handling.RequestFixture

class PreFlightRequestHandlerShould : StringSpec({

  val handler = PreFlightRequestHandler()

  "return a 204 with the correct headers when is a preflight request" {
    val result = RequestFixture.handle(handler) {
      it.header("access-control-request-method", "POST")
      it.header("access-control-request-headers", "authorization,content-type")
      it.method("OPTIONS")
    }

    result.status shouldBe Status.NO_CONTENT
    result.headers["access-control-allow-methods"] shouldBe "GET,PUT,POST,DELETE,PATCH"
    result.headers["access-control-allow-headers"] shouldBe "authorization,content-type"
  }

  "do nothing when is not a preflight request" {
    RequestFixture.handle(handler) {
    }.status shouldNotBe Status.NO_CONTENT
  }
},)
