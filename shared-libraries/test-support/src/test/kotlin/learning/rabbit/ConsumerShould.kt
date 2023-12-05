package learning.rabbit

import com.cooltra.zeus.api.ratpack.logger
import com.cooltra.zeus.containers.initRabbitMQTestContainerWithDlq
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.testcontainers.containers.RabbitMQContainer
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Ignored
class ConsumerShould : StringSpec({
  isolationMode = IsolationMode.InstancePerTest

  val queue = "a-queue"
  val deadLetter = "dlq-test"
  lateinit var rabbitMQContainer: RabbitMQContainer
  lateinit var connection: Connection
  lateinit var producer: Producer
  beforeSpec {
    rabbitMQContainer = initRabbitMQTestContainerWithDlq(listOf(queue))
    var factory = ConnectionFactory()
    factory.username = rabbitMQContainer.adminUsername
    factory.password = rabbitMQContainer.adminPassword
    factory.host = rabbitMQContainer.host
    factory.port = rabbitMQContainer.amqpPort
    factory.virtualHost = "/"

    connection = factory.newConnection()

    producer = Producer(
      RabbitMQConfig(
        "/",
        "localhost",
        rabbitMQContainer.adminUsername,
        rabbitMQContainer.adminPassword,
        rabbitMQContainer.amqpPort,
      ),
    )

    val channel = connection.createChannel()
    channel.queuePurge(queue)
    channel.queuePurge(deadLetter)
    channel.close()
  }

  "rabbitMQ consumer with autoAck process message sequential " {
    val numMessages = 5
    val latch = CountDownLatch(numMessages)

    repeat(numMessages) {
      producer.send(queue, "$it")
    }

    val elements = mutableListOf<String>()
    val channel = connection.createChannel()
    channel.basicQos(3)
    channel
      .basicConsume(
        queue,
        true,
        "ReportConsumer",
        { _, m ->
          val message = String(m.body, StandardCharsets.UTF_8)
          elements.add(message)
          println("Read message $message - ${Instant.now()}")
          latch.countDown()
        },
      ) { consumerTag: String -> println("[$consumerTag] was canceled") }

    latch.await(1, TimeUnit.SECONDS)
    channel.close()
    latch.count shouldBe 0
    elements shouldBe listOf("0", "1", "2", "3", "4")
  }

  "rabbitMQ consumer with manual ack process message sequential " {
    val numMessages = 5
    val latch = CountDownLatch(numMessages)

    repeat(numMessages) {
      producer.send(queue, "$it")
    }

    val elements = mutableListOf<String>()
    val channel = connection.createChannel()
    channel.basicQos(3)
    channel
      .basicConsume(
        queue,
        false,
        "ReportConsumer",
        object : DefaultConsumer(channel) {
          override fun handleDelivery(
            consumerTag: String,
            envelope: Envelope,
            properties: AMQP.BasicProperties,
            body: ByteArray,
          ) {
            val message = String(body, StandardCharsets.UTF_8)
            elements.add(message)
            println("Read message $message - ${Instant.now()}")
            latch.countDown()
            channel.basicAck(envelope.deliveryTag, false)
          }
        },
      )

    latch.await(1, TimeUnit.SECONDS)
    channel.close()
    latch.count shouldBe 0
    elements shouldBe listOf("0", "1", "2", "3", "4")
  }

  "rabbitMQ consumer with manual ack process message in parallel but not in order " {
    val numMessages = 5
    val latch = CountDownLatch(numMessages)

    repeat(numMessages) {
      producer.send(queue, "$it")
    }

    val elements = mutableListOf<String>()
    val channel = connection.createChannel()
    channel.basicQos(3)
    channel
      .basicConsume(
        queue,
        false,
        "ReportConsumer",
        object : DefaultConsumer(channel) {
          private val pool = Executors.newCachedThreadPool()
          override fun handleDelivery(
            consumerTag: String,
            envelope: Envelope,
            properties: AMQP.BasicProperties,
            body: ByteArray,
          ) {
            pool.submit {
              val message = String(body, StandardCharsets.UTF_8)
              elements.add(message)
              println("${Thread.currentThread().name} Read message $message - ${Instant.now()}")
              latch.countDown()
              channel.basicAck(envelope.deliveryTag, false)
            }
          }
        },
      )

    latch.await(1, TimeUnit.SECONDS)
    channel.close()
    latch.count shouldBe 0
    elements shouldContainExactlyInAnyOrder listOf("0", "1", "2", "3", "4")
  }

  "consumer with manual ack in parallel prefetch delivers new messages" {
    val numMessages = 5
    val latch = CountDownLatch(numMessages)

    repeat(numMessages) {
      producer.send(queue, "$it")
    }

    val elements = mutableListOf<String>()
    val channel = connection.createChannel()
    channel.basicQos(3)
    channel
      .basicConsume(
        queue,
        false,
        "ReportConsumer",
        object : DefaultConsumer(channel) {
          private val pool = Executors.newCachedThreadPool()
          override fun handleDelivery(
            consumerTag: String,
            envelope: Envelope,
            properties: AMQP.BasicProperties,
            body: ByteArray,
          ) {
            pool.submit {
              val message = String(body, StandardCharsets.UTF_8)
              println("${Thread.currentThread().name} In message $message - ${Instant.now()}")
              if (message.toInt() % 2 != 0) {
                Thread.sleep(200)
              }
              elements.add(message)
              println("${Thread.currentThread().name} Out message $message - ${Instant.now()}")
              latch.countDown()
              channel.basicAck(envelope.deliveryTag, false)
            }
          }
        },
      )

    latch.await(5, TimeUnit.SECONDS)
    channel.close()
    latch.count shouldBe 0
    elements shouldContainExactlyInAnyOrder listOf("0", "1", "2", "3", "4")
  }

  "consumer with manual ack in coroutines parallel prefetch delivers new messages" {
    val numMessages = 5
    val latch = CountDownLatch(numMessages)

    repeat(numMessages) {
      producer.send(queue, "$it")
    }

    val elements = mutableListOf<String>()
    val channel = connection.createChannel()
    channel.basicQos(3)
    channel
      .basicConsume(
        queue,
        false,
        "ReportConsumer",
        object : DefaultConsumer(channel) {
          override fun handleDelivery(
            consumerTag: String,
            envelope: Envelope,
            properties: AMQP.BasicProperties,
            body: ByteArray,
          ) {
            CoroutineScope(Dispatchers.IO).launch {
              val message = String(body, StandardCharsets.UTF_8)
              println("${Thread.currentThread().name} In message $message - ${Instant.now()}")
              if (message.toInt() % 2 != 0) {
                delay(200)
              }
              elements.add(message)
              println("${Thread.currentThread().name} Read message $message - ${Instant.now()}")
              channel.basicAck(envelope.deliveryTag, false)
              latch.countDown()
            }
          }
        },
      )

    latch.await(5, TimeUnit.SECONDS)
    channel.close()
    latch.count shouldBe 0
    elements shouldContainExactlyInAnyOrder listOf("0", "1", "2", "3", "4")
  }

  "rabbitMQ consumer with nack will requeue failed ack with limit or send them to dlq" {
    val numMessages = 5
    val latch = CountDownLatch(numMessages + 1)
    repeat(numMessages) {
      producer.send(queue, "$it")
    }

    val elements = mutableListOf<String>()
    val channel = connection.createChannel()
    channel.basicQos(3)
    channel
      .basicConsume(
        queue,
        false,
        "ReportConsumer",
        object : DefaultConsumer(channel) {
          override fun handleDelivery(
            consumerTag: String,
            envelope: Envelope,
            properties: AMQP.BasicProperties,
            body: ByteArray,
          ) {
            val message = String(body, StandardCharsets.UTF_8)
            println("In message $message - ${Instant.now()}")
            elements.add(message)
            latch.countDown()
            if (message.toInt() == 1) {
              channel.basicNack(envelope.deliveryTag, false, true)
            } else {
              channel.basicAck(envelope.deliveryTag, false)
            }
          }
        },
      )

    val dqlLatch = CountDownLatch(1)
    val dlqElements = mutableListOf<String>()
    channel
      .basicConsume(
        deadLetter,
        false,
        "ReportConsumerDlq",
        object : DefaultConsumer(channel) {
          override fun handleDelivery(
            consumerTag: String,
            envelope: Envelope,
            properties: AMQP.BasicProperties,
            body: ByteArray,
          ) {
            val message = String(body, StandardCharsets.UTF_8)
            dlqElements.add(message)
            println("Retried message $message - ${Instant.now()} - ack - $envelope - $properties")
            dqlLatch.countDown()
            channel.basicAck(envelope.deliveryTag, false)
          }
        },
      )

    latch.await(1, TimeUnit.SECONDS)
    dqlLatch.await(1, TimeUnit.SECONDS)
    channel.close()
    latch.count shouldBe 0
    elements shouldContainExactlyInAnyOrder listOf("0", "1", "2", "3", "4", "1", "1")
    dlqElements shouldContain "1"
  }
},)

data class RabbitMQConfig(
  val vhost: String,
  val host: String,
  val username: String,
  val password: String,
  val port: Int,
)

class Producer(config: RabbitMQConfig) {

  private val log by logger()
  private val factory: ConnectionFactory = ConnectionFactory()

  init {
    factory.username = config.username
    factory.password = config.password
    factory.host = config.host
    factory.port = config.port
    factory.virtualHost = config.vhost
  }

  fun send(queue: String, message: String) {
    factory.newConnection("test-acceptance")
      .use { connection ->
        connection.createChannel().use { channel ->
          channel.basicPublish(
            "",
            queue,
            null,
            message.toByteArray(Charsets.UTF_8),
          )
          log.info(" [x] Sent '$message'")
        }
      }
  }
}
