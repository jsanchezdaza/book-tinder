package com.cooltra.zeus.stubs

import com.cooltra.zeus.randomIdCardNumber
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aMultipart
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.patch
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.stubbing.Scenario
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import com.google.common.base.Charsets.UTF_8
import com.google.common.io.Resources
import com.google.common.io.Resources.getResource
import io.kotest.core.listeners.AfterSpecListener
import io.kotest.core.listeners.AfterTestListener
import io.kotest.core.listeners.BeforeSpecListener
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult

class AliceStubExtension : BeforeSpecListener, AfterTestListener, AfterSpecListener {
  val aliceStub: AliceStub by lazy { AliceStub.also { it.start() } }

  override suspend fun beforeSpec(spec: Spec) {
    aliceStub
  }

  override suspend fun afterAny(testCase: TestCase, result: TestResult) = aliceStub.reset()
  override suspend fun afterSpec(spec: Spec) = aliceStub.stop()
}

object AliceStub {
  val server: WireMockServer

  init {
    val configuration = WireMockConfiguration.options().dynamicPort()
      .notifier(Slf4jNotifier(false))
    server = WireMockServer(configuration)
    start()
  }

  fun start() {
    server.start()
    defaultMappings()
  }

  private fun defaultMappings() {
    stubValidIdCard()
    stubValidDriverLicense()
    stubValidSelfie()
  }

  fun stop() {
    server.stop()
  }

  fun port(): Int = server.port()

  fun reset() {
    server.resetAll()
    defaultMappings()
  }

  private fun stubValidIdCard() {
    on(
      get(urlMatching("/onboarding/login_token"))
        .willReturnJson("""{"token": "LoginToken"}"""),
    )
    on(
      get(urlMatching("/onboarding/backend_token"))
        .willReturnJson("""{"token": "BackendToken"}"""),
    )
    on(
      post(urlMatching("/onboarding/user"))
        .withMultipartRequestBody(
          aMultipart()
            .withName("first_name")
            .withName("last_name")
            .withName("email"),
        )
        .willReturnJson("""{"user_id": "alice-user-uuid"}"""),
    )
    on(
      get(urlMatching("/onboarding/user_token/(.*)"))
        .willReturnJson("""{"token": "UserToken"}"""),
    )
    on(
      get(urlMatching("/onboarding/backend_token/(.*)"))
        .willReturnJson("""{"token": "UserBackendToken"}"""),
    )
    on(
      post(urlMatching("/onboarding/user/document"))
        .withMultipartRequestBody(
          aMultipart()
            .withName("issuing_country")
            .withName("type")
            .withBody(containing("idcard")),
        )
        .willReturnJson("""{"document_id": "idcard-document-id"}"""),
    )
    on(
      put(urlMatching("/onboarding/user/document"))
        .withMultipartRequestBody(
          aMultipart()
            .withName("document_id")
            .withName("side")
            .withName("manual")
            .withName("source")
            .withName("image"),
        )
        .willReturnJson("""{"message": "Given document was added successfully"}"""),
    )
    on(
      patch(urlMatching("/onboarding/user/document/(.*)"))
        .willReturnJson("""{"message": "OK. The document has been voided"}"""),
    )
    on(
      patch(urlMatching("/onboarding/user/selfie"))
        .willReturnJson("""{"message": "OK. The selfie has been voided"}"""),
    )
    stubIdCardType("FRA", "ID")
  }

  fun stubInvalidIdCard() = userReportWith(content("report-invalid-id_number-response"))

  fun stubPassport(country: String = "FRA") {
    on(
      post(urlMatching("/onboarding/user/document"))
        .withMultipartRequestBody(
          aMultipart()
            .withName("issuing_country")
            .withName("type")
            .withBody(containing("passport")),
        )
        .willReturnJson("""{"document_id": "passport-document-id"}"""),
    )

    on(
      getUserReport()
        .inScenario("Passport")
        .whenScenarioStateIs(Scenario.STARTED)
        .willSetStateTo("idCard failed")
        .willReturnJson(content("report-idcard-not-read-response")),
    )
    on(
      getUserReport()
        .inScenario("Passport")
        .whenScenarioStateIs("idCard failed")
        .willReturnJson(content("report-passport-response").country(country)),
    )
  }

  fun stubCreatesDocumentForResidentPermit() {
    on(
      post(urlMatching("/onboarding/user/document"))
        .withMultipartRequestBody(
          aMultipart()
            .withName("issuing_country")
            .withName("type")
            .withBody(containing("residencepermit")),
        )
        .willReturnJson("""{"document_id": "residencepermit-document-id"}"""),
    )
  }

  fun stubResidentPermit(country: String, residencePermit: String) {
    on(
      getUserReport()
        .inScenario("Residence permit")
        .whenScenarioStateIs(Scenario.STARTED)
        .willSetStateTo("residence permit")
        .willReturnJson(
          content("report-idcard-residence-permit-response")
            .country(country)
            .residencePermitType(residencePermit),
        ),
    )

    on(
      getUserReport()
        .inScenario("Residence permit")
        .whenScenarioStateIs("residence permit")
        .willReturnJson(
          content("report-residence-permit-response")
            .country(country)
            .residencePermitType(residencePermit),
        ),
    )
  }

