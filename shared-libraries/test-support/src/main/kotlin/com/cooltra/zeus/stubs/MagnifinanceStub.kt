package com.cooltra.zeus.stubs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration

class MagnifinanceStub(val configuration: WireMockConfiguration = WireMockConfiguration.options().dynamicPort()) {

  val server: WireMockServer

  init {
    configuration.notifier(Slf4jNotifier(false))
    server = WireMockServer(configuration)
  }

  fun start(): MagnifinanceStub {
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
    downloadPDF()
  }

  fun acceptCreateDocument() {
    on(
      WireMock.post(urlMatching("/api/v1/document")).willReturn(
        aResponse().withBody(
          """
              {
                "RequestId": "98c3bc62-911d-4f14-a5f4-d610c66e8b8b",
                "Object": {
                  "DocumentId": 67195,
                  "ErrorMessage": null
                },
                "Type": 0,
                "ErrorValue": null,
                "ErrorHumanReadable": null,
                "ValidationErrors": null,
                "IsSuccess": true,
                "IsError": false
              }
          """.trimIndent(),
        ).withStatus(200),
      ),
    )
  }

  fun notAcceptInvoice() {
    on(
      WireMock.post(urlMatching("/api/v1/document")).willReturn(
        aResponse().withBody(
          """
              {
                "RequestId": "ef25b1e1-e0f2-43b3-8583-e3126cb13b08",
                "Object": null,
                "Type": 1,
                "ErrorValue": {
                  "Value": 4,
                  "Name": "InvalidTaxId"
                },
                "ErrorHumanReadable": null,
                "ValidationErrors": null,
                "IsSuccess": false,
                "IsError": true
              }
          """.trimIndent(),
        ).withStatus(200),
      ),
    )
  }

  fun acceptSecondRequestInvoice() {
    on(
      WireMock.post(urlMatching("/api/v1/document"))
        .inScenario("send_magnifinance_request")
        .willSetStateTo("accept_second_magnifinance_request").willReturn(
          aResponse().withBody(
            """
              {
                "RequestId": "ef25b1e1-e0f2-43b3-8583-e3126cb13b08",
                "Object": null,
                "Type": 1,
                "ErrorValue": {
                  "Value": 4,
                  "Name": "InvalidTaxId"
                },
                "ErrorHumanReadable": null,
                "ValidationErrors": null,
                "IsSuccess": false,
                "IsError": true
              }
            """.trimIndent(),
          ).withStatus(200),
        ),
    )
    on(
      WireMock.post(urlMatching("/api/v1/document"))
        .inScenario("send_magnifinance_request")
        .whenScenarioStateIs("accept_second_magnifinance_request").willReturn(
          aResponse().withBody(
            """
              {
                "RequestId": "98c3bc62-911d-4f14-a5f4-d610c66e8b8b",
                "Object": {
                  "DocumentId": 67195,
                  "ErrorMessage": null
                },
                "Type": 0,
                "ErrorValue": null,
                "ErrorHumanReadable": null,
                "ValidationErrors": null,
                "IsSuccess": true,
                "IsError": false
              }
            """.trimIndent(),
          ).withStatus(200),
        ),
    )
  }

  fun isDown() {
    on(
      WireMock.post(urlMatching("/api/v1/document"))
        .willReturn(
          aResponse().withStatus(500),
        ),
    )
    on(
      WireMock.get(urlMatching("/api/v1/document"))
        .willReturn(
          aResponse().withStatus(500),
        ),
    )
  }
  fun acceptInvoiceWithDuplicateError() {
    on(
      WireMock.post(urlMatching("/api/v1/document")).willReturn(
        aResponse().withBody(
          """
               {
                "RequestId": "a2e2794a-74c5-42b0-858e-27eb7dfdaf2a",
                "Object": null,
                "Type": 1,
                "ErrorValue": {
                  "Value": 14,
                  "Name": "SaveFailed"
                },
                "ErrorHumanReadable": "ValidationError",
                "ValidationErrors": [
                  {
                    "Type": "DocumentIsADuplicate",
                    "ElementNumber": 67195,
                    "Field": "DocumentDetailExternalId",
                    "Detail": "Duplicate Document"
                  }
                ],
                "IsSuccess": false,
                "IsError": true
              }
          """.trimIndent(),
        ).withStatus(200),
      ),
    )
  }

  fun downloadPDF() {
    on(
      WireMock.get(urlMatching("/downloadPDF/.*")).willReturn(
        aResponse().withBody("A PDF").withStatus(200),
      ),
    )
  }

  fun failsDownloadingDocument() {
    on(
      WireMock.get(urlMatching("/downloadPDF/.*")).willReturn(aResponse().withStatus(403)),
    )
  }

