package com.booktinder.extensions

import com.fasterxml.jackson.databind.node.ObjectNode
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.UUID

fun String.toUUID(): UUID = UUID.fromString(this)

fun String.toUUIDOrNull(): UUID? = try {
  UUID.fromString(this)
} catch (e: IllegalArgumentException) {
  null
}

fun String.toInstant(): Instant = Instant.parse(this)

fun String.toInstantOrNull(): Instant? = try {
  Instant.parse(this)
} catch (e: DateTimeParseException) {
  null
}

fun ObjectNode.putOrNull(fieldName: String, field: String?) {
  if (field == null) {
    this.putNull(fieldName)
  } else {
    this.put(fieldName, field)
  }
}