  fun stubIdCardType(country: String, idCardType: String, token: String? = null) = userReportWith(
    content("report-valid-response")
      .country(country)
      .idCardType(idCardType),
    token,
  )

  fun stubIdCard(idCardNumber: String) = userReportWith(
    Resources.toString(getResource("alice/report-valid-response.json"), UTF_8)
      .country("FRA")
      .idCardType("ID")
      .idCardNumber(idCardNumber),
    null,
  )

  fun stubReportWithSevenDocuments() = userReportWith(content("report-response-with-seven-documents"))

  fun stubTessera() {
    on(
      post(urlMatching("/onboarding/user/document"))
        .withMultipartRequestBody(
          aMultipart()
            .withName("issuing_country")
            .withName("type")
            .withBody(containing("healthinsurancecard")),
        )
        .willReturnJson("""{"document_id": "healthinsurancecard-document-id"}"""),
    )
    on(
      getUserReport()
        .inScenario("Tessera")
        .whenScenarioStateIs(Scenario.STARTED)
        .willSetStateTo("idCard failed")
        .willReturnJson(content("report-idcard-invalid-response")),
    )
    on(
      getUserReport()
        .inScenario("Tessera")
        .whenScenarioStateIs("idCard failed")
        .willReturnJson(content("report-tessera-response")),
    )
  }

  private fun stubValidDriverLicense() {
    on(
      post(urlMatching("/onboarding/user/document"))
        .withMultipartRequestBody(
          aMultipart()
            .withName("type")
            .withBody(containing("driverlicense")),
        )
        .willReturnJson("""{"document_id": "driving-license-document-id"}"""),
    )
  }

  fun stubInvalidDriverLicense() = userReportWith(content("report-invalid-expired_driver_license-response"))

  fun stubInvalidSelfie() = userReportWith(content("report-invalid-selfie-response"))

  fun stubValidIdentityCardAndSelfie() = userReportWith(content("report-user-without-driver-license"))

  fun stubValidIdentityCardAndSelfieNotRecognisedDriverLicense() = userReportWith(content("report-user-not-recognized-driver-license"))

  private fun stubValidSelfie() {
    on(
      post(urlMatching("/onboarding/user/selfie"))
        .withMultipartRequestBody(
          aMultipart()
            .withName("video"),
        )
        .willReturnJson("""{"message": "Selfie uploaded"}"""),
    )
  }

  private fun userReportWith(content: String, token: String? = null) {
    val userReport = getUserReport()
    if (token != null) {
      userReport.withHeader("Authorization", equalTo("Bearer $token"))
    }
    on(userReport.willReturnJson(content))
  }

  private fun getUserReport() = get(urlMatching("/onboarding/user/report"))

  private fun getUserReportV0() = get(urlMatching("/onboarding/user/report"))
    .withHeader("Alice-Report-Version", equalTo("0"))

  private fun on(mapping: StubMapping) = server.addStubMapping(mapping)

  private fun json() = WireMock.ok().withHeader("Content-Type", "application/json")

  private fun MappingBuilder.willReturnJson(content: String) = this.willReturn(json().withBody(content)).build()

  private fun content(fileName: String, idCardNumber: String = randomIdCardNumber()): String =
    Resources.toString(getResource("alice/$fileName.json"), UTF_8)
      .idCardNumber(idCardNumber)

  private fun String.country(value: String): String = this.replace("{stub.country}", value)

  private fun String.residencePermitType(value: String): String = this.replace("{stub.residence_permit.type}", value)

  private fun String.idCardType(value: String): String = this.replace("{stub.id_card.type}", value)

  private fun String.idCardNumber(value: String): String = this.replace("{stub.id_card.number}", value)

  fun stubGetUser(email: String, aliceId: String) {
    on(
      post(urlMatching("/onboarding/user"))
        .withHeader("content-type", containing("multipart/form-data"))
        .withMultipartRequestBody(
          aMultipart().withName("email").withBody(containing(email)),
        )
        .willReturnJson("""{"user_id": "$aliceId"}"""),
    )
  }

  fun stubGetBackendToken(aliceId: String, token: String) {
    on(
      get(urlMatching("/onboarding/backend_token/$aliceId"))
        .willReturnJson("""{"token": "$token"}"""),
    )
  }

  fun stubGetUserToken(aliceId: String, token: String) {
    on(
      get(urlMatching("/onboarding/user_token/$aliceId"))
        .willReturnJson("""{"token": "$token"}"""),
    )
  }

  fun getUserReportWith(
    file: String,
    idCardNumber: String,
  ) {
    val userReport = getUserReportV0()
    val content = content(file, idCardNumber)
    on(userReport.willReturnJson(content))
  }
}
