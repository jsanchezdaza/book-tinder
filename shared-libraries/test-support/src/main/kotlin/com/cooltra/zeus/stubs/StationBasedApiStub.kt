package com.cooltra.zeus.stubs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration

@Suppress("UnstableApiUsage")
class StationBasedApiStub(configuration: WireMockConfiguration = WireMockConfiguration.options().dynamicPort()) {

  val server: WireMockServer

  init {
    configuration
      .notifier(Slf4jNotifier(false))
    server = WireMockServer(configuration)
  }

  fun start(): StationBasedApiStub {
    server.start()
    defaultMappings()
    return this
  }

  fun stop() {
    server.stop()
  }

  fun port(): Int = server.port()

  fun reset() {
    server.resetAll()
    defaultMappings()
  }

  fun on(mappingBuilder: MappingBuilder) = server.addStubMapping(mappingBuilder.build())

  private fun defaultMappings() {
    stubDelegationPrices()
  }

  fun stubDelegationPrices() {
    on(
      get(urlMatching("/prices"))
        .willReturn(aResponse().withStatus(200)),
    )
  }
}