  fun getDocument() {
    on(
      WireMock.get(urlMatching("/api/v1/document.*")).willReturn(
        aResponse().withBody(
          """
              {
                "RequestId": "6576a586-0fc0-4cd1-b615-05ccd7ff5a79",
                "Object": {
                  "DocumentNumber": "magnifinance-{{randomValue type='UUID'}}",
                  "DownloadUrl": "http://localhost:${server.port()}/downloadPDF/454766/2022/16d17553-c29a-41db-880a-38ed87c1c979.pdf?X-Amz-Expires=3600&response-cache-control=No-cache&response-content-disposition=attachment%3B%20filename%3DFTZFT_15_MarcTest_2022-12-30_EUR1%252c30.pdf&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAJTHBRPTXFAY7SNOQ/20230111/eu-west-1/s3/aws4_request&X-Amz-Date=20230111T160356Z&X-Amz-SignedHeaders=host&X-Amz-Signature=f0c64d1e862a2e09ec5ecb45263dd71f25096b6fdb5a6b7c124fde82c5e7a520",
                  "PaymentStatus": null,
                  "ErrorMessage": null,
                  "TotalAmount": null,
                  "UnpaidAmount": null,
                  "Details": {
                    "Document": {
                      "DocumentStatusId": "C",
                      "DocumentStatus": "Closed",
                      "LoadDateTime": null,
                      "LoadAddress": null,
                      "DestinationAddress": null,
                      "VehicleLicence": null,
                      "TotalCredit": 0.00,
                      "TotalDebit": 0.0,
                      "Date": "2022-12-30T00:00:00",
                      "DueDate": "2022-12-30T00:00:00",
                      "Description": "",
                      "Type": "I",
                      "Serie": "ZFT",
                      "Id": 148812412,
                      "TaxExemptionReasonCode": null,
                      "DocumentReference": "Z FT100",
                      "Currency": "EUR",
                      "Retention": 0.00,
                      "EuroRate": null,
                      "ExternalId": null,
                      "Lines": [
                        {
                          "Code": "A",
                          "Description": "2022-12-27 Rental",
                          "UnitPrice": 1.06000000,
                          "Quantity": 1.00,
                          "Unit": "aluguer",
                          "Type": "S",
                          "TaxValue": 23.00,
                          "ProductDiscount": 0.0000,
                          "CostCenter": "CC Geral"
                        }
                      ],
                      "PurchaseOrder": ""
                    },
                    "Receiver": {
                      "TaxId": "38131606K",
                      "Name": "Marc Test",
                      "Address": "Cooltra Street",
                      "City": "Barcelona",
                      "PostCode": "08080",
                      "CountryCode": "ES",
                      "PhoneNumber": null,
                      "CountryName": "Espanha",
                      "LegalName": "Marc Test",
                      "Email": null,
                      "IBAN": null
                    },
                    "Totals": {
                      "Amount": 1.303800000000,
                      "AmountText": "1,30",
                      "WithoutTax": 1.0600000000,
                      "WithoutTaxText": "1,06",
                      "Tax": 0.243800000000,
                      "TaxText": "0,24",
                      "Discount": 0.0,
                      "DiscountText": "0,00",
                      "Retention": 0.0,
                      "RetentionText": "0,00",
                      "AmountBeforeDiscountAndTax": 1.0600000000,
                      "AmountBeforeDiscountAndTaxText": "1,06",
                      "TotalAmountWithExchangeRate": 0.0,
                      "TotalAmountWithExchangeRateText": "0,00",
                      "TotalCreditAmount": 1.0600000000,
                      "TotalCreditAmountText": "1,06",
                      "TotalDebitAmount": 0.0,
                      "TotalDebitAmountText": "0,00"
                    }
                  }
                },
                "Type": 0,
                "ErrorValue": null,
                "ErrorHumanReadable": null,
                "ValidationErrors": null,
                "IsSuccess": true,
                "IsError": false
              }
          """.trimIndent(),
        ).withStatus(200),
      ),
    )
  }

  fun rejectsDocument() {
    on(
      WireMock.get(urlMatching("/api/v1/document.*")).willReturn(
        aResponse().withBody(
          """
              {
                  "RequestId": "6345a30a-528c-40c9-8138-c90cd57e7992",
                  "Object": null,
                  "Type": 1,
                  "ErrorValue": {
                      "Value": 45,
                      "Name": "NotAllowedToAccessThisDocument"
                  },
                  "ErrorHumanReadable": "",
                  "ValidationErrors": null,
                  "IsSuccess": false,
                  "IsError": true
              }
          """.trimIndent(),
        ).withStatus(200),
      ),
    )
  }
}
