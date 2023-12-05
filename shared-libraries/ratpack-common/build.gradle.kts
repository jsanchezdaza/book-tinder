plugins {
  kotlin("jvm")
}

dependencies {
  api(libs.bundles.ratpack)
  api(libs.ratpack.jdbc)
  api(libs.hikari)

  api("javax.inject:javax.inject:1")
  api("org.jetbrains.kotlinx:kotlinx-coroutines-core")

  api("com.fasterxml.jackson.module:jackson-module-kotlin")
  api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

  api("io.micrometer:micrometer-registry-prometheus:1.12.0")
  api("io.opentelemetry:opentelemetry-api")
  api("io.opentelemetry:opentelemetry-sdk")
  implementation("io.opentelemetry:opentelemetry-exporter-otlp")
  implementation("io.opentelemetry.semconv:opentelemetry-semconv:1.23.1-alpha")
  implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-api")
  api("io.opentelemetry.instrumentation:opentelemetry-ratpack-1.7")
  api("io.opentelemetry:opentelemetry-extension-kotlin:1.32.0")
  runtimeOnly("io.opentelemetry:opentelemetry-extension-kotlin:1.32.0")
  implementation("com.rabbitmq:amqp-client:5.20.0")

  implementation(platform("io.grpc:grpc-bom:1.58.0"))
  implementation("io.grpc:grpc-protobuf")
  implementation("io.grpc:grpc-netty-shaded")

  testImplementation("io.opentelemetry:opentelemetry-sdk-testing")
  testImplementation("com.squareup.okhttp3:okhttp")
  testImplementation("io.opentelemetry.instrumentation:opentelemetry-okhttp-3.0")

  implementation("com.auth0:java-jwt:4.4.0")

  testImplementation(platform(libs.jdbi.bom))
  testImplementation("org.jdbi:jdbi3-core")
  testImplementation("org.jdbi:jdbi3-kotlin")
  testImplementation("org.jdbi:jdbi3-postgres")
  testImplementation(libs.hikari)
  testImplementation("org.postgresql:postgresql:42.7.0")

  testImplementation(testLibs.testcontainers)
  testImplementation(testLibs.testcontainers.postgresql)
  testImplementation(libs.ratpack.test)

  // Learning tests
  testImplementation("ch.qos.logback:logback-classic:1.4.11")
  testImplementation("net.logstash.logback:logstash-logback-encoder:7.4")
}
