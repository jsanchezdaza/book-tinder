pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.15.1"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("ratpack", "1.9.0")
            version("guice", "5.1.0")
            version("okhttp", "4.11.0")
            version("jackson", "2.15.3")
            version("jdbi", "3.41.3")
            version("hikari", "5.0.1")
            version("flyway", "9.21.1")

            library("jackson-bom", "com.fasterxml.jackson", "jackson-bom").versionRef("jackson")
            library("okhttp-bom", "com.squareup.okhttp3", "okhttp-bom").versionRef("okhttp")
            library("jdbi-bom", "org.jdbi", "jdbi3-bom").versionRef("jdbi")

            library("ratpack-core", "io.ratpack", "ratpack-core").versionRef("ratpack")
            library("ratpack-guice", "io.ratpack", "ratpack-guice").versionRef("ratpack")
            library("guice", "com.google.inject", "guice").versionRef("guice")

            library("ratpack-test", "io.ratpack", "ratpack-test").versionRef("ratpack")
            library("ratpack-groovy-test", "io.ratpack", "ratpack-groovy-test").versionRef("ratpack")
            library("ratpack-jdbc", "io.ratpack", "ratpack-jdbc-tx").versionRef("ratpack")

            library("hikari", "com.zaxxer", "HikariCP").versionRef("hikari")
            library("flyway", "org.flywaydb", "flyway-core").versionRef("flyway")

            bundle("ratpack", listOf("ratpack-core", "ratpack-guice", "guice"))
        }

        create("testLibs") {
            version("logcapture", "1.3.2")
            version("kotest", "5.5.5")
            version("jgiven", "1.2.5")
            version("testcontainers", "1.19.0")

            library("logcapture-core", "org.logcapture", "logcapture-core").versionRef("logcapture")
            library("logcapture-kotest", "org.logcapture", "logcapture-kotest").versionRef("logcapture")

            library("kotest", "io.kotest", "kotest-bom").versionRef("kotest")
            library("jgiven-core", "com.tngtech.jgiven", "jgiven-core").versionRef("jgiven")
            library("jgiven-html5-report", "com.tngtech.jgiven", "jgiven-html5-report").versionRef("jgiven")
            library("jgiven-spock2", "com.tngtech.jgiven", "jgiven-spock2").versionRef("jgiven")

            library("testcontainers", "org.testcontainers", "testcontainers").versionRef("testcontainers")
            library("testcontainers-postgresql", "org.testcontainers", "postgresql").versionRef("testcontainers")
            library("testcontainers-rabbitmq", "org.testcontainers", "rabbitmq").versionRef("testcontainers")

            bundle("jgiven", listOf("jgiven-core", "jgiven-html5-report", "jgiven-spock2"))
            bundle("logcapture", listOf("logcapture-core", "logcapture-kotest"))
        }
    }
}

include(
    "shared-libraries:ratpack-common",
    "shared-libraries:test-support",
    "book-tinder-api",

)

rootProject.name = "book-tinder"
include("book-tinder-api")
