package com.booktinder.api.ratpack.opentelemetry.rabbitmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.Envelope
import com.rabbitmq.client.ShutdownSignalException
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.SpanKind.CONSUMER
import io.opentelemetry.api.trace.StatusCode.ERROR
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import io.opentelemetry.semconv.SemanticAttributes.MESSAGING_DESTINATION_KIND
import io.opentelemetry.semconv.SemanticAttributes.MESSAGING_DESTINATION_NAME
import io.opentelemetry.semconv.SemanticAttributes.MESSAGING_MESSAGE_ID
import io.opentelemetry.semconv.SemanticAttributes.MESSAGING_RABBITMQ_DESTINATION_ROUTING_KEY
import io.opentelemetry.semconv.SemanticAttributes.MESSAGING_SYSTEM
import io.opentelemetry.semconv.SemanticAttributes.MessagingOperationValues.PROCESS

object AMQPBasicPropertiesGetter : TextMapGetter<AMQP.BasicProperties> {
  override fun keys(carrier: AMQP.BasicProperties): MutableIterable<String> {
    return carrier.headers.keys
  }

  override fun get(carrier: AMQP.BasicProperties?, key: String): String? {
    return carrier?.headers?.get(key)?.toString()
  }
}

open class InstrumentedConsumer(private val tracer: Tracer, private val queue: String, private val delegate: Consumer) :
  Consumer {
  override fun handleConsumeOk(consumerTag: String) {
    delegate.handleConsumeOk(consumerTag)
  }

  override fun handleCancelOk(consumerTag: String) {
    delegate.handleCancelOk(consumerTag)
  }

  override fun handleCancel(consumerTag: String) {
    delegate.handleCancel(consumerTag)
  }

  override fun handleShutdownSignal(consumerTag: String, sig: ShutdownSignalException) {
    delegate.handleShutdownSignal(consumerTag, sig)
  }

  override fun handleRecoverOk(consumerTag: String) {
    delegate.handleRecoverOk(consumerTag)
  }

  override fun handleDelivery(
    consumerTag: String,
    envelope: Envelope,
    properties: AMQP.BasicProperties,
    body: ByteArray,

  ) {
    val parentContext = GlobalOpenTelemetry.getPropagators().textMapPropagator.extract(Context.current(), properties, AMQPBasicPropertiesGetter)
    val span = tracer.spanBuilder("$queue $PROCESS")
      .setSpanKind(CONSUMER)
      .setAttribute(MESSAGING_SYSTEM, "rabbitmq")
      .setAttribute(MESSAGING_DESTINATION_NAME, queue)
      .setAttribute(MESSAGING_DESTINATION_KIND, PROCESS)
      .setAttribute(MESSAGING_MESSAGE_ID, properties.messageId)
      .setAttribute(MESSAGING_RABBITMQ_DESTINATION_ROUTING_KEY, envelope.routingKey)
      .setAttribute("event.type", properties.type)
      .setParent(parentContext)
      .startSpan()

    try {
      parentContext.with(span).makeCurrent().use {
        delegate.handleDelivery(consumerTag, envelope, properties, body)
      }
    } catch (ex: Throwable) {
      span.recordException(ex)
      span.setStatus(ERROR)
      throw ex
    } finally {
      span.end()
    }
  }
}

fun OpenTelemetry.rabbitTracer(): Tracer {
  return this.tracerProvider
    .tracerBuilder("rabbitmq-custom")
    .setInstrumentationVersion("0.1").build()
}
