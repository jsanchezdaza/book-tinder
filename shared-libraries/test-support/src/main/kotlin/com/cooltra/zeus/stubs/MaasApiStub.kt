package com.cooltra.zeus.stubs

import com.cooltra.zeus.api.ratpack.JacksonModule
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import java.time.Instant
import java.util.UUID

@Suppress("UnstableApiUsage")
class MaasApiStub(configuration: WireMockConfiguration = WireMockConfiguration.options().dynamicPort()) {

  val server: WireMockServer

  init {
    configuration.notifier(Slf4jNotifier(false))
    server = WireMockServer(configuration)
  }

  fun start(): MaasApiStub {
    server.start()
    defaultMappings()
    return this
  }

  private fun defaultMappings() {
    stubGetMaasUser()
  }

  fun reset() {
    server.resetAll()
    defaultMappings()
  }

  fun on(mappingBuilder: MappingBuilder) = server.addStubMapping(mappingBuilder.build())

  fun stubGetMaasUser() {
    val jsonNode = JacksonModule.OBJECT_MAPPER.createObjectNode()
    jsonNode.put("firstName", UUID.randomUUID().toString())
    jsonNode.put("lastName", UUID.randomUUID().toString())
    jsonNode.putNull("deletedAt")
    jsonNode.putNull("suspendedAt")

    on(
      get(WireMock.urlMatching("/maas/users/.+"))
        .willReturn(
          WireMock.aResponse().withStatus(200).withBody(
            jsonNode.toPrettyString(),
          ),
        ),
    )
  }

  fun stubNotFoundUser() {
    on(
      get(WireMock.urlMatching("/maas/users/.+"))
        .willReturn(WireMock.aResponse().withStatus(404)),
    )
  }

  fun stubDeletedUser() {
    val jsonNode = JacksonModule.OBJECT_MAPPER.createObjectNode()
    jsonNode.put("firstName", UUID.randomUUID().toString())
    jsonNode.put("lastName", UUID.randomUUID().toString())
    jsonNode.put("deletedAt", Instant.now().toString())
    jsonNode.putNull("suspendedAt")

    on(
      get(WireMock.urlMatching("/maas/users/.+"))
        .willReturn(
          WireMock.aResponse().withStatus(200).withBody(
            jsonNode.toPrettyString(),
          ),
        ),
    )
  }

  fun stubSuspendedUser() {
    val jsonNode = JacksonModule.OBJECT_MAPPER.createObjectNode()
    jsonNode.put("firstName", UUID.randomUUID().toString())
    jsonNode.put("lastName", UUID.randomUUID().toString())
    jsonNode.putNull("deletedAt")
    jsonNode.put("suspendedAt", Instant.now().toString())

    on(
      get(WireMock.urlMatching("/maas/users/.+"))
        .willReturn(
          WireMock.aResponse().withStatus(200).withBody(
            jsonNode.toPrettyString(),
          ),
        ),
    )
  }
}
