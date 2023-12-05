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

class StripeStub(configuration: WireMockConfiguration = WireMockConfiguration.options().dynamicPort()) {
  val server: WireMockServer

  init {
    configuration.notifier(Slf4jNotifier(false))
    server = WireMockServer(configuration)
  }

  fun start(): StripeStub {
    server.start()
    defaultMappings()
    return this
  }

  fun stop() {
    server.stop()
  }

  private fun defaultMappings() {
    stubCreateCustomer()
    stubSetupIntent()
    stubGetSetupIntent()
    stubGetPaymentMethod()
    stubCreatePaymentIntent()
  }

  fun stubCreatePaymentIntent(
    paymentIntentId: String = "pi_12313213234234",
    clientSecret: String = "pi_232134324234",
    walletType: String? = null,
  ) {
    on(
      post(urlMatching("/v1/payment_intents"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
              {
                "id": "$paymentIntentId",
                "object": "payment_intent",
                "allowed_source_types": [
                  "card"
                ],
                "amount": 10,
                "amount_capturable": 0,
                "amount_received": 10,
                "application": null,
                "application_fee_amount": null,
                "canceled_at": null,
                "cancellation_reason": null,
                "capture_method": "automatic",
                "charges": {
                  "object": "list",
                  "data": [
                    {
                      "id": "ch_1GgQ7CLZ1O17bN2aO1hOX4FF",
                      "object": "charge",
                      "amount": 10,
                      "amount_refunded": 0,
                      "amount_updates": [
                      ],
                      "application": null,
                      "application_fee": null,
                      "application_fee_amount": null,
                      "balance_transaction": "txn_1GgQ7DLZ1O17bN2acMXAHdl6",
                      "billing_details": {
                        "address": {
                          "city": null,
                          "country": null,
                          "line1": null,
                          "line2": null,
                          "postal_code": null,
                          "state": null
                        },
                        "email": null,
                        "name": null,
                        "phone": null
                      },
                      "calculated_statement_descriptor": "cooltra PAYMENT",
                      "captured": true,
                      "created": 1588920922,
                      "currency": "gbp",
                      "customer": null,
                      "description": null,
                      "destination": "acct_1FgDLaEDnVX3mKJR",
                      "dispute": null,
                      "disputed": false,
                      "failure_code": null,
                      "failure_message": null,
                      "fraud_details": {
                      },
                      "invoice": null,
                      "livemode": false,
                      "metadata": {
                      },
                      "on_behalf_of": null,
                      "order": null,
                      "outcome": {
                        "network_status": "approved_by_network",
                        "reason": null,
                        "risk_level": "normal",
                        "risk_score": 17,
                        "seller_message": "Payment complete.",
                        "type": "authorized"
                      },
                      "paid": true,
                      "payment_intent": "$paymentIntentId",
                      "payment_method": "pm_1GgPvTLZ1O17bN2ak2pkOE7P",
                      "payment_method_details": {
                        "card": {
                          "brand": "visa",
                          "checks": {
                            "address_line1_check": null,
                            "address_postal_code_check": null,
                            "cvc_check": null
                          },
                          "country": "GB",
                          "exp_month": 1,
                          "exp_year": 2021,
                          "fingerprint": "ATCWGiwzjc3COYof",
                          "funding": "debit",
                          "installments": null,
                          "last4": "0005",
                          "moto": null,
                          "network": "visa",
                          "three_d_secure": null,
                          "wallet": ${
              if (walletType == null) {
                "null"
              } else {
                """
                      {
                        "$walletType": {
                          "type": "$walletType"
                        },
                        "dynamic_last4": "3063",
                        "type": "$walletType"
                      }
                    """
              }
              }
                        },
                        "type": "card"
                      },
                      "receipt_email": "paymentsquad@cooltra.com",
                      "receipt_number": null,
                      "receipt_url": "https://pay.stripe.com/receipts/acct_1708fRLZ1O17bN2a/ch_1GgQ7CLZ1O17bN2aO1hOX4FF/rcpt_HEtvbjjt0z8WSJU2hCmkTYAH3bIaBjp",
                      "refunded": false,
                      "refunds": {
                        "object": "list",
                        "data": [
                        ],
                        "has_more": false,
                        "total_count": 0,
                        "url": "/v1/charges/ch_1GgQ7CLZ1O17bN2aO1hOX4FF/refunds"
                      },
                      "review": null,
                      "shipping": null,
                      "source": null,
                      "source_transfer": null,
                      "statement_descriptor": null,
                      "statement_descriptor_suffix": null,
                      "status": "requires_confirmation",
                      "transfer": "tr_1GgQ7DLZ1O17bN2akJ1MoLbr",
                      "transfer_data": {
                        "amount": null,
                        "destination": "acct_1FgDLaEDnVX3mKJR"
                      },
                      "transfer_group": "group_pi_1GgQ7CLZ1O17bN2aW3jwg6sj"
                    }
                  ],
                  "has_more": false,
                  "total_count": 1,
                  "url": "/v1/charges?payment_intent=pi_1GgQ7CLZ1O17bN2aW3jwg6sj"
                },
                "client_secret": "$clientSecret",
                "confirmation_method": "manual",
                "created": 1588920922,
                "currency": "gbp",
                "customer": null,
                "description": null,
                "invoice": null,
                "last_payment_error": null,
                "livemode": false,
                "metadata": {
                },
                "next_action": null,
                "next_source_action": null,
                "on_behalf_of": null,
                "payment_method": "pm_1GgPvTLZ1O17bN2ak2pkOE7P",
                "payment_method_options": {
                  "card": {
                    "installments": null,
                    "request_three_d_secure": "automatic"
                  }
                },
                "payment_method_types": [
                  "card"
                ],
                "receipt_email": "paymentsquad@cooltra.com",
                "review": null,
                "setup_future_usage": null,
                "shipping": null,
                "source": null,
                "statement_descriptor": null,
                "statement_descriptor_suffix": null,
                "status": "requires_confirmation",
                "transfer_data": {
                  "destination": "acct_1FgDLaEDnVX3mKJR"
                },
                "transfer_group": "group_pi_1GgQ7CLZ1O17bN2aW3jwg6sj"
              }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubUpdatePaymentIntent(paymentIntentId: String = "pi_12313213234234", clientSecret: String = "pi_232134324234") {
    on(
      post(urlMatching("/v1/payment_intents/$paymentIntentId"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
              {
                "id": "$paymentIntentId",
                "object": "payment_intent",
                "allowed_source_types": [
                  "card"
                ],
                "amount": 10,
                "amount_capturable": 0,
                "amount_received": 10,
                "application": null,
                "application_fee_amount": null,
                "canceled_at": null,
                "cancellation_reason": null,
                "capture_method": "automatic",
                "charges": {
                  "object": "list",
                  "data": [
                    {
                      "id": "ch_1GgQ7CLZ1O17bN2aO1hOX4FF",
                      "object": "charge",
                      "amount": 10,
                      "amount_refunded": 0,
                      "amount_updates": [
                      ],
                      "application": null,
                      "application_fee": null,
                      "application_fee_amount": null,
                      "balance_transaction": "txn_1GgQ7DLZ1O17bN2acMXAHdl6",
                      "billing_details": {
                        "address": {
                          "city": null,
                          "country": null,
                          "line1": null,
                          "line2": null,
                          "postal_code": null,
                          "state": null
                        },
                        "email": null,
                        "name": null,
                        "phone": null
                      },
                      "calculated_statement_descriptor": "cooltra PAYMENT",
                      "captured": true,
                      "created": 1588920922,
                      "currency": "gbp",
                      "customer": null,
                      "description": null,
                      "destination": "acct_1FgDLaEDnVX3mKJR",
                      "dispute": null,
                      "disputed": false,
                      "failure_code": null,
                      "failure_message": null,
                      "fraud_details": {
                      },
                      "invoice": null,
                      "livemode": false,
                      "metadata": {
                      },
                      "on_behalf_of": null,
                      "order": null,
                      "outcome": {
                        "network_status": "approved_by_network",
                        "reason": null,
                        "risk_level": "normal",
                        "risk_score": 17,
                        "seller_message": "Payment complete.",
                        "type": "authorized"
                      },
                      "paid": true,
                      "payment_intent": "$paymentIntentId",
                      "payment_method": "pm_1GgPvTLZ1O17bN2ak2pkOE7P",
                      "payment_method_details": {
                        "card": {
                          "brand": "visa",
                          "checks": {
                            "address_line1_check": null,
                            "address_postal_code_check": null,
                            "cvc_check": null
                          },
                          "country": "GB",
                          "exp_month": 1,
                          "exp_year": 2021,
                          "fingerprint": "ATCWGiwzjc3COYof",
                          "funding": "debit",
                          "installments": null,
                          "last4": "0005",
                          "moto": null,
                          "network": "visa",
                          "three_d_secure": null,
                          "wallet": null
                        },
                        "type": "card"
                      },
                      "receipt_email": "paymentsquad@cooltra.com",
                      "receipt_number": null,
                      "receipt_url": "https://pay.stripe.com/receipts/acct_1708fRLZ1O17bN2a/ch_1GgQ7CLZ1O17bN2aO1hOX4FF/rcpt_HEtvbjjt0z8WSJU2hCmkTYAH3bIaBjp",
                      "refunded": false,
                      "refunds": {
                        "object": "list",
                        "data": [
                        ],
                        "has_more": false,
                        "total_count": 0,
                        "url": "/v1/charges/ch_1GgQ7CLZ1O17bN2aO1hOX4FF/refunds"
                      },
                      "review": null,
                      "shipping": null,
                      "source": null,
                      "source_transfer": null,
                      "statement_descriptor": null,
                      "statement_descriptor_suffix": null,
                      "status": "requires_confirmation",
                      "transfer": "tr_1GgQ7DLZ1O17bN2akJ1MoLbr",
                      "transfer_data": {
                        "amount": null,
                        "destination": "acct_1FgDLaEDnVX3mKJR"
                      },
                      "transfer_group": "group_pi_1GgQ7CLZ1O17bN2aW3jwg6sj"
                    }
                  ],
                  "has_more": false,
                  "total_count": 1,
                  "url": "/v1/charges?payment_intent=pi_1GgQ7CLZ1O17bN2aW3jwg6sj"
                },
                "client_secret": "$clientSecret",
                "confirmation_method": "manual",
                "created": 1588920922,
                "currency": "gbp",
                "customer": null,
                "description": null,
                "invoice": null,
                "last_payment_error": null,
                "livemode": false,
                "metadata": {
                },
                "next_action": null,
                "next_source_action": null,
                "on_behalf_of": null,
                "payment_method": "pm_1GgPvTLZ1O17bN2ak2pkOE7P",
                "payment_method_options": {
                  "card": {
                    "installments": null,
                    "request_three_d_secure": "automatic"
                  }
                },
                "payment_method_types": [
                  "card"
                ],
                "receipt_email": "paymentsquad@cooltra.com",
                "review": null,
                "setup_future_usage": null,
                "shipping": null,
                "source": null,
                "statement_descriptor": null,
                "statement_descriptor_suffix": null,
                "status": "requires_confirmation",
                "transfer_data": {
                  "destination": "acct_1FgDLaEDnVX3mKJR"
                },
                "transfer_group": "group_pi_1GgQ7CLZ1O17bN2aW3jwg6sj"
              }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubUpdatePaymentIntentFails(paymentIntentId: String = "pi_12313213234234") {
    on(
      post(urlMatching("/v1/payment_intents/$paymentIntentId"))
        .willReturn(
          aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }

  fun stubUpdatePaymentIntentFailsWithInvalidRequestError(paymentIntentId: String = "pi_12313213234234") {
    on(
      post(urlMatching("/v1/payment_intents/$paymentIntentId"))
        .willReturn(
          aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
              {
                "error": {
                  "message": "The provided PaymentMethod was previously used with a PaymentIntent without Customer attachment, shared with a connected account without Customer attachment, or was detached from a Customer. It may not be used again. To use a PaymentMethod multiple times, you must attach it to a Customer first.",
                  "request_log_url": "https://dashboard.stripe.com/logs/req_2bIotGF60G7EGH?t=1681902766",
                  "type": "invalid_request_error"
                }
              }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubUpdatePaymentIntentFailsWithPaymentAlreadyPaid(paymentIntentId: String = "pi_12313213234234") {
    on(
      post(urlMatching("/v1/payment_intents/$paymentIntentId"))
        .willReturn(
          aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
                {
                  "error": {
                    "code": "payment_intent_unexpected_state",
                    "doc_url": "https://stripe.com/docs/error-codes/payment-intent-unexpected-state",
                    "message": "You cannot confirm this PaymentIntent because it has already succeeded after being previously confirmed.",
                    "payment_intent": {
                      "id": "$paymentIntentId",
                      "object": "payment_intent",
                      "amount": 380,
                      "amount_capturable": 0,
                      "amount_details": {
                        "tip": {
                        }
                      },
                      "amount_received": 380,
                      "application": null,
                      "application_fee_amount": null,
                      "automatic_payment_methods": null,
                      "canceled_at": null,
                      "cancellation_reason": null,
                      "capture_method": "automatic",
                      "client_secret": "pi_3MvzipFwLMQuqQ0P1ujTiUf0_secret_w9tiL94D5jHd0SKdxIWIzJyh9",
                      "confirmation_method": "automatic",
                      "created": 1681290731,
                      "currency": "eur",
                      "customer": "cus_G3h1A00RDL7GOv",
                      "description": null,
                      "invoice": null,
                      "last_payment_error": null,
                      "latest_charge": "ch_3MvzipFwLMQuqQ0P1Xxgx4cb",
                      "livemode": true,
                      "metadata": {
                        "invoiceId": "a25cb315-4f88-464f-b693-9247ea00bd06",
                        "origin": "RENTAL",
                        "chargeAttemptId": "23589670-ed88-4286-9b9a-e05f5752f513",
                        "userId": "9dc6ef99-f811-4b30-80a3-cb70f8873b2a",
                        "paymentId": "51023786",
                        "retriedByUser": "false"
                      },
                      "next_action": null,
                      "on_behalf_of": null,
                      "payment_method": "pm_1LBbczFwLMQuqQ0Pcjl1CsXE",
                      "payment_method_options": {
                        "card": {
                          "installments": null,
                          "mandate_options": null,
                          "network": null,
                          "request_three_d_secure": "automatic"
                        }
                      },
                      "payment_method_types": [
                        "card"
                      ],
                      "processing": null,
                      "receipt_email": null,
                      "review": null,
                      "setup_future_usage": null,
                      "shipping": null,
                      "source": null,
                      "statement_descriptor": null,
                      "statement_descriptor_suffix": null,
                      "status": "succeeded",
                      "transfer_data": null,
                      "transfer_group": null
                    },
                    "request_log_url": "https://dashboard.stripe.com/logs/req_nXQ5PZQNKs7oVi?t=1682490204",
                    "type": "invalid_request_error"
                  }
                }                                                                                                                                               
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubGetPaymentMethodByCustomerId(
    paymentMethodId: String = "payment_method_id",
    customerId: String = "customer_id",
  ) {
    on(
      get(urlMatching("/v1/payment_methods/.+"))
        .inScenario("get_payment_method")
        .withHeader("Authorization", equalTo("Bearer stripe-key"))
        .willSetStateTo("get_second_payment_method")
        .willReturn(
          aResponse().withStatus(200).withBody(
            getPaymentMethodResponseFrom(
              customerId = customerId,
              paymentMethodId = paymentMethodId,
              brand = "visa",
              expMonth = 8,
              expYear = 2023,
              last4 = "4242",
            ),
          ),
        ),
    )
  }

  fun stubGetPaymentMethodByCustomerIdFromAWallet(
    paymentMethodId: String = "payment_method_id",
    customerId: String = "customer_id",
    walletType: String = "apple_pay",
  ) {
    on(
      get(urlMatching("/v1/payment_methods/.+"))
        .inScenario("get_payment_method")
        .withHeader("Authorization", equalTo("Bearer stripe-key"))
        .willSetStateTo("get_second_payment_method")
        .willReturn(
          aResponse().withStatus(200).withBody(
            getPaymentMethodResponseFrom(
              customerId = customerId,
              paymentMethodId = paymentMethodId,
              brand = "visa",
              expMonth = 8,
              expYear = 2023,
              last4 = "4242",
              walletType = walletType,
            ),
          ),
        ),
    )
  }

  fun stubGetPaymentMethodExpired(
    paymentMethodId: String = "payment_method_id",
    customerId: String = "customer_id",
    expiryMonth: Int,
    expiryYear: Int,
    lastFourDigits: String,
  ) {
    on(
      get(urlMatching("/v1/payment_methods/.+"))
        .inScenario("get_payment_method")
        .withHeader("Authorization", equalTo("Bearer stripe-key"))
        .willSetStateTo("get_second_payment_method")
        .willReturn(
          aResponse().withStatus(200).withBody(
            getPaymentMethodResponseFrom(
              customerId = customerId,
              paymentMethodId = paymentMethodId,
              brand = "visa",
              expMonth = expiryMonth,
              expYear = expiryYear,
              last4 = lastFourDigits,
            ),
          ),
        ),
    )
  }

  fun stubGetPaymentMethod(
    paymentMethodId: String = "payment_method_id",
    customerId: String = "customer_id",
    walletType: String? = null,
  ) {
    on(
      get(urlMatching("/v1/payment_methods/.+"))
        .inScenario("get_payment_method")
        .withHeader("Authorization", equalTo("Bearer stripe-key"))
        .willSetStateTo("get_second_payment_method")
        .willReturn(
          aResponse().withStatus(200).withBody(
            getPaymentMethodResponseFrom(
              customerId = customerId,
              paymentMethodId = paymentMethodId,
              brand = "visa",
              expMonth = 8,
              expYear = 2023,
              last4 = "4242",
              walletType = walletType,
            ),
          ),
        ),
    )
    on(
      get(urlMatching("/v1/payment_methods/.+"))
        .inScenario("get_payment_method")
        .whenScenarioStateIs("get_second_payment_method")
        .withHeader("Authorization", equalTo("Bearer stripe-key"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            getPaymentMethodResponseFrom(
              customerId = "new_customer_id",
              paymentMethodId = "new_payment_method_id",
              brand = "mastercard",
              expMonth = 9,
              expYear = 2024,
              last4 = "4243",
            ),
          ),
        ),
    )
  }

  fun stubGetSetupIntentByCustomerId(
    paymentMethodId: String = "payment_method_id",
    customerId: String = "customer_id",
  ) {
    on(
      get(urlMatching("/v1/setup_intents/.+"))
        .inScenario("get_setup_intent")
        .willSetStateTo("get_second_setup_intent")
        .withHeader("Authorization", equalTo("Bearer stripe-key"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            getSetupIntentResponseFrom(
              customerId = customerId,
              paymentMethodId = paymentMethodId,
            ),
          ),
        ),
    )
  }

  fun stubGetSetupIntent(paymentMethodId: String = "payment_method_id", customerId: String = "customer_id") {
    on(
      get(urlMatching("/v1/setup_intents/.+"))
        .inScenario("get_setup_intent")
        .willSetStateTo("get_second_setup_intent")
        .withHeader("Authorization", equalTo("Bearer stripe-key"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            getSetupIntentResponseFrom(
              customerId = customerId,
              paymentMethodId = paymentMethodId,
            ),
          ),
        ),
    )
    on(
      get(urlMatching("/v1/setup_intents/.+"))
        .inScenario("get_setup_intent")
        .whenScenarioStateIs("get_second_setup_intent")
        .withHeader("Authorization", equalTo("Bearer stripe-key"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            getSetupIntentResponseFrom(
              customerId = "new_customer_id",
              paymentMethodId = "new_payment_method_id",
            ),
          ),
        ),
    )
  }

  fun stubGetSetupIntentWithoutPaymentMethod(customerId: String = "customer_id") {
    on(
      get(urlMatching("/v1/setup_intents/.+"))
        .inScenario("get_setup_intent_without_payment_method")
        .willSetStateTo("get_second_setup_intent")
        .withHeader("Authorization", equalTo("Bearer stripe-key"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            getSetupIntentResponseFrom(
              customerId = customerId,
              paymentMethodId = null,
              status = "requires_payment_method",
            ),
          ),
        ),
    )
  }

  fun stubGetSetupIntentNotSucceeded(customerId: String = "customer_id") {
    on(
      get(urlMatching("/v1/setup_intents/.+"))
        .inScenario("get_setup_intent_without_payment_method")
        .willSetStateTo("get_second_setup_intent")
        .withHeader("Authorization", equalTo("Bearer stripe-key"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            getSetupIntentResponseFrom(
              customerId = customerId,
              paymentMethodId = "new_payment_method_id",
              status = "requires_payment_method",
            ),
          ),
        ),
    )
  }

  private fun stubSetupIntent() {
    on(
      post(urlMatching("/v1/setup_intents"))
        .withHeader("Authorization", equalTo("Bearer stripe-key"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            """
               {
                "id": "seti_xyz",
                "object": "setup_intent",
                "application": null,
                "cancellation_reason": null,
                "client_secret": "seti_xyz_secret_xyz",
                "created": 1669738663,
                "customer": "customer_id",
                "description": null,
                "flow_directions": null,
                "last_setup_error": null,
                "latest_attempt": null,
                "livemode": false,
                "mandate": null,
                "metadata": {},
                "next_action": null,
                "on_behalf_of": null,
                "payment_method": null,
                "payment_method_options": {
                  "card": {
                    "mandate_options": null,
                    "network": null,
                    "request_three_d_secure": "automatic"
                  }
                },
                "payment_method_types": [
                  "card"
                ],
                "single_use_mandate": null,
                "status": "requires_payment_method",
                "usage": "off_session"
              }                                                                                                                                                                                                                                    
            """.trimIndent(),
          ),
        ),
    )
  }

  private fun stubCreateCustomer() {
    on(
      post(urlMatching("/v1/customers"))
        .withHeader("Authorization", equalTo("Bearer stripe-key"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            """
              {
                "id": "customer_id",
                "object": "customer",
                "account_balance": 0,
                "address": null,
                "balance": 0,
                "created": 1669738306,
                "currency": null,
                "default_currency": null,
                "default_source": null,
                "delinquent": false,
                "description": "Jordi",
                "discount": null,
                "email": null,
                "invoice_prefix": "138F2BBA",
                "invoice_settings": {
                  "custom_fields": null,
                  "default_payment_method": null,
                  "footer": null,
                  "rendering_options": null
                },
                "livemode": false,
                "metadata": {
                },
                "name": null,
                "next_invoice_sequence": 1,
                "phone": null,
                "preferred_locales": [],
                "shipping": null,
                "sources": {
                  "object": "list",
                  "data": [],
                  "has_more": false,
                  "total_count": 0,
                  "url": "/v1/customers/customer_id/sources"
                },
                "subscriptions": {
                  "object": "list",
                  "data": [],
                  "has_more": false,
                  "total_count": 0,
                  "url": "/v1/customers/customer_id/subscriptions"
                },
                "tax_exempt": "none",
                "tax_ids": {
                  "object": "list",
                  "data": [],
                  "has_more": false,
                  "total_count": 0,
                  "url": "/v1/customers/customer_id/tax_ids"
                },
                "tax_info": null,
                "tax_info_verification": null,
                "test_clock": null
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

  private fun getPaymentMethodResponseFrom(
    customerId: String,
    paymentMethodId: String,
    brand: String,
    expMonth: Int,
    expYear: Int,
    last4: String,
    walletType: String? = null,
  ): String = """
     {
      "id": "$paymentMethodId",
      "object": "payment_method",
      "billing_details": {
        "address": {
          "city": null,
          "country": null,
          "line1": null,
          "line2": null,
          "postal_code": null,
          "state": null
        },
        "email": null,
        "name": null,
        "phone": null
      },
      "card": {
        "brand": "$brand",
        "checks": {
          "address_line1_check": null,
          "address_postal_code_check": null,
          "cvc_check": "unchecked"
        },
        "country": "US",
        "exp_month": $expMonth,
        "exp_year": $expYear,
        "fingerprint": "finger_print",
        "funding": "credit",
        "generated_from": null,
        "last4": "$last4",
        "networks": {
          "available": [
            "visa"
          ],
          "preferred": null
        },
        "three_d_secure_usage": {
          "supported": true
        },
        "wallet": ${
  if (walletType == null) {
    "null"
  } else {
    """
                      {
                        "$walletType": {
                          "type": "$walletType"
                        },
                        "dynamic_last4": "3063",
                        "type": "$walletType"
                      }
                    """
  }
  }
      },
      "created": 1669140419,
      "customer": "$customerId",
      "livemode": false,
      "metadata": {},
      "redaction": null,
      "type": "card"
    }                                                                                                                                                                                                                                
  """.trimIndent()

  private fun getSetupIntentResponseFrom(
    customerId: String,
    paymentMethodId: String?,
    status: String = "succeeded",
  ): String = """
       {
        "id": "seti_intent_id",
        "object": "setup_intent",
        "application": null,
        "cancellation_reason": null,
        "client_secret": "seti_intent_id",
        "created": 1562687389,
        "customer": "$customerId",
        "description": null,
        "flow_directions": null,
        "last_setup_error": null,
        "latest_attempt": null,
        "livemode": false,
        "mandate": null,
        "metadata": {},
        "next_action": null,
        "on_behalf_of": null,
        ${paymentMethodId?.let { """ "payment_method": "$it", """ } ?: ""} 
        "payment_method_options": {
          "card": {
            "mandate_options": null,
            "network": null,
            "request_three_d_secure": "automatic"
          }
        },
        "payment_method_types": [
          "card"
        ],
        "redaction": null,
        "single_use_mandate": null,
        "status": "$status",
        "usage": "off_session"
      }                                                                                                                                                                                                                                  
  """.trimIndent()

  fun stubCreatePaymentIntentFails() = apply {
    on(
      post(urlMatching("/v1/payment_intents"))
        .willReturn(
          aResponse()
            .withStatus(500)
            .withBody(
              """
              {
                "error": {
                  "message": "An unknown error occurred",
                  "request_log_url": "https://dashboard.stripe.com/logs/req_x3XKDi1sBYSYuN?t=1681940544",
                  "type": "api_error"
                }
              }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubCreatePaymentIntentOffline(paymentIntentId: String, walletType: String? = null) {
    on(
      post(urlMatching("/v1/payment_intents"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
              {
                "id": "$paymentIntentId",
                "object": "payment_intent",
                "allowed_source_types": [
                  "card"
                ],
                "amount": 1000,
                "amount_capturable": 0,
                "amount_details": {
                  "tip": {}
                },
                "amount_received": 1000,
                "application": null,
                "application_fee_amount": null,
                "automatic_payment_methods": null,
                "canceled_at": null,
                "cancellation_reason": null,
                "capture_method": "automatic",
                "charges": {
                  "object": "list",
                  "data": [
                    {
                      "id": "ch_charge_id",
                      "object": "charge",
                      "amount": 1000,
                      "amount_captured": 1000,
                      "amount_refunded": 0,
                      "application": null,
                      "application_fee": null,
                      "application_fee_amount": null,
                      "balance_transaction": "txn_transaction_id",
                      "billing_details": {
                        "address": {
                          "city": null,
                          "country": null,
                          "line1": null,
                          "line2": null,
                          "postal_code": null,
                          "state": null
                        },
                        "email": null,
                        "name": null,
                        "phone": null
                      },
                      "calculated_statement_descriptor": "COOLTRA",
                      "captured": true,
                      "created": 1670491774,
                      "currency": "eur",
                      "customer": "cus_customer_id",
                      "description": null,
                      "destination": null,
                      "dispute": null,
                      "disputed": false,
                      "failure_balance_transaction": null,
                      "failure_code": null,
                      "failure_message": null,
                      "fraud_details": {},
                      "invoice": null,
                      "livemode": false,
                      "metadata": {
                      },
                      "on_behalf_of": null,
                      "order": null,
                      "outcome": {
                        "network_status": "approved_by_network",
                        "reason": null,
                        "risk_level": "normal",
                        "risk_score": 28,
                        "seller_message": "Payment complete.",
                        "type": "authorized"
                      },
                      "paid": true,
                      "payment_intent": "$paymentIntentId",
                      "payment_method": "pm_payment_method_id",
                      "payment_method_details": {
                        "card": {
                          "brand": "visa",
                          "checks": {
                            "address_line1_check": null,
                            "address_postal_code_check": null,
                            "cvc_check": null
                          },
                          "country": "US",
                          "exp_month": 11,
                          "exp_year": 2023,
                          "fingerprint": "a_finger_print",
                          "funding": "credit",
                          "installments": null,
                          "last4": "4242",
                          "mandate": null,
                          "network": "visa",
                          "three_d_secure": null,
                          "wallet": ${
              if (walletType == null) {
                "null"
              } else {
                """
                      {
                        "$walletType": {
                          "type": "$walletType"
                        },
                        "dynamic_last4": "3063",
                        "type": "$walletType"
                      }
                    """
              }
              }
                        },
                        "type": "card"
                      },
                      "receipt_email": null,
                      "receipt_number": null,
                      "receipt_url": "https://pay.stripe.com/receipts/payment",
                      "refunded": false,
                      "refunds": {
                        "object": "list",
                        "data": [],
                        "has_more": false,
                        "total_count": 0,
                        "url": "/v1/charges/ch_charge_id/refunds"
                      },
                      "review": null,
                      "shipping": null,
                      "source": null,
                      "source_transfer": null,
                      "statement_descriptor": null,
                      "statement_descriptor_suffix": null,
                      "status": "succeeded",
                      "transfer_data": null,
                      "transfer_group": null
                    }
                  ],
                  "has_more": false,
                  "total_count": 1,
                  "url": "/v1/charges?payment_intent=$paymentIntentId"
                },
                "client_secret": "${paymentIntentId}_secret_seti_intent_id",
                "confirmation_method": "automatic",
                "created": 1670491774,
                "currency": "eur",
                "customer": "cus_customer_id",
                "description": null,
                "invoice": null,
                "last_payment_error": null,
                "livemode": false,
                "metadata": {
                },
                "next_action": null,
                "next_source_action": null,
                "on_behalf_of": null,
                "payment_method": "pm_payment_method_id",
                "payment_method_options": {
                  "card": {
                    "installments": null,
                    "mandate_options": null,
                    "network": null,
                    "request_three_d_secure": "automatic"
                  }
                },
                "payment_method_types": [
                  "card"
                ],
                "processing": null,
                "receipt_email": null,
                "review": null,
                "setup_future_usage": null,
                "shipping": null,
                "source": null,
                "statement_descriptor": null,
                "statement_descriptor_suffix": null,
                "status": "succeeded",
                "transfer_data": null,
                "transfer_group": null
              }                                                                                                                                                                   
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubCreatePaymentIntentFailsWithCardDeclined(paymentIntentId: String) {
    on(
      post(urlMatching("/v1/payment_intents"))
        .willReturn(
          aResponse()
            .withStatus(402)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
                {
                  "error": {
                    "charge": "ch_charge_id",
                    "code": "card_declined",
                    "decline_code": "generic_decline",
                    "doc_url": "https://stripe.com/docs/error-codes/card-declined",
                    "message": "Your card was declined.",
                    "payment_intent": {
                      "id": "$paymentIntentId",
                      "object": "payment_intent",
                      "allowed_source_types": [
                        "card"
                      ],
                      "amount": 1000,
                      "amount_capturable": 0,
                      "amount_details": {
                        "tip": {
                        }
                      },
                      "amount_received": 0,
                      "application": null,
                      "application_fee_amount": null,
                      "automatic_payment_methods": null,
                      "canceled_at": null,
                      "cancellation_reason": null,
                      "capture_method": "automatic",
                      "charges": {
                        "object": "list",
                        "data": [
                          {
                            "id": "ch_charge_id",
                            "object": "charge",
                            "amount": 1000,
                            "amount_captured": 0,
                            "amount_refunded": 0,
                            "application": null,
                            "application_fee": null,
                            "application_fee_amount": null,
                            "balance_transaction": null,
                            "billing_details": {
                              "address": {
                                "city": null,
                                "country": null,
                                "line1": null,
                                "line2": null,
                                "postal_code": null,
                                "state": null
                              },
                              "email": null,
                              "name": null,
                              "phone": null
                            },
                            "calculated_statement_descriptor": "COOLTRA",
                            "captured": false,
                            "created": 1670533898,
                            "currency": "eur",
                            "customer": "cus_customer_id",
                            "description": null,
                            "destination": null,
                            "dispute": null,
                            "disputed": false,
                            "failure_balance_transaction": null,
                            "failure_code": "card_declined",
                            "failure_message": "Your card was declined.",
                            "fraud_details": {
                            },
                            "invoice": null,
                            "livemode": false,
                            "metadata": {
                            },
                            "on_behalf_of": null,
                            "order": null,
                            "outcome": {
                              "network_status": "declined_by_network",
                              "reason": "generic_decline",
                              "risk_level": "normal",
                              "risk_score": 17,
                              "seller_message": "The bank did not return any further details with this decline.",
                              "type": "issuer_declined"
                            },
                            "paid": false,
                            "payment_intent": "$paymentIntentId",
                            "payment_method": "pm_payment_method_id",
                            "payment_method_details": {
                              "card": {
                                "brand": "visa",
                                "checks": {
                                  "address_line1_check": null,
                                  "address_postal_code_check": null,
                                  "cvc_check": null
                                },
                                "country": "US",
                                "exp_month": 12,
                                "exp_year": 2022,
                                "fingerprint": "fingerprint",
                                "funding": "credit",
                                "installments": null,
                                "last4": "0341",
                                "mandate": null,
                                "network": "visa",
                                "three_d_secure": null,
                                "wallet": null
                              },
                              "type": "card"
                            },
                            "receipt_email": null,
                            "receipt_number": null,
                            "receipt_url": null,
                            "refunded": false,
                            "refunds": {
                              "object": "list",
                              "data": [
                
                              ],
                              "has_more": false,
                              "total_count": 0,
                              "url": "/v1/charges/ch_charge_id/refunds"
                            },
                            "review": null,
                            "shipping": null,
                            "source": null,
                            "source_transfer": null,
                            "statement_descriptor": null,
                            "statement_descriptor_suffix": null,
                            "status": "failed",
                            "transfer_data": null,
                            "transfer_group": null
                          }
                        ],
                        "has_more": false,
                        "total_count": 1,
                        "url": "/v1/charges?payment_intent=$paymentIntentId"
                      },
                      "client_secret": "${paymentIntentId}_secret",
                      "confirmation_method": "automatic",
                      "created": 1670533898,
                      "currency": "eur",
                      "customer": "cus_customer_id",
                      "description": null,
                      "invoice": null,
                      "last_payment_error": {
                        "charge": "ch_charge_id",
                        "code": "card_declined",
                        "decline_code": "generic_decline",
                        "doc_url": "https://stripe.com/docs/error-codes/card-declined",
                        "message": "Your card was declined.",
                        "payment_method": {
                          "id": "pm_payment_method_id",
                          "object": "payment_method",
                          "billing_details": {
                            "address": {
                              "city": null,
                              "country": null,
                              "line1": null,
                              "line2": null,
                              "postal_code": null,
                              "state": null
                            },
                            "email": null,
                            "name": null,
                            "phone": null
                          },
                          "card": {
                            "brand": "visa",
                            "checks": {
                              "address_line1_check": null,
                              "address_postal_code_check": null,
                              "cvc_check": "pass"
                            },
                            "country": "US",
                            "exp_month": 12,
                            "exp_year": 2022,
                            "fingerprint": "fingerprint",
                            "funding": "credit",
                            "generated_from": null,
                            "last4": "0341",
                            "networks": {
                              "available": [
                                "visa"
                              ],
                              "preferred": null
                            },
                            "three_d_secure_usage": {
                              "supported": true
                            },
                            "wallet": null
                          },
                          "created": 1670521867,
                          "customer": "cus_customer_id",
                          "livemode": false,
                          "metadata": {
                          },
                          "type": "card"
                        },
                        "type": "card_error"
                      },
                      "livemode": false,
                      "metadata": {
                      },
                      "next_action": null,
                      "next_source_action": null,
                      "on_behalf_of": null,
                      "payment_method": null,
                      "payment_method_options": {
                        "card": {
                          "installments": null,
                          "mandate_options": null,
                          "network": null,
                          "request_three_d_secure": "automatic"
                        }
                      },
                      "payment_method_types": [
                        "card"
                      ],
                      "processing": null,
                      "receipt_email": null,
                      "review": null,
                      "setup_future_usage": null,
                      "shipping": null,
                      "source": null,
                      "statement_descriptor": null,
                      "statement_descriptor_suffix": null,
                      "status": "requires_source",
                      "transfer_data": null,
                      "transfer_group": null
                    },
                    "payment_method": {
                      "id": "pm_payment_method_id",
                      "object": "payment_method",
                      "billing_details": {
                        "address": {
                          "city": null,
                          "country": null,
                          "line1": null,
                          "line2": null,
                          "postal_code": null,
                          "state": null
                        },
                        "email": null,
                        "name": null,
                        "phone": null
                      },
                      "card": {
                        "brand": "visa",
                        "checks": {
                          "address_line1_check": null,
                          "address_postal_code_check": null,
                          "cvc_check": "pass"
                        },
                        "country": "US",
                        "exp_month": 12,
                        "exp_year": 2022,
                        "fingerprint": "fingerprint",
                        "funding": "credit",
                        "generated_from": null,
                        "last4": "0341",
                        "networks": {
                          "available": [
                            "visa"
                          ],
                          "preferred": null
                        },
                        "three_d_secure_usage": {
                          "supported": true
                        },
                        "wallet": null
                      },
                      "created": 1670521867,
                      "customer": "cus_customer_id",
                      "livemode": false,
                      "metadata": {
                      },
                      "type": "card"
                    },
                    "request_log_url": "https://dashboard.stripe.com/test/logs/",
                    "type": "card_error"
                  }
                }                                                                                                                                                             
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubCreatePaymentIntentFailsWithIdempotencyKeyError(paymentIntentId: String) {
    on(
      post(urlMatching("/v1/payment_intents"))
        .willReturn(
          aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
                {
                  "error": {
                    "message": "Keys for idempotent requests can only be used with the same parameters they were first used with. Try using a key other than 'e2f43624-cc65-44aa-a7fa-08033f995fa1' if you meant to execute a different request.",
                    "request_log_url": "https://dashboard.stripe.com/logs/req_Ypa3cldsd5yDPb?t=1683100113",
                    "type": "idempotency_error"
                  }
                }                                                                                                                                                
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubConfirmPaymentIntentOfflineFailsWithPaymentAlreadyPaid(paymentIntentId: String) {
    on(
      post(urlMatching("/v1/payment_intents/.+/confirm"))
        .willReturn(
          aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
                {
                  "error": {
                    "code": "payment_intent_unexpected_state",
                    "doc_url": "https://stripe.com/docs/error-codes/payment-intent-unexpected-state",
                    "message": "You cannot confirm this PaymentIntent because it has already succeeded after being previously confirmed.",
                    "payment_intent": {
                      "id": "$paymentIntentId",
                      "object": "payment_intent",
                      "amount": 380,
                      "amount_capturable": 0,
                      "amount_details": {
                        "tip": {
                        }
                      },
                      "amount_received": 380,
                      "application": null,
                      "application_fee_amount": null,
                      "automatic_payment_methods": null,
                      "canceled_at": null,
                      "cancellation_reason": null,
                      "capture_method": "automatic",
                      "client_secret": "pi_3MvzipFwLMQuqQ0P1ujTiUf0_secret_w9tiL94D5jHd0SKdxIWIzJyh9",
                      "confirmation_method": "automatic",
                      "created": 1681290731,
                      "currency": "eur",
                      "customer": "cus_G3h1A00RDL7GOv",
                      "description": null,
                      "invoice": null,
                      "last_payment_error": null,
                      "latest_charge": "ch_3MvzipFwLMQuqQ0P1Xxgx4cb",
                      "livemode": true,
                      "metadata": {
                        "invoiceId": "a25cb315-4f88-464f-b693-9247ea00bd06",
                        "origin": "RENTAL",
                        "chargeAttemptId": "23589670-ed88-4286-9b9a-e05f5752f513",
                        "userId": "9dc6ef99-f811-4b30-80a3-cb70f8873b2a",
                        "paymentId": "51023786",
                        "retriedByUser": "false"
                      },
                      "next_action": null,
                      "on_behalf_of": null,
                      "payment_method": "pm_1LBbczFwLMQuqQ0Pcjl1CsXE",
                      "payment_method_options": {
                        "card": {
                          "installments": null,
                          "mandate_options": null,
                          "network": null,
                          "request_three_d_secure": "automatic"
                        }
                      },
                      "payment_method_types": [
                        "card"
                      ],
                      "processing": null,
                      "receipt_email": null,
                      "review": null,
                      "setup_future_usage": null,
                      "shipping": null,
                      "source": null,
                      "statement_descriptor": null,
                      "statement_descriptor_suffix": null,
                      "status": "succeeded",
                      "transfer_data": null,
                      "transfer_group": null
                    },
                    "request_log_url": "https://dashboard.stripe.com/logs/req_nXQ5PZQNKs7oVi?t=1682490204",
                    "type": "invalid_request_error"
                  }
                }                                                                                                                                               
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubCreatePaymentIntentOfflineFailsWith3ds(paymentIntentId: String) {
    on(
      post(urlMatching("/v1/payment_intents"))
        .willReturn(
          aResponse()
            .withStatus(402)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
               {
                "error": {
                  "charge": "ch_3Mc6LgFwLMQuqQ0P1NhlCQzn",
                  "code": "authentication_required",
                  "decline_code": "authentication_required",
                  "doc_url": "https://stripe.com/docs/error-codes/authentication-required",
                  "message": "Your card was declined. This transaction requires authentication.",
                  "payment_intent": {
                    "id": "$paymentIntentId",
                    "object": "payment_intent",
                    "allowed_source_types": [
                      "card"
                    ],
                    "amount": 200,
                    "amount_capturable": 0,
                    "amount_details": {
                      "tip": {
                      }
                    },
                    "amount_received": 0,
                    "application": null,
                    "application_fee_amount": null,
                    "automatic_payment_methods": null,
                    "canceled_at": null,
                    "cancellation_reason": null,
                    "capture_method": "automatic",
                    "charges": {
                      "object": "list",
                      "data": [
                        {
                          "id": "ch_3Mc6LgFwLMQuqQ0P1NhlCQzn",
                          "object": "charge",
                          "amount": 200,
                          "amount_captured": 0,
                          "amount_refunded": 0,
                          "application": null,
                          "application_fee": null,
                          "application_fee_amount": null,
                          "balance_transaction": null,
                          "billing_details": {
                            "address": {
                              "city": null,
                              "country": null,
                              "line1": null,
                              "line2": null,
                              "postal_code": null,
                              "state": null
                            },
                            "email": null,
                            "name": null,
                            "phone": null
                          },
                          "calculated_statement_descriptor": "COOLTRA",
                          "captured": false,
                          "created": 1676549644,
                          "currency": "eur",
                          "customer": null,
                          "description": null,
                          "destination": null,
                          "dispute": null,
                          "disputed": false,
                          "failure_balance_transaction": null,
                          "failure_code": "authentication_required",
                          "failure_message": "Your card was declined. This transaction requires authentication.",
                          "fraud_details": {
                          },
                          "invoice": null,
                          "livemode": false,
                          "metadata": {
                          },
                          "on_behalf_of": null,
                          "order": null,
                          "outcome": {
                            "network_status": "declined_by_network",
                            "reason": "authentication_required",
                            "risk_level": "normal",
                            "risk_score": 30,
                            "seller_message": "The bank returned the decline code `authentication_required`.",
                            "type": "issuer_declined"
                          },
                          "paid": false,
                          "payment_intent": "$paymentIntentId",
                          "payment_method": "pm_1Mc6LfFwLMQuqQ0PbcW0kkqo",
                          "payment_method_details": {
                            "card": {
                              "brand": "visa",
                              "checks": {
                                "address_line1_check": null,
                                "address_postal_code_check": null,
                                "cvc_check": null
                              },
                              "country": "IE",
                              "exp_month": 2,
                              "exp_year": 2024,
                              "fingerprint": "VxXTYcsNpFQAXmlT",
                              "funding": "credit",
                              "installments": null,
                              "last4": "3220",
                              "mandate": null,
                              "network": "visa",
                              "three_d_secure": null,
                              "wallet": null
                            },
                            "type": "card"
                          },
                          "receipt_email": null,
                          "receipt_number": null,
                          "receipt_url": null,
                          "refunded": false,
                          "refunds": {
                            "object": "list",
                            "data": [
                            ],
                            "has_more": false,
                            "total_count": 0,
                            "url": "/v1/charges/ch_3Mc6LgFwLMQuqQ0P1NhlCQzn/refunds"
                          },
                          "review": null,
                          "shipping": null,
                          "source": null,
                          "source_transfer": null,
                          "statement_descriptor": null,
                          "statement_descriptor_suffix": null,
                          "status": "failed",
                          "transfer_data": null,
                          "transfer_group": null
                        }
                      ],
                      "has_more": false,
                      "total_count": 1,
                      "url": "/v1/charges?payment_intent=$paymentIntentId"
                    },
                    "client_secret": "pi_3Mc6LgFwLMQuqQ0P1qv3bfRi_secret_ipPkCuGtpE2dhJJUDLVucXJZ4",
                    "confirmation_method": "automatic",
                    "created": 1676549644,
                    "currency": "eur",
                    "customer": null,
                    "description": null,
                    "invoice": null,
                    "last_payment_error": {
                      "charge": "ch_3Mc6LgFwLMQuqQ0P1NhlCQzn",
                      "code": "authentication_required",
                      "decline_code": "authentication_required",
                      "doc_url": "https://stripe.com/docs/error-codes/authentication-required",
                      "message": "Your card was declined. This transaction requires authentication.",
                      "payment_method": {
                        "id": "pm_1Mc6LfFwLMQuqQ0PbcW0kkqo",
                        "object": "payment_method",
                        "billing_details": {
                          "address": {
                            "city": null,
                            "country": null,
                            "line1": null,
                            "line2": null,
                            "postal_code": null,
                            "state": null
                          },
                          "email": null,
                          "name": null,
                          "phone": null
                        },
                        "card": {
                          "brand": "visa",
                          "checks": {
                            "address_line1_check": null,
                            "address_postal_code_check": null,
                            "cvc_check": null
                          },
                          "country": "IE",
                          "exp_month": 2,
                          "exp_year": 2024,
                          "fingerprint": "VxXTYcsNpFQAXmlT",
                          "funding": "credit",
                          "generated_from": null,
                          "last4": "3220",
                          "networks": {
                            "available": [
                              "visa"
                            ],
                            "preferred": null
                          },
                          "three_d_secure_usage": {
                            "supported": true
                          },
                          "wallet": null
                        },
                        "created": 1676549643,
                        "customer": null,
                        "livemode": false,
                        "metadata": {
                        },
                        "type": "card"
                      },
                      "type": "card_error"
                    },
                    "latest_charge": "ch_3Mc6LgFwLMQuqQ0P1NhlCQzn",
                    "livemode": false,
                    "metadata": {
                    },
                    "next_action": null,
                    "next_source_action": null,
                    "on_behalf_of": null,
                    "payment_method": null,
                    "payment_method_options": {
                      "card": {
                        "installments": null,
                        "mandate_options": null,
                        "network": null,
                        "request_three_d_secure": "automatic"
                      }
                    },
                    "payment_method_types": [
                      "card"
                    ],
                    "processing": null,
                    "receipt_email": null,
                    "review": null,
                    "setup_future_usage": null,
                    "shipping": null,
                    "source": null,
                    "statement_descriptor": null,
                    "statement_descriptor_suffix": null,
                    "status": "requires_source",
                    "transfer_data": null,
                    "transfer_group": null
                  },
                  "payment_method": {
                    "id": "pm_1Mc6LfFwLMQuqQ0PbcW0kkqo",
                    "object": "payment_method",
                    "billing_details": {
                      "address": {
                        "city": null,
                        "country": null,
                        "line1": null,
                        "line2": null,
                        "postal_code": null,
                        "state": null
                      },
                      "email": null,
                      "name": null,
                      "phone": null
                    },
                    "card": {
                      "brand": "visa",
                      "checks": {
                        "address_line1_check": null,
                        "address_postal_code_check": null,
                        "cvc_check": null
                      },
                      "country": "IE",
                      "exp_month": 2,
                      "exp_year": 2024,
                      "fingerprint": "VxXTYcsNpFQAXmlT",
                      "funding": "credit",
                      "generated_from": null,
                      "last4": "3220",
                      "networks": {
                        "available": [
                          "visa"
                        ],
                        "preferred": null
                      },
                      "three_d_secure_usage": {
                        "supported": true
                      },
                      "wallet": null
                    },
                    "created": 1676549643,
                    "customer": null,
                    "livemode": false,
                    "metadata": {
                    },
                    "type": "card"
                  },
                  "request_log_url": "https://dashboard.stripe.com/test/logs/req_VNoI6PtqI2nPmU?t=1676549643",
                  "type": "card_error"
                }
              }                                                                                                                                                          
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubCreatePaymentIntentOfflineFailsWithUnexpectedError() {
    on(
      post(urlMatching("/v1/payment_intents"))
        .willReturn(
          aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
                {
                  "error": {
                    "message": "The provided PaymentMethod was previously used with a PaymentIntent without Customer attachment, shared with a connected account without Customer attachment, or was detached from a Customer. It may not be used again. To use a PaymentMethod multiple times, you must attach it to a Customer first.",
                    "payment_intent": {
                      "id": "pi_3N3ZI4FwLMQuqQ0P0xIyddxt",
                      "object": "payment_intent",
                      "amount": 560,
                      "amount_capturable": 0,
                      "amount_details": {
                        "tip": {
                        }
                      },
                      "amount_received": 0,
                      "application": null,
                      "application_fee_amount": null,
                      "automatic_payment_methods": null,
                      "canceled_at": null,
                      "cancellation_reason": null,
                      "capture_method": "automatic",
                      "client_secret": "pi_3N3ZI4FwLMQuqQ0P0xIyddxt_secret_hus7VTgD2vHnCgN8rMc4P0yRj",
                      "confirmation_method": "automatic",
                      "created": 1683095752,
                      "currency": "eur",
                      "customer": "cus_J484fh4FeOhtFL",
                      "description": null,
                      "invoice": null,
                      "last_payment_error": null,
                      "latest_charge": null,
                      "livemode": true,
                      "metadata": {
                        "retriedByUser": "false",
                        "origin": "RENTAL",
                        "userId": "8458f35c-99e9-4f0d-ad9f-8a8fee653598",
                        "paymentId": "49000101",
                        "invoiceId": "468e76f5-6e1f-4be4-9cf8-248d1b079cf9",
                        "chargeAttemptId": "ee324a0d-be77-4d89-b2c3-1c5ba288c8fe"
                      },
                      "next_action": null,
                      "on_behalf_of": null,
                      "payment_method": null,
                      "payment_method_options": {
                        "card": {
                          "installments": null,
                          "mandate_options": null,
                          "network": null,
                          "request_three_d_secure": "automatic"
                        }
                      },
                      "payment_method_types": [
                        "card"
                      ],
                      "processing": null,
                      "receipt_email": null,
                      "review": null,
                      "setup_future_usage": null,
                      "shipping": null,
                      "source": null,
                      "statement_descriptor": null,
                      "statement_descriptor_suffix": null,
                      "status": "requires_payment_method",
                      "transfer_data": null,
                      "transfer_group": null
                    },
                    "request_log_url": "https://dashboard.stripe.com/logs/req_QB2CmtVRszrooh?t=1683096214",
                    "type": "invalid_request_error"
                  }
                }                                                                                                                                            
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubRefundPayment() {
    on(
      post(urlMatching("/v1/refunds"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
              {
                "id": "re_3MOedeFwLMQuqQ0P05Z2no8e",
                "object": "refund",
                "amount": 250,
                "balance_transaction": "txn_3MOedeFwLMQuqQ0P0fDQdiBt",
                "charge": "ch_3MOedeFwLMQuqQ0P0avw8V62",
                "created": 1673344888,
                "currency": "eur",
                "metadata": {},
                "payment_intent": "pi_3MOedeFwLMQuqQ0P0hlgLfsy",
                "reason": "requested_by_customer",
                "receipt_number": null,
                "source_transfer_reversal": null,
                "status": "succeeded",
                "transfer_reversal": null
              }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubRefundRejectionAsAlreadyRefunded() {
    on(
      post(urlMatching("/v1/refunds"))
        .willReturn(
          aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
                {
                  "error": {
                    "code": "charge_already_refunded",
                    "doc_url": "https://stripe.com/docs/error-codes/charge-already-refunded",
                    "message": "Charge ch_3MOerAFwLMQuqQ0P0eGHUW9c has already been refunded.",
                    "request_log_url": "https://dashboard.stripe.com/test/logs/req_RXwovt0LSQTU81?t=1673446922",
                    "type": "invalid_request_error"
                  }
                }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubRefundRejectionAsDisputed() {
    on(
      post(urlMatching("/v1/refunds"))
        .willReturn(
          aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
                {
                  "error": {
                    "code": "charge_disputed",
                    "doc_url": "https://stripe.com/docs/error-codes/charge-disputed",
                    "message": "Charge ch_3MI9w4FwLMQuqQ0P08ZPtxtm has been charged back; cannot issue a refund.",
                    "request_log_url": "https://dashboard.stripe.com/logs/req_vbXy618bsc5Yiw?t=1673981492",
                    "type": "invalid_request_error"
                  }
                }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubConfirmPaymentIntent(
    paymentIntentId: String = "pi_12313213234234",
    clientSecret: String = "pi_12313213234234_secret_seti_intent_id",
  ) {
    on(
      post(urlMatching("/v1/payment_intents/.+/confirm"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
              {
                "id": "$paymentIntentId",
                "object": "payment_intent",
                "allowed_source_types": [
                  "card"
                ],
                "amount": 1000,
                "amount_capturable": 0,
                "amount_details": {
                  "tip": {}
                },
                "amount_received": 1000,
                "application": null,
                "application_fee_amount": null,
                "automatic_payment_methods": null,
                "canceled_at": null,
                "cancellation_reason": null,
                "capture_method": "automatic",
                "charges": {
                  "object": "list",
                  "data": [
                    {
                      "id": "ch_charge_id",
                      "object": "charge",
                      "amount": 1000,
                      "amount_captured": 1000,
                      "amount_refunded": 0,
                      "application": null,
                      "application_fee": null,
                      "application_fee_amount": null,
                      "balance_transaction": "txn_transaction_id",
                      "billing_details": {
                        "address": {
                          "city": null,
                          "country": null,
                          "line1": null,
                          "line2": null,
                          "postal_code": null,
                          "state": null
                        },
                        "email": null,
                        "name": null,
                        "phone": null
                      },
                      "calculated_statement_descriptor": "COOLTRA",
                      "captured": true,
                      "created": 1670491774,
                      "currency": "eur",
                      "customer": "cus_customer_id",
                      "description": null,
                      "destination": null,
                      "dispute": null,
                      "disputed": false,
                      "failure_balance_transaction": null,
                      "failure_code": null,
                      "failure_message": null,
                      "fraud_details": {},
                      "invoice": null,
                      "livemode": false,
                      "metadata": {
                      },
                      "on_behalf_of": null,
                      "order": null,
                      "outcome": {
                        "network_status": "approved_by_network",
                        "reason": null,
                        "risk_level": "normal",
                        "risk_score": 28,
                        "seller_message": "Payment complete.",
                        "type": "authorized"
                      },
                      "paid": true,
                      "payment_intent": "$paymentIntentId",
                      "payment_method": "pm_payment_method_id",
                      "payment_method_details": {
                        "card": {
                          "brand": "visa",
                          "checks": {
                            "address_line1_check": null,
                            "address_postal_code_check": null,
                            "cvc_check": null
                          },
                          "country": "US",
                          "exp_month": 11,
                          "exp_year": 2023,
                          "fingerprint": "a_finger_print",
                          "funding": "credit",
                          "installments": null,
                          "last4": "4242",
                          "mandate": null,
                          "network": "visa",
                          "three_d_secure": null,
                          "wallet": null
                        },
                        "type": "card"
                      },
                      "receipt_email": null,
                      "receipt_number": null,
                      "receipt_url": "https://pay.stripe.com/receipts/payment",
                      "refunded": false,
                      "refunds": {
                        "object": "list",
                        "data": [],
                        "has_more": false,
                        "total_count": 0,
                        "url": "/v1/charges/ch_charge_id/refunds"
                      },
                      "review": null,
                      "shipping": null,
                      "source": null,
                      "source_transfer": null,
                      "statement_descriptor": null,
                      "statement_descriptor_suffix": null,
                      "status": "succeeded",
                      "transfer_data": null,
                      "transfer_group": null
                    }
                  ],
                  "has_more": false,
                  "total_count": 1,
                  "url": "/v1/charges?payment_intent=$paymentIntentId"
                },
                "client_secret": "$clientSecret",
                "confirmation_method": "automatic",
                "created": 1670491774,
                "currency": "eur",
                "customer": "cus_customer_id",
                "description": null,
                "invoice": null,
                "last_payment_error": null,
                "livemode": false,
                "metadata": {
                },
                "next_action": null,
                "next_source_action": null,
                "on_behalf_of": null,
                "payment_method": "pm_payment_method_id",
                "payment_method_options": {
                  "card": {
                    "installments": null,
                    "mandate_options": null,
                    "network": null,
                    "request_three_d_secure": "automatic"
                  }
                },
                "payment_method_types": [
                  "card"
                ],
                "processing": null,
                "receipt_email": null,
                "review": null,
                "setup_future_usage": null,
                "shipping": null,
                "source": null,
                "statement_descriptor": null,
                "statement_descriptor_suffix": null,
                "status": "succeeded",
                "transfer_data": null,
                "transfer_group": null
              }                                                                                                                                                                   
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubConfirmPaymentIntentOfflineFailsWithCardExpired(paymentIntentId: String = "pi_12313213234234") {
    on(
      post(urlMatching("/v1/payment_intents/.+/confirm"))
        .willReturn(
          aResponse()
            .withStatus(402)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
                {
                  "error": {
                    "charge": "ch_charge_id",
                    "code": "card_declined",
                    "decline_code": "generic_decline",
                    "doc_url": "https://stripe.com/docs/error-codes/card-declined",
                    "message": "Your card was declined.",
                    "payment_intent": {
                      "id": "$paymentIntentId",
                      "object": "payment_intent",
                      "allowed_source_types": [
                        "card"
                      ],
                      "amount": 1000,
                      "amount_capturable": 0,
                      "amount_details": {
                        "tip": {
                        }
                      },
                      "amount_received": 0,
                      "application": null,
                      "application_fee_amount": null,
                      "automatic_payment_methods": null,
                      "canceled_at": null,
                      "cancellation_reason": null,
                      "capture_method": "automatic",
                      "charges": {
                        "object": "list",
                        "data": [
                          {
                            "id": "ch_charge_id",
                            "object": "charge",
                            "amount": 1000,
                            "amount_captured": 0,
                            "amount_refunded": 0,
                            "application": null,
                            "application_fee": null,
                            "application_fee_amount": null,
                            "balance_transaction": null,
                            "billing_details": {
                              "address": {
                                "city": null,
                                "country": null,
                                "line1": null,
                                "line2": null,
                                "postal_code": null,
                                "state": null
                              },
                              "email": null,
                              "name": null,
                              "phone": null
                            },
                            "calculated_statement_descriptor": "COOLTRA",
                            "captured": false,
                            "created": 1670533898,
                            "currency": "eur",
                            "customer": "cus_customer_id",
                            "description": null,
                            "destination": null,
                            "dispute": null,
                            "disputed": false,
                            "failure_balance_transaction": null,
                            "failure_code": "card_declined",
                            "failure_message": "Your card was declined.",
                            "fraud_details": {
                            },
                            "invoice": null,
                            "livemode": false,
                            "metadata": {
                            },
                            "on_behalf_of": null,
                            "order": null,
                            "outcome": {
                              "network_status": "declined_by_network",
                              "reason": "generic_decline",
                              "risk_level": "normal",
                              "risk_score": 17,
                              "seller_message": "The bank did not return any further details with this decline.",
                              "type": "issuer_declined"
                            },
                            "paid": false,
                            "payment_intent": "$paymentIntentId",
                            "payment_method": "pm_payment_method_id",
                            "payment_method_details": {
                              "card": {
                                "brand": "visa",
                                "checks": {
                                  "address_line1_check": null,
                                  "address_postal_code_check": null,
                                  "cvc_check": null
                                },
                                "country": "US",
                                "exp_month": 12,
                                "exp_year": 2022,
                                "fingerprint": "fingerprint",
                                "funding": "credit",
                                "installments": null,
                                "last4": "0341",
                                "mandate": null,
                                "network": "visa",
                                "three_d_secure": null,
                                "wallet": null
                              },
                              "type": "card"
                            },
                            "receipt_email": null,
                            "receipt_number": null,
                            "receipt_url": null,
                            "refunded": false,
                            "refunds": {
                              "object": "list",
                              "data": [
                
                              ],
                              "has_more": false,
                              "total_count": 0,
                              "url": "/v1/charges/ch_charge_id/refunds"
                            },
                            "review": null,
                            "shipping": null,
                            "source": null,
                            "source_transfer": null,
                            "statement_descriptor": null,
                            "statement_descriptor_suffix": null,
                            "status": "failed",
                            "transfer_data": null,
                            "transfer_group": null
                          }
                        ],
                        "has_more": false,
                        "total_count": 1,
                        "url": "/v1/charges?payment_intent=$paymentIntentId"
                      },
                      "client_secret": "${paymentIntentId}_secret",
                      "confirmation_method": "automatic",
                      "created": 1670533898,
                      "currency": "eur",
                      "customer": "cus_customer_id",
                      "description": null,
                      "invoice": null,
                      "last_payment_error": {
                        "charge": "ch_charge_id",
                        "code": "card_declined",
                        "decline_code": "generic_decline",
                        "doc_url": "https://stripe.com/docs/error-codes/card-declined",
                        "message": "Your card was declined.",
                        "payment_method": {
                          "id": "pm_payment_method_id",
                          "object": "payment_method",
                          "billing_details": {
                            "address": {
                              "city": null,
                              "country": null,
                              "line1": null,
                              "line2": null,
                              "postal_code": null,
                              "state": null
                            },
                            "email": null,
                            "name": null,
                            "phone": null
                          },
                          "card": {
                            "brand": "visa",
                            "checks": {
                              "address_line1_check": null,
                              "address_postal_code_check": null,
                              "cvc_check": "pass"
                            },
                            "country": "US",
                            "exp_month": 12,
                            "exp_year": 2022,
                            "fingerprint": "fingerprint",
                            "funding": "credit",
                            "generated_from": null,
                            "last4": "0341",
                            "networks": {
                              "available": [
                                "visa"
                              ],
                              "preferred": null
                            },
                            "three_d_secure_usage": {
                              "supported": true
                            },
                            "wallet": null
                          },
                          "created": 1670521867,
                          "customer": "cus_customer_id",
                          "livemode": false,
                          "metadata": {
                          },
                          "type": "card"
                        },
                        "type": "card_error"
                      },
                      "livemode": false,
                      "metadata": {
                      },
                      "next_action": null,
                      "next_source_action": null,
                      "on_behalf_of": null,
                      "payment_method": null,
                      "payment_method_options": {
                        "card": {
                          "installments": null,
                          "mandate_options": null,
                          "network": null,
                          "request_three_d_secure": "automatic"
                        }
                      },
                      "payment_method_types": [
                        "card"
                      ],
                      "processing": null,
                      "receipt_email": null,
                      "review": null,
                      "setup_future_usage": null,
                      "shipping": null,
                      "source": null,
                      "statement_descriptor": null,
                      "statement_descriptor_suffix": null,
                      "status": "requires_source",
                      "transfer_data": null,
                      "transfer_group": null
                    },
                    "payment_method": {
                      "id": "pm_payment_method_id",
                      "object": "payment_method",
                      "billing_details": {
                        "address": {
                          "city": null,
                          "country": null,
                          "line1": null,
                          "line2": null,
                          "postal_code": null,
                          "state": null
                        },
                        "email": null,
                        "name": null,
                        "phone": null
                      },
                      "card": {
                        "brand": "visa",
                        "checks": {
                          "address_line1_check": null,
                          "address_postal_code_check": null,
                          "cvc_check": "pass"
                        },
                        "country": "US",
                        "exp_month": 12,
                        "exp_year": 2022,
                        "fingerprint": "fingerprint",
                        "funding": "credit",
                        "generated_from": null,
                        "last4": "0341",
                        "networks": {
                          "available": [
                            "visa"
                          ],
                          "preferred": null
                        },
                        "three_d_secure_usage": {
                          "supported": true
                        },
                        "wallet": null
                      },
                      "created": 1670521867,
                      "customer": "cus_customer_id",
                      "livemode": false,
                      "metadata": {
                      },
                      "type": "card"
                    },
                    "request_log_url": "https://dashboard.stripe.com/test/logs/",
                    "type": "card_error"
                  }
                }                                                                                                                                                             
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubConfirmPaymentIntentOfflineFailsWithInternalServerError() {
    on(
      post(urlMatching("/v1/payment_intents/.+/confirm"))
        .willReturn(
          aResponse()
            .withStatus(500)
            .withBody(
              """
              {
                "error": {
                  "message": "An unknown error occurred",
                  "request_log_url": "https://dashboard.stripe.com/logs/req_x3XKDi1sBYSYuN?t=1681940544",
                  "type": "api_error"
                }
              }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubConfirmPaymentIntentOfflineFailsWithInvalidRequestError() {
    on(
      post(urlMatching("/v1/payment_intents/.+/confirm"))
        .willReturn(
          aResponse()
            .withStatus(400)
            .withBody(
              """
              {
                "error": {
                  "message": "The provided PaymentMethod was previously used with a PaymentIntent without Customer attachment, shared with a connected account without Customer attachment, or was detached from a Customer. It may not be used again. To use a PaymentMethod multiple times, you must attach it to a Customer first.",
                  "request_log_url": "https://dashboard.stripe.com/logs/req_2bIotGF60G7EGH?t=1681902766",
                  "type": "invalid_request_error"
                }
              }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubConfirmPaymentIntentOfflineFailsWithPaymentRequired() {
    on(
      post(urlMatching("/v1/payment_intents/.+/confirm"))
        .willReturn(
          aResponse()
            .withStatus(402)
            .withBody(
              """
              {
                "error": {
                  "message": "The provided PaymentMethod was previously used with a PaymentIntent without Customer attachment, shared with a connected account without Customer attachment, or was detached from a Customer. It may not be used again. To use a PaymentMethod multiple times, you must attach it to a Customer first.",
                  "request_log_url": "https://dashboard.stripe.com/logs/req_2bIotGF60G7EGH?t=1681902766",
                  "type": "invalid_request_error"
                }
              }
              """.trimIndent(),
            ),
        ),
    )
  }
}
