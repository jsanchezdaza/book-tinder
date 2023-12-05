package com.cooltra.zeus.stubs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration

class EtendoStub(configuration: WireMockConfiguration = WireMockConfiguration.options().dynamicPort()) {
  val server: WireMockServer

  init {
    configuration.notifier(Slf4jNotifier(false))
    server = WireMockServer(configuration)
  }

  fun start(): EtendoStub {
    server.start()
    defaultMappings()
    return this
  }

  fun stop() {
    server.stop()
  }

  fun reset() {
    server.resetAll()
    defaultMappings()
  }

  private fun defaultMappings() {
  }

  fun on(mappingBuilder: MappingBuilder) = server.addStubMapping(mappingBuilder.build())

  fun stubCreatePaidReservation() {
    on(
      post(urlMatching("/etendo/rest/api/migrateapireservation.+"))
        .withQueryParam("token", equalTo("a_valid_token"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            """
              {
                  "data": [
                      {
                          "Smfscm_Api_Reservation_ID": "1000712",
                          "c_bpartner_id": "26F7B307ACA34F8D85928776973C0163"
                      }
                  ]
              }
            """.trimIndent(),
          ),
        ),
    )
  }

  fun stubAuthErrorWhenCreatingPaidReservation() {
    on(
      post(urlMatching("/etendo/rest/api/migrateapireservation.+"))
        .withQueryParam("token", equalTo("a_valid_token"))
        .willReturn(
          aResponse().withStatus(401),
        ),
    )
  }

  fun stubLogin() {
    on(
      post(urlMatching("/etendo/rest/authentication/login"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            """
            {
                "token": "a_valid_token"
            }
            """.trimIndent(),
          ),
        ),
    )
  }

  fun verifyLoginIsCalled() {
    server.verify(
      WireMock.postRequestedFor(WireMock.urlEqualTo("/etendo/rest/authentication/login"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(
          equalToJson(
            """
          {
              "username":"test",
              "password":"test"
          }
            """.trimIndent(),
          ),
        ),
    )
  }

  fun verifyCreatePaidReservationIsCalled() {
    server.verify(
      WireMock.postRequestedFor(urlMatching("/etendo/rest/api/migrateapireservation.+"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withHeader("Cookie", equalTo("JSESSIONID=BDAA870ED91186C0237088FBC39FAC32"))
        .withQueryParam("token", equalTo("a_valid_token")),
    )
  }

  fun verifyCreatePaidReservationIsCalledForBooking(bookingId: String) {
    server.verify(
      1,
      WireMock.postRequestedFor(urlMatching("/etendo/rest/api/migrateapireservation.+"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withHeader("Cookie", equalTo("JSESSIONID=BDAA870ED91186C0237088FBC39FAC32"))
        .withQueryParam("token", equalTo("a_valid_token"))
        .withRequestBody(matchingJsonPath("booking_id", equalTo(bookingId))),
    )
  }
}
