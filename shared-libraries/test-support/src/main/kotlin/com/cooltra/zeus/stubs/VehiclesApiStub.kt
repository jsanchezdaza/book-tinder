package com.cooltra.zeus.stubs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import ratpack.http.Status

class VehiclesApiStub(configuration: WireMockConfiguration = WireMockConfiguration.options().dynamicPort()) {

  val server: WireMockServer

  init {
    configuration.notifier(Slf4jNotifier(false))
    server = WireMockServer(configuration)
  }

  fun start(): VehiclesApiStub {
    server.start()
    defaultMappings()
    return this
  }

  private fun defaultMappings() {
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

  fun stubUserRentals(statusCode: Status, body: String) {
    on(
      WireMock.get(WireMock.urlMatching("/users/.+/rentals"))
        .willReturn(
          WireMock.aResponse().withStatus(statusCode.code).withBody(body),
        ),
    )
  }
}
