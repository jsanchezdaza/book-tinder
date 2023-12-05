package com.booktinder.api.ratpack

import com.fasterxml.jackson.databind.JsonNode

fun JsonNode?.asTextOrNull(): String? = if (this?.asText(null).isNullOrEmpty()) null else this?.asText()

fun JsonNode?.asIntOrNull(): Int? = if (this != null && this.isNull) null else this?.asInt()
fun JsonNode?.asDoubleOrNull(): Double? = if (this != null && this.isNull) null else this?.asDouble()

fun JsonNode?.asLongOrNull(): Long? = if (this != null && this.isNull) null else this?.asLong()

fun JsonNode?.asBooleanOrNull(): Boolean? =
  if (this?.asText().isNullOrEmpty()) null else this?.asText()?.toBooleanStrictOrNull()
