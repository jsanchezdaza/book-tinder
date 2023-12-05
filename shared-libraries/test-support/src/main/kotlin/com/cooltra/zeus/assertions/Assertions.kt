package com.cooltra.zeus.assertions

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import java.util.UUID

fun beValidUUID() = Matcher<String> { value ->
  MatcherResult(
    isValidUUID(value),
    { "string $value should be an uuid" },
    { "string $value should not be an uuid" },
  )
}

fun String.shouldBeValidUUID(): String {
  this should beValidUUID()
  return this
}

private fun isValidUUID(uuid: String): Boolean =
  try {
    UUID.fromString(uuid)
    true
  } catch (exception: IllegalArgumentException) {
    false
  }
