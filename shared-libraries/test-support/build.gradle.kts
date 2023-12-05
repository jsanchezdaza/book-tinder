plugins {
  kotlin("jvm")
}

dependencies {
  api("com.github.tomakehurst:wiremock:3.0.1")
  api("org.testcontainers:localstack:1.19.3")

  api("io.netty:netty-resolver-dns-native-macos:4.1.101.Final:osx-aarch_64")

  implementation(project(":shared-libraries:ratpack-common"))
  implementation(libs.ratpack.test)

  implementation("io.opentelemetry:opentelemetry-sdk")
  implementation("io.opentelemetry.semconv:opentelemetry-semconv:1.23.1-alpha")
  implementation("io.opentelemetry:opentelemetry-sdk-testing")
  implementation("io.kotest:kotest-runner-junit5")
  implementation("com.icegreen:greenmail:2.0.1")

  implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359") // required for jwt
  implementation("com.amazonaws:aws-java-sdk:1.12.597")
  implementation("software.amazon.awssdk:s3:2.21.30")
  implementation("com.rabbitmq:amqp-client:5.20.0")

  implementation(platform(libs.jdbi.bom))
  implementation("org.jdbi:jdbi3-core")

  api(testLibs.testcontainers)
  api(testLibs.testcontainers.postgresql)
  api(testLibs.testcontainers.rabbitmq)

  // learning tests
  testImplementation(libs.bundles.ratpack)
  testImplementation(libs.ratpack.test)
  testImplementation(libs.ratpack.jdbc)

  testImplementation("org.jdbi:jdbi3-kotlin")
  testImplementation("org.jdbi:jdbi3-postgres")
  testImplementation(libs.hikari)
  testImplementation("org.postgresql:postgresql:42.7.0")
}
