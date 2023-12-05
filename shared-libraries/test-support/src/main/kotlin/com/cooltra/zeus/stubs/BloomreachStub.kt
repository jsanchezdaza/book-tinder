package com.cooltra.zeus.stubs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration

class BloomreachStub(configuration: WireMockConfiguration = WireMockConfiguration.options().dynamicPort()) {
  val server: WireMockServer

  init {
    configuration.notifier(Slf4jNotifier(false))
    server = WireMockServer(configuration)
  }

  fun start(): BloomreachStub {
    server.start()
    defaultMappings()
    return this
  }

  fun stop() {
    server.stop()
  }

  private fun defaultMappings() {
    stubSendUserActivated()
  }

  private fun stubSendUserActivated() {
    on(
      post(urlMatching("/track/v2/projects/.+/customers/events"))
        .inScenario("user_activated")
        .withHeader("Authorization", equalTo("Basic apiKeyDummy"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            """
              {
                  "success": true,
                  "errors": []
              }
            """.trimIndent(),
          ),
        ),
    )
  }

  fun port(): Int = server.port()

  fun reset() {
    server.resetAll()
    defaultMappings()
  }

  fun on(mappingBuilder: MappingBuilder) = server.addStubMapping(mappingBuilder.build())
  fun setupError() {
    on(
      post(urlMatching("/track/v2/projects/.+/customers/events"))
        .willReturn(
          aResponse().withStatus(500).withBody(
            """
              {
                  "success": false,
                  "error": "Unexpected error"
              }
            """.trimIndent(),
          ),
        ),
    )
  }
}
