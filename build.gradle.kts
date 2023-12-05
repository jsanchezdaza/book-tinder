import com.gradle.enterprise.gradleplugin.testretry.retry
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "1.9.21"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    id("idea")
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "idea")
    apply(plugin = "kotlin")
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    group = "com.cooltra.zeus"

    repositories {
        maven { url = uri("https://repo.osgeo.org/repository/release/") }
        mavenCentral()
    }

    dependencies {
        implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.7.3"))
        implementation(platform("io.opentelemetry:opentelemetry-bom:1.32.0"))
        implementation(platform("io.opentelemetry:opentelemetry-bom-alpha:1.32.0-alpha"))
        implementation(platform("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha:1.32.0-alpha"))

        implementation(platform("com.fasterxml.jackson:jackson-bom:2.16.0"))
        implementation("org.yaml:snakeyaml") { version { strictly("1.33") } }
        implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))

        implementation("ch.qos.logback:logback-classic:1.4.11")
        implementation("net.logstash.logback:logstash-logback-encoder:7.4")
        implementation("org.codehaus.janino:janino:3.1.10")

        implementation(platform("io.kotest:kotest-bom:5.5.5"))
        testImplementation("io.kotest:kotest-runner-junit5")
        testImplementation("io.kotest:kotest-assertions-core")
        testImplementation("io.kotest:kotest-assertions-json")
        testImplementation("io.mockk:mockk:1.13.8")
    }

    kotlin {
        jvmToolchain(17)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        maxHeapSize = "2g"

        retry {
            if (System.getenv("CI") == "true") {
                failOnPassedAfterRetry.set(false)
                maxFailures.set(5)
                maxRetries.set(1)
            }
        }

        if (System.getenv("CI") != "true") {
            environment("TESTCONTAINERS_REUSE_ENABLE", true)
        }

        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.FAILED, TestLogEvent.STANDARD_ERROR, TestLogEvent.SKIPPED)
        }
    }

    tasks.withType<Jar> {
        manifest {
            attributes["Implementation-Title"] = project.name
            attributes["Implementation-Version"] = project.version
        }
    }

    tasks.register<DependencyReportTask>("allDeps") { }
}


kotlin {
    jvmToolchain(8)
}