package com.cooltra.zeus.stubs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS

@Suppress("UnstableApiUsage")
class TelematicsApiStub(configuration: WireMockConfiguration = WireMockConfiguration.options().dynamicPort()) {

  val server: WireMockServer

  init {
    configuration
      .notifier(Slf4jNotifier(false))
    server = WireMockServer(configuration)
  }

  fun start(): TelematicsApiStub {
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
    stubAssignTelematicsToVehicle()
    stubUnassignTelematicsToVehicle()
    stubVehicleLock()
    stubVehicleUnlock()
    stubVehicleSync()
    stubVehicleHonk()
    stubVehicleUnlockTopCase()
  }

  fun stubAssignTelematicsToVehicle() {
    on(
      post(urlMatching("/vehicles/.*/telematics"))
        .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
        .willReturn(aResponse().withStatus(204)),
    )
  }

  fun stubUnassignTelematicsToVehicle() {
    on(
      delete(urlMatching("/vehicles/.*/telematics"))
        .willReturn(aResponse().withStatus(204)),
    )
  }

  fun stubVehicleUnlock() {
    on(
      post(urlMatching("/vehicles/.*/actions/unlock"))
        .willReturn(aResponse().withStatus(204)),
    )
  }

  fun stubVehicleLock() {
    on(
      post(urlMatching("/vehicles/.*/actions/lock"))
        .willReturn(aResponse().withStatus(204)),
    )
  }

  fun stubVehicleHonk() {
    on(
      post(urlMatching("/vehicles/.*/actions/honk"))
        .willReturn(aResponse().withStatus(204)),
    )
  }

  fun stubVehicleUnlockTopCase() {
    on(
      post(urlMatching("/vehicles/.*/actions/unlock-top-case"))
        .willReturn(aResponse().withStatus(204)),
    )
  }

  fun stubFailsUnlockingVehicle() {
    on(
      post(urlMatching("/vehicles/.*/actions/unlock"))
        .willReturn(aResponse().withStatus(500)),
    )
  }

  fun stubFailsHonkVehicle() {
    on(
      post(urlMatching("/vehicles/.*/actions/honk"))
        .willReturn(aResponse().withStatus(500)),
    )
  }

  fun stubFailsUnlockTopCaseVehicle() {
    on(
      post(urlMatching("/vehicles/.*/actions/unlock-top-case"))
        .willReturn(aResponse().withStatus(500)),
    )
  }

  fun stubFailsUnlockingVehicleWithTimeout() {
    on(
      post(urlMatching("/vehicles/.*/actions/unlock"))
        .willReturn(aResponse().withFixedDelay(2000).withStatus(204)),
    )
  }

  fun stubFailsLockingVehicle() {
    on(
      post(urlMatching("/vehicles/.*/actions/lock"))
        .willReturn(aResponse().withStatus(500)),
    )
  }

  fun stubFailsLockingVehicleWithTimeout() {
    on(
      post(urlMatching("/vehicles/.*/actions/lock"))
        .willReturn(aResponse().withFixedDelay(2000).withStatus(204)),
    )
  }

  fun stubVehicleSync() {
    on(
      post(urlMatching("/vehicles/.*/actions/sync"))
        .willReturn(aResponse().withStatus(204)),
    )
  }

  fun stubVehicleSyncFails() {
    on(
      post(urlMatching("/vehicles/.*/actions/sync"))
        .willReturn(aResponse().withStatus(422)),
    )
  }

  fun stubGetStatusEvents(startedAt: Instant) {
    on(
      get(urlMatching("/vehicles/.*/status?.*"))
        .willReturn(
          aResponse().withStatus(200)
            .withBody(
              """[
                        {
                          "occurredOn": "${startedAt.minusSeconds(1)}",
                          "lock": true,
                          "centralStandDown": true,
                          "topCaseClosed": true,
                          "powerOff": true,
                          "batteryCharge": 99
                        },
                        {
                          "occurredOn": "${startedAt.plus(1, HOURS)}",
                          "topCaseClosed": false,
                          "powerOff": false,
                          "batteryCharge": 25
                        },
                        {
                          "occurredOn": "${startedAt.plus(2, HOURS)}",
                          "lock": false,
                          "centralStandDown": false
                        }
                      ]""",
            ),
        ),
    )
  }

  fun stubGetStatusEventsWithLargeResponse(startedAt: Instant) {
    on(
      get(urlMatching("/vehicles/.*/status?.*"))
        .willReturn(
          aResponse().withStatus(200)
            .withBody(
              """[
                        ${
              """
                            {
                              "occurredOn": "${startedAt.minusSeconds(1)}",
                              "lock": true,
                              "centralStandDown": true,
                              "topCaseClosed": true,
                              "powerOff": true,
                              "batteryCharge": 99
                            },
                          """.repeat(10_000)
              }
                        {
                          "occurredOn": "${startedAt.plus(1, HOURS)}",
                          "topCaseClosed": false,
                          "powerOff": false,
                          "batteryCharge": 25
                        },
                        {
                          "occurredOn": "${startedAt.plus(2, HOURS)}",
                          "lock": false,
                          "centralStandDown": false
                        }
                      ]""",
            ),
        ),
    )
  }
}
