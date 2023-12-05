package com.booktinder.api.ratpack

import io.netty.channel.ConnectTimeoutException
import io.netty.handler.ssl.SslHandshakeTimeoutException
import ratpack.error.ClientErrorHandler
import ratpack.error.ServerErrorHandler
import ratpack.handling.Context
import ratpack.http.MediaType.APPLICATION_JSON
import ratpack.http.Status.BAD_GATEWAY
import ratpack.http.Status.BAD_REQUEST
import ratpack.http.Status.CONFLICT
import ratpack.http.Status.FORBIDDEN
import ratpack.http.Status.GATEWAY_TIMEOUT
import ratpack.http.Status.INTERNAL_SERVER_ERROR
import ratpack.http.Status.NOT_FOUND
import ratpack.http.Status.TOO_MANY_REQUESTS
import ratpack.http.Status.UNAUTHORIZED
import ratpack.http.Status.UNPROCESSABLE_ENTITY
import ratpack.http.Status.UNSUPPORTED_MEDIA_TYPE
import ratpack.http.client.HttpClientReadTimeoutException
import java.net.SocketException

open class ErrorHandler : ClientErrorHandler, ServerErrorHandler {

  override fun error(ctx: Context, throwable: Throwable) {
    when (throwable) {
      is ConnectTimeoutException,
      is SocketException,
      is HttpClientReadTimeoutException,
      is com.booktinder.api.ratpack.ThirdPartyInternalException,
      is SslHandshakeTimeoutException,
      is com.booktinder.api.ratpack.BadGatewayException,
      -> ctx.response.status(BAD_GATEWAY).send()
      is com.booktinder.api.ratpack.ThirdPartyTimeoutException -> ctx.response.status(GATEWAY_TIMEOUT).send()
      is com.booktinder.api.ratpack.NotFoundException ->
        if (throwable.message == null) {
          ctx.response.status(NOT_FOUND).send("")
        } else {
          ctx.response.status(NOT_FOUND).send(throwable.message)
        }
      is com.booktinder.api.ratpack.ResourceNotFoundException ->
        if (throwable.body.isNullOrEmpty()) {
          ctx.response.status(NOT_FOUND).send()
        } else {
          ctx.response.status(NOT_FOUND).send(APPLICATION_JSON, throwable.body)
        }
      is com.booktinder.api.ratpack.BadRequestException -> ctx.response.status(BAD_REQUEST).send()
      is com.booktinder.api.ratpack.UnauthorizedException -> ctx.response.status(UNAUTHORIZED).send()
      is com.booktinder.api.ratpack.ForbiddenRequestException -> ctx.response.status(FORBIDDEN).send(throwable.message)
      is com.booktinder.api.ratpack.ConflictException -> ctx.response.status(CONFLICT).send()
      is com.booktinder.api.ratpack.UnprocessableEntityException ->
        if (throwable.body.isNullOrEmpty()) {
          ctx.response.status(UNPROCESSABLE_ENTITY).send()
        } else {
          ctx.response.status(UNPROCESSABLE_ENTITY).send(APPLICATION_JSON, throwable.body)
        }
      is com.booktinder.api.ratpack.UnsupportedMediaException -> ctx.response.status(UNSUPPORTED_MEDIA_TYPE).send()
      is com.booktinder.api.ratpack.TooManyRequestException -> ctx.response.status(TOO_MANY_REQUESTS).send()
      else -> ctx.response.status(INTERNAL_SERVER_ERROR).send()
    }
  }

  override fun error(ctx: Context, statusCode: Int) {
    ctx.response.status(statusCode).send()
  }
}

/**
 * Only use when need backwards compatible json response, otherwise probably use
 * @see NotFoundException
 */
class ResourceNotFoundException(message: String, cause: Throwable, val body: String? = null) :
  RuntimeException(message, cause)

class UnprocessableEntityException(message: String? = null, cause: Throwable? = null, val body: String? = null) :
  RuntimeException(message, cause)

class NotFoundException : RuntimeException {
  constructor(cause: Throwable) : super(cause)
  constructor(message: String? = null) : super(message)
  constructor(message: String, ex: Throwable) : super(message, ex)
}

class BadRequestException : RuntimeException {
  constructor(cause: Throwable) : super(cause)
  constructor(message: String) : super(message)
  constructor(message: String, ex: Throwable) : super(message, ex)
}

class ForbiddenRequestException : RuntimeException {
  constructor(cause: Throwable) : super(cause)
  constructor(message: String) : super(message)
}

class ThirdPartyInternalException : RuntimeException {
  constructor(cause: Throwable) : super(cause)
  constructor(message: String) : super(message)
}

class BadGatewayException : RuntimeException {
  constructor(cause: Throwable) : super(cause)
  constructor(message: String) : super(message)
}

class ThirdPartyTimeoutException : RuntimeException {
  constructor(cause: Throwable) : super(cause)
  constructor(message: String) : super(message)
}

class UnauthorizedException : RuntimeException {
  constructor(cause: Throwable) : super(cause)
  constructor(message: String) : super(message)
}

class ConflictException : RuntimeException {
  constructor() : super()
  constructor(message: String) : super(message)
  constructor(cause: Throwable) : super(cause)
}

class UnsupportedMediaException : RuntimeException {
  constructor() : super()
  constructor(message: String) : super(message)
  constructor(cause: Throwable) : super(cause)
}

class TooManyRequestException : RuntimeException {
  constructor() : super()
  constructor(message: String) : super(message)
  constructor(cause: Throwable) : super(cause)
}
