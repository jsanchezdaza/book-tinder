package com.cooltra.zeus.containers

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgisContainerProvider
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.S3
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.utility.DockerImageName

@Suppress("UnstableApiUsage")
fun initFirebaseTestContainer(): GenericContainer<Nothing> =
  GenericContainer<Nothing>(
    ImageFromDockerfile()
      .withFileFromClasspath("Dockerfile", "testcontainers/firebase/Dockerfile")
      .withFileFromClasspath("firebase.json", "testcontainers/firebase/firebase.json"),
  ).apply {
    withExposedPorts(9099)
    withReuse(true)
    start()
  }

fun initPostgresTestContainer(databaseName: String): PostgreSQLContainer<*> =
  createPostgresTestContainer(databaseName).apply { start() }

fun initPostgisTestContainer(databaseName: String): PostgreSQLContainer<*> =
  createPostgisTestContainer(databaseName).apply { start() }

fun initPostgresTestContainer(databaseName: String, initScript: String): PostgreSQLContainer<*> =
  createPostgresTestContainer(databaseName).apply { withInitScript(initScript).start() }

fun initLocalStackS3(): LocalStackContainer = LocalStackContainer(DockerImageName.parse("localstack/localstack:2.1.0"))
  .withServices(S3)
  .withReuse(true)
  .apply { start() }

const val POSTGRES_VERSION = "postgres:14.7-alpine"
fun createPostgresTestContainer(databaseName: String, username: String = "zeus"): PostgreSQLContainer<*> =
  PostgreSQLContainer(POSTGRES_VERSION).apply {
    withDatabaseName(databaseName)
      .withUsername(username)
      .withPassword("password")
      .withReuse(true)
  }

fun createPostgisTestContainer(databaseName: String, username: String = "zeus"): PostgreSQLContainer<*> {
  return PostgisContainerProvider().newInstance("14-3.3-alpine").apply {
    withDatabaseName(databaseName)
      .withUsername(username)
      .withPassword("password")
      .withReuse(true)
  } as PostgreSQLContainer<*>
}

fun createPostgres(databaseName: String, username: String = "zeus"): PostgreSQLContainer<*> {
  val image = DockerImageName.parse("alexfdz/postgres:14.7-alpine-postgis-partman")
    .asCompatibleSubstituteFor("postgres")
  return PostgreSQLContainer(image)
    .apply {
      withDatabaseName(databaseName)
        .withUsername(username)
        .withPassword("password")
        .withReuse(true)
    }
}

fun createRabbitMQTestContainer(): RabbitMQContainer =
  RabbitMQContainer(DockerImageName.parse("rabbitmq").withTag("3.9.21-management-alpine"))
    .withVhost("/")
    .withReuse(true)

fun initRabbitMQTestContainer(queues: List<String>): RabbitMQContainer =
  queues.fold(
    RabbitMQContainer(DockerImageName.parse("rabbitmq").withTag("3.9.21-management-alpine")),
  ) { container, queueName ->
    container
      .withQueue(queueName)
      .withQueue("$queueName-test")
      .withExchange(queueName, "fanout")
      .withBinding(queueName, queueName)
      .withBinding(queueName, "$queueName-test")
  }.withVhost("/")
    .withReuse(true)
    .apply { start() }

fun initRabbitMQTestContainerWithDlq(queues: List<String>): RabbitMQContainer =
  queues.fold(
    RabbitMQContainer(DockerImageName.parse("rabbitmq").withTag("3.9.21-management-alpine")),
  ) { container, queueName ->
    val dlqName = "dlq-test"
    container.withQueue(dlqName, false, true, emptyMap())
    container.withExchange("dead-letter-exchange", "direct", true, true, true, emptyMap())
    container.withBinding("dead-letter-exchange", dlqName, emptyMap(), queueName, "queue")
    val args = mapOf(
      "x-queue-type" to "quorum",
      "x-delivery-limit" to 2,
      "x-dead-letter-exchange" to "dead-letter-exchange",
    )
    container.withQueue(queueName, false, true, args)
  }.withVhost("/")
    .withReuse(true)
    .apply { start() }

fun initPostgres(
  databaseName: String,
  initScript: String,
  username: String = "zeus",
  reuse: Boolean = true,
): PostgreSQLContainer<*> =
  createPostgres(databaseName).apply {
    withUsername(username)
    withInitScript(initScript)
    withReuse(reuse)
    start()
  }
