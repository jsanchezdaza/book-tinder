package com.booktinder.api.ratpack

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import ratpack.server.BaseDir
import ratpack.server.ServerConfigBuilder

object RatpackBaseConfig {
  @JvmStatic
  fun forService(serverConfigBuilder: ServerConfigBuilder): ServerConfigBuilder {
    serverConfigBuilder
      .baseDir(BaseDir.find())
      .configureObjectMapper { objectMapper -> objectMapper.registerKotlinModule() }
      .yaml("application.yaml")
      .sysProps()
      .env()

    val env = System.getenv("COPILOT_ENVIRONMENT_NAME") ?: "local"
    serverConfigBuilder
      .yaml("application-$env.yaml")
      .sysProps()
      .env()

    return serverConfigBuilder
  }
}
