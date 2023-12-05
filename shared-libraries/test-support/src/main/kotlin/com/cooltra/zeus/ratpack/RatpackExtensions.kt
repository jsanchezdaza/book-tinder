package com.cooltra.zeus.ratpack

import ratpack.http.client.RequestSpec
import ratpack.test.handling.RequestFixture

fun RequestSpec?.authorizeWith(authorizationToken: String) {
  this
    ?.headers { headers ->
      headers
        .set("Authorization", "Bearer $authorizationToken")
    }
}

fun RequestFixture?.authorizeWith(authorizationToken: String) {
  this?.header("Authorization", "Bearer $authorizationToken")
}

fun RequestFixture?.withUserAgent(userAgent: String) {
  this?.header("User-Agent", userAgent)
}
