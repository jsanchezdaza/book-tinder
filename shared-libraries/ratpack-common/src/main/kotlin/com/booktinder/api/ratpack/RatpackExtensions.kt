package com.booktinder.api.ratpack

import com.fasterxml.jackson.core.JsonParseException
import io.netty.handler.codec.http.HttpHeaderNames.ACCEPT_LANGUAGE
import ratpack.exec.Promise
import ratpack.handling.Context
import ratpack.http.MediaType
import ratpack.http.Status
import ratpack.http.TypedData
import ratpack.http.client.RequestSpec
import java.util.Locale

operator fun <L, R> ratpack.func.Pair<L, R>.component1(): L {
  return left
}

operator fun <L, R> ratpack.func.Pair<L, R>.component2(): R {
  return right
}

@Deprecated("Use Context.parse with kotlin coroutines")
fun <T : Any> Context.tryParse(parseAction: (String) -> T): Promise<T> =
  this.request.body.map { body -> parseBody(body, parseAction) }
    .onError { error -> this.handleInvalidDataError(error) }

suspend fun <T : Any?> Context.parse(parseAction: (String) -> T): T =
  this.request.body.map { body ->
    try {
      parseAction(body.text)
    } catch (error: Exception) {
      when (error) {
        is NullPointerException, is JsonParseException -> throw UnprocessableEntityException(cause = error)
        else -> throw error
      }
    }
  }.yield()

suspend fun <T : Any> Context.parseOrNull(parseAction: (String) -> T?): T? =
  this.request.body.map { body ->
    try {
      parseAction(body.text)
    } catch (error: Exception) {
      null
    }
  }.yield()

fun <T : Any> parseBody(body: TypedData, parseAction: (String) -> T) = try {
  parseAction(body.text)
} catch (e: NullPointerException) {
  throw InvalidDataException("request_missing_fields", "")
} catch (e: JsonParseException) {
  throw InvalidDataException("json_formatting_error", "")
}

fun Context.handleInvalidDataError(error: Throwable) {
  when (error) {
    is InvalidDataException -> {
      error.addEvent()
      this.response.status(Status.UNPROCESSABLE_ENTITY).send(MediaType.APPLICATION_JSON, error.asJson())
    }

    else -> throw error
  }
}

private fun InvalidDataException.addEvent() {
  com.booktinder.api.ratpack.opentelemetry.addEvent(
    "registration_validation_error",
    "registration_validation_error_detail" to "$field $error",
  )
}

data class InvalidDataException(val field: String, val error: String) :
  IllegalArgumentException("Invalid data with details: $field $error")

fun InvalidDataException.asJson() = """{ "error": "data_invalid", "details": { "$field": ["$error"] } }"""

fun Context.getLocale(): Locale {
  val language = this.request.headers[ACCEPT_LANGUAGE] ?: "en"
  return createLocaleFrom(language)
}

fun createLocaleFrom(language: String): Locale =
  when (val iso = language.take(2)) {
    "ca", "es", "fr", "it", "pt" -> Locale.forLanguageTag(iso)
    else -> Locale.ENGLISH
  }

fun String.extractBearerToken() = removePrefix("Bearer ")

fun RequestSpec.authorizeWith(accessToken: String?) = apply {
  if (accessToken != null) this.headers?.add("Authorization", "Bearer $accessToken")
}
