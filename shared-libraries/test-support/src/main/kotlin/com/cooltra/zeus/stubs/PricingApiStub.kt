package com.cooltra.zeus.stubs

import com.cooltra.zeus.domain.HomeSystem
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import ratpack.http.Status
import java.time.Instant

class PricingApiStub(configuration: WireMockConfiguration = WireMockConfiguration.options().dynamicPort()) {

  val server: WireMockServer

  init {
    configuration.notifier(Slf4jNotifier(false))
    server = WireMockServer(configuration)
  }

  fun start(): PricingApiStub {
    server.start()
    defaultMappings()
    return this
  }

  private fun defaultMappings() {
    stubUserRegistrationSubscription()
    stubSubscriptionCharges()
  }

  private fun stubUserRegistrationSubscription() {
    on(
      WireMock.post(WireMock.urlMatching("/users/.+/registration-subscription")).willReturn(
        WireMock.aResponse().withStatus(204),
      ),
    )
  }

  private fun stubSubscriptionCharges() {
    val body = """
      {
        "subscriptionName": "basic",
        "riding": {
            "value": 0.35,
            "currency": "EUR"
        },
        "pausing": {
            "value": 0.15,
            "currency": "EUR"
        },
        "starting": {
            "value": 0.50,
            "currency": "EUR"
        }
      }
    """.trimMargin()
    on(
      WireMock.get(WireMock.urlMatching("/pricing/subscription-charges.+")).willReturn(
        WireMock.aResponse().withStatus(200).withBody(body),
      ),
    )
  }

  fun stubUserCurrentSubscription(subscription: String) {
    val body = """{
      "subscription": "$subscription",
      "validFrom": "${Instant.now()}"
     }
    """.trimMargin()
    on(
      WireMock.get(WireMock.urlMatching("/users/.+/current-subscription")).willReturn(
        WireMock.aResponse().withStatus(200).withBody(body),
      ),
    )
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

  fun stubUserProfile(statusCode: Status, body: String) {
    on(
      WireMock.get(WireMock.urlMatching("/users/.+/profile-data"))
        .willReturn(
          WireMock.aResponse().withStatus(statusCode.code).withBody(body),
        ),
    )
  }

  fun stubUserRegistrationSubscriptionFails() {
    on(
      WireMock.post(WireMock.urlMatching("/users/.+/registration-subscription")).willReturn(
        WireMock.aResponse().withStatus(500),
      ),
    )
  }

  fun stubCalculateRentalPrice(statusCode: Status, body: String) {
    on(
      WireMock.post(WireMock.urlPathMatching("/pricing/rental"))
        .withRequestBody(
          matchingJsonPath("$[?(@.billableDuration == null)]"),
        )
        .willReturn(
          WireMock.aResponse().withStatus(statusCode.code).withBody(body),
        ),
    )
  }

  fun stubCalculateRentalPriceWithBillingDuration(statusCode: Status, body: String) {
    on(
      WireMock.post(WireMock.urlPathMatching("/pricing/rental"))
        .withRequestBody(
          matchingJsonPath("$.billableDuration"),
        )
        .willReturn(
          WireMock.aResponse().withStatus(statusCode.code).withBody(body),
        ),

    )
  }

  fun stubGetRentalPrice(statusCode: Status, body: String) {
    on(
      WireMock.get(WireMock.urlPathMatching("/pricing/rental/.+"))
        .willReturn(
          WireMock.aResponse().withStatus(statusCode.code).withBody(body),
        ),
    )
  }

  fun stubGetRentalsPrices(statusCode: Status, body: String) {
    on(
      WireMock.get(WireMock.urlPathMatching("/pricing/users/.+/rentals"))
        .willReturn(
          WireMock.aResponse().withStatus(statusCode.code).withBody(body),
        ),
    )
  }

  fun stubSubscriptionChargesWith(subscriptionName: String = "basic") {
    val body = """
      {
        "subscriptionName": "$subscriptionName",
        "riding": {
            "value": 0,
            "currency": "EUR"
        },
        "pausing": null,
        "starting": {
            "value": 0.50,
            "currency": "EUR"
        }
      }
    """.trimMargin()
    on(
      WireMock.get(WireMock.urlPathMatching("/pricing/subscription-charges")).willReturn(
        WireMock.aResponse().withStatus(200).withBody(body),
      ),
    )
  }

  fun stubGetDefaultPrices() {
    val body = """
      {
        "subscription": {
          "id": "basic",
          "title": "Basica",
          "charges": {
            "riding": {
              "value": 0.3,
              "currency": "EUR"
            },
            "pausing": {
              "value": 0.15,
              "currency": "EUR"
            },
            "starting": {
              "value": 0.5,
              "currency": "EUR"
            }
          }
        }
      }
    """.trimMargin()
    on(
      WireMock.get(WireMock.urlPathMatching("/pricing/default-prices")).willReturn(
        WireMock.aResponse().withStatus(200).withBody(body),
      ),
    )
  }

  fun stubGetUserPrices(vehicleType: String) {
    val body = """
      {
        "credit" : {
          "value": 0.00,
          "currency": "EUR"
        },
        "subscription": {
          "id": "basic",
          "title": "Basic",
          "charges": {
            "riding": {
              "value": 0.31,
              "currency": "EUR"
            },
            "pausing": {
              "value": 0.10,
              "currency": "EUR"
            },
            "starting": {
              "value": 0.00,
              "currency": "EUR"
            }
          }
        },
        "promotions": 0
      }
    """.trimMargin()
    on(
      WireMock.get(WireMock.urlMatching("/users/.+/prices\\?vehicleType=$vehicleType.+")).willReturn(
        WireMock.aResponse().withStatus(200).withBody(body),
      ),
    )
  }

  fun stubSubscriptionChargesByName(subscriptionId: String) {
    val homeSystemCharges = HomeSystem.entries.joinToString(",") {
      """
        {
            "system": "${it.name.lowercase()}",
            "charges": {
              "scooter" : {
                "riding" : {
                  "value": 0.38,
                  "currency": "EUR"
                  },
                "pausing" : {
                  "value": 0.15,
                  "currency": "EUR"
                },
                "starting" : {
                  "value": 0.5,
                  "currency": "EUR"
                }
              },
              "bicycle" : {
                "riding" : {
                  "value": 0.40,
                  "currency": "EUR"
                },
                "pausing" : null,
                "starting" : null
              }
            }
          }
      """
    }

    val body =
      """
        [
          $homeSystemCharges
        ]
      """.trimIndent()
    on(
      WireMock.get(WireMock.urlMatching("/subscriptions/$subscriptionId/charges")).willReturn(
        WireMock.aResponse().withStatus(200).withBody(body),
      ),
    )
  }

  fun stubSubscriptionChargesWithoutBikesByName(subscriptionId: String) {
    val homeSystemCharges = HomeSystem.entries.joinToString(",") {
      """
        {
            "system": "${it.name.lowercase()}",
            "charges": {
              "scooter" : {
                "riding" : {
                  "value": 0.38,
                  "currency": "EUR"
                  },
                "pausing" : {
                  "value": 0.15,
                  "currency": "EUR"
                },
                "starting" : {
                  "value": 0.5,
                  "currency": "EUR"
                }
              }
            }
          }
      """
    }

    val body =
      """
        [
          $homeSystemCharges
        ]
      """.trimIndent()
    on(
      WireMock.get(WireMock.urlMatching("/subscriptions/$subscriptionId/charges")).willReturn(
        WireMock.aResponse().withStatus(200).withBody(body),
      ),
    )
  }
}
