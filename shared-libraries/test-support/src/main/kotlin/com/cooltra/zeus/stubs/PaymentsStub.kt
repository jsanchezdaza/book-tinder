package com.cooltra.zeus.stubs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.patch
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import java.time.Year
import java.util.UUID
import kotlin.random.Random

class PaymentsStub(configuration: WireMockConfiguration = WireMockConfiguration.options().dynamicPort()) {

  val server: WireMockServer

  init {
    configuration.notifier(Slf4jNotifier(false))
    server = WireMockServer(configuration)
  }

  fun start(): PaymentsStub {
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
    stubConfirmSetupIntent()
    stubGetUserCardOn(Year.now().value + 1, 1)
    refundPayment()
    paymentIntent()
  }

  private fun stubConfirmSetupIntent() {
    on(
      post(urlMatching("/users/.+/setup_intents/.+"))
        .willReturn(aResponse().withStatus(204)),
    )
  }

  fun confirmSetupIntentWillFail() {
    on(
      post(urlMatching("/users/.+/setup_intents/.+"))
        .willReturn(aResponse().withStatus(500)),
    )
  }

  fun confirmSetupIntentReturnsUnprocessableEntity() {
    on(
      post(urlMatching("/users/.+/setup_intents/.+"))
        .willReturn(aResponse().withStatus(422)),
    )
  }

  fun stubGetUserCardOn(year: Int, month: Int) {
    on(
      get(urlMatching("/users/.+/card"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            """
            {
              "brand": "visa",
              "expiryMonth": $month,
              "expiryYear": $year,
              "lastFourDigits": "4242"
            }
            """.trimIndent(),
          ),
        ),
    )
  }

  fun stubGetExpiredUserCard() {
    on(
      get(urlMatching("/users/.+/card"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            """
            {
              "brand": "visa",
              "expiryMonth": 8,
              "expiryYear": 2022,
              "lastFourDigits": "4242"
            }
            """.trimIndent(),
          ),
        ),
    )
  }

  fun stubGetUserCardNotFound() {
    on(
      get(urlMatching("/users/.+/card"))
        .willReturn(aResponse().withStatus(404)),
    )
  }

  fun stubGetUserCardFails() {
    on(
      get(urlMatching("/users/.+/card"))
        .willReturn(aResponse().withStatus(500)),
    )
  }

  fun stubGetUsersWithExpiredCards(from: String, to: String, vararg userIds: String) {
    on(
      get(urlPathEqualTo("/payments/users/expired-card"))
        .withQueryParam("from", equalTo(from))
        .withQueryParam("to", equalTo(to))
        .willReturn(
          aResponse().withStatus(200).withBody(
            """[${userIds.asList().joinToString(prefix = "\"", postfix = "\"")}]""",
          ),
        ),
    )
  }

  fun createPaymentOnline(
    paymentId: String = UUID.randomUUID().toString(),
    clientSecret: String = UUID.randomUUID().toString(),
  ) {
    on(
      post(urlMatching("/payments"))
        .willReturn(
          aResponse().withStatus(201).withBody(
            """
              {
                "paymentId": "$paymentId",
                "clientSecret": "$clientSecret"
              }
            """.trimIndent(),
          ),
        ),
    )
  }

  fun retryPaymentOnline(clientSecret: String = UUID.randomUUID().toString()) {
    on(
      patch(urlMatching("/payments"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            """
              {
                "clientSecret": "$clientSecret"
              }
            """.trimIndent(),
          ),
        ),
    )
  }

  fun createPaymentOffline(
    paymentId: String = Random.nextLong().toString(),
  ) {
    on(
      post(urlMatching("/payments-offline"))
        .willReturn(
          aResponse().withStatus(201).withBody(
            """
              {
                "paymentId": "$paymentId"
              }
            """.trimIndent(),
          ),
        ),
    )
  }

  fun retryPaymentOffline(paymentId: String = Random.nextLong().toString()) {
    on(
      patch(urlMatching("/payments-offline"))
        .willReturn(
          aResponse().withStatus(204).withBody(
            """
              {
                "paymentId": "$paymentId"
              }
            """.trimIndent(),
          ),
        ),
    )
  }

  fun retryPaymentOfflineFailsWithInternalServerError() {
    on(
      patch(urlMatching("/payments-offline"))
        .willReturn(aResponse().withStatus(500)),
    )
  }

  fun createPaymentOnlineFailsWithUnprocessableEntity() {
    on(
      post(urlMatching("/payments"))
        .willReturn(
          aResponse().withStatus(422).withBody(
            """
              {
                "error": "error_code",
                "message": "error message"
              }
            """.trimIndent(),
          ),
        ),
    )
  }

  fun retryPaymentOnlineFailsWithInternalServerError() {
    on(
      patch(urlMatching("/payments"))
        .willReturn(aResponse().withStatus(500)),
    )
  }

  fun createPaymentOfflineFailsWithInternalServerError() {
    on(
      post(urlMatching("/payments-offline"))
        .willReturn(
          aResponse().withStatus(500),
        ),
    )
  }

  fun createPaymentFails() {
    on(
      post(urlMatching("/payments"))
        .willReturn(
          aResponse().withStatus(500),
        ),
    )
  }

  fun refundPayment() {
    on(
      post(urlMatching("/payments/.+/refund"))
        .willReturn(aResponse().withStatus(201)),
    )
  }

  fun paymentIntent(clientSecret: String = UUID.randomUUID().toString()) {
    on(
      post(urlMatching("/payments/payment-intent"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            """
              {
                "clientSecret": "$clientSecret"
              }
            """.trimIndent(),
          ),
        ),
    )
  }

  fun verifyPaymentRefundIsCalled(paymentId: Long) {
    server.verify(
      postRequestedFor(urlEqualTo("/payments/$paymentId/refund"))
        .withHeader("Content-Type", equalTo("application/json")),
    )
  }

  fun verifyRentalPaymentRefundIsCalled(paymentId: Long, amount: Long, idempotencyKey: String) {
    server.verify(
      postRequestedFor(urlEqualTo("/payments/$paymentId/refund"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(
          equalToJson(
            """
              {
                "amount": {
                  "value": $amount,
                  "currency": "EUR"
                },
                "idempotencyKey": "$idempotencyKey"
              }
            """.trimIndent(),
          ),
        ),
    )
  }

  fun refundPaymentFails() {
    on(
      post(urlMatching("/payments/.+/refund"))
        .willReturn(aResponse().withStatus(422)),
    )
  }

  fun refundPaymentConflict() {
    on(
      post(urlMatching("/payments/.+/refund"))
        .willReturn(aResponse().withStatus(409)),
    )
  }

  fun verifyRefundIsNotCalled() {
    server.verify(0, postRequestedFor(urlMatching("/payments/.+/refund")))
  }
}
