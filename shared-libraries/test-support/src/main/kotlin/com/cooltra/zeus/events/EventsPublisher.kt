package com.cooltra.zeus.events

import com.cooltra.zeus.api.ratpack.logger
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.ConnectionFactory
import java.time.Instant
import java.util.Date
import java.util.UUID
import kotlin.text.Charsets.UTF_8

class EventsPublisher(rabbitMQTestConfig: RabbitMQTestConfig) {

  private val log by logger()
  private val factory: ConnectionFactory = ConnectionFactory()

  init {
    factory.username = rabbitMQTestConfig.username
    factory.password = rabbitMQTestConfig.password
    factory.host = rabbitMQTestConfig.host
    factory.port = rabbitMQTestConfig.port
    factory.virtualHost = rabbitMQTestConfig.vhost
  }

  fun send(
    exchange: String,
    type: String,
    message: String,
    messageId: String = UUID.randomUUID().toString(),
    timestamp: Instant? = Instant.now(),
    routingKey: String = exchange,
  ) {
    val properties = AMQP.BasicProperties().builder()
      .type(type)
      .messageId(messageId)
      .timestamp(timestamp?.let { Date.from(it) })
      .build()
    factory.newConnection("$exchange-publisher")
      .use { connection ->
        connection.createChannel().use { channel ->
          channel.basicPublish(exchange, routingKey, properties, message.toByteArray(UTF_8))
          log.info("Message published: $message on exchange $exchange")
        }
      }
  }
}

data class RabbitMQTestConfig(
  val vhost: String,
  val host: String,
  val username: String,
  val password: String,
  val port: Int,
)
