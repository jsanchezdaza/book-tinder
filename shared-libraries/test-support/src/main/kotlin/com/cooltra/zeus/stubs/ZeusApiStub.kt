package com.cooltra.zeus.stubs

import com.cooltra.zeus.api.ratpack.JacksonModule
import com.cooltra.zeus.domain.Country
import com.cooltra.zeus.domain.HomeSystem
import com.cooltra.zeus.domain.State
import com.cooltra.zeus.domain.UserType
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.patch
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import ratpack.http.Status
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit

@Suppress("UnstableApiUsage")
class ZeusApiStub(configuration: WireMockConfiguration = WireMockConfiguration.options().dynamicPort()) {

  val server: WireMockServer

  init {
    configuration.notifier(Slf4jNotifier(false))
    server = WireMockServer(configuration)
  }

  fun start(): ZeusApiStub {
    server.start()
    defaultMappings()
    return this
  }

  private fun defaultMappings() {
    stubReject(Status.OK, "")
    stubUploadDocumentImage()
    stubUpdateVehicleStatus()
    stubDefaultInternalUser(InternalUserExample(activatedAt = Instant.now().minus(8, ChronoUnit.DAYS)))
  }

  private fun stubDefaultInternalUser(user: InternalUserExample = InternalUserExample()) {
    on(
      get(urlMatching("/internal/users/.+"))
        .willReturn(aResponse().withStatus(200).withBody(user.toJson())),

    )
  }

  fun stubInternalUser(user: InternalUserExample = InternalUserExample()) {
    val userIdPattern = user.userId ?: ".+"
    on(
      get(urlMatching("/internal/users/$userIdPattern"))
        .willReturn(aResponse().withStatus(200).withBody(user.toJson())),
    )
  }

  fun stubInternalUserNotFound() {
    on(
      get(urlMatching("/internal/users/.+"))
        .willReturn(
          aResponse().withStatus(404),
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

  fun stubReject(statusCode: Status, body: String) {
    on(
      post(urlMatching("/users/.+/reject"))
        .willReturn(aResponse().withStatus(statusCode.code).withBody(body)),
    )
  }

  fun stubActivate(statusCode: Status, body: String) {
    on(
      post(urlMatching("/users/.+/activate"))
        .willReturn(
          aResponse().withStatus(statusCode.code).withBody(body),
        ),
    )
  }

  fun stubRequestCorrections(statusCode: Status, body: String) {
    on(
      post(urlMatching("/users/.+/request-changes"))
        .willReturn(
          aResponse().withStatus(statusCode.code).withBody(body),
        ),
    )
  }

  fun stubUpdateUserDetails(statusCode: Status, body: String = "") {
    on(
      patch(urlMatching("/users/.+/details"))
        .willReturn(
          aResponse().withStatus(statusCode.code).withBody(body),
        ),
    )
  }

  fun stubDeleteUser(statusCode: Status) {
    on(
      delete(urlMatching("/users/.+")).willReturn(
        aResponse().withStatus(statusCode.code),
      ),
    )
  }

  fun stubUpdateUserBillingAddress(statusCode: Status, body: String = "") {
    on(
      patch(urlMatching("/users/.+/billing-address"))
        .willReturn(
          aResponse().withStatus(statusCode.code).withBody(body),
        ),
    )
  }

  fun stubUpdateUserDocuments(statusCode: Status, body: String = "") {
    on(
      patch(urlMatching("/users/.+/documents"))
        .willReturn(
          aResponse().withStatus(statusCode.code).withBody(body),
        ),
    )
  }

  fun stubUploadDocumentImage() {
    on(
      post(urlMatching("/images/documents"))
        .withHeader("Content-Type", equalTo("multipart/form-data; boundary=test")).willReturn(
          aResponse().withStatus(200).withBody("""{ "id": "imageId" }"""),
        ),
    )
  }

  fun stubUpdateVehicleStatus() {
    on(
      post(urlMatching("/vehicles/.+/telematics-data")).willReturn(aResponse().withStatus(Status.CREATED.code)),
    )
  }
}

data class InternalUserExample(
  val userId: String? = null,
  val hasDriverLicense: Boolean = true,
  val state: State = State.ACTIVATED,
  val activatedAt: Instant? = Instant.now(),
  val taxIdCountry: String? = "ES",
  val taxId: String? = null,
  val firstName: String = "firstName",
  val lastName: String = "lastName",
  val address: AddressExample = AddressExample(),
  val billingAddress: BillingAddressExample? = null,
  val homeSystem: HomeSystem = HomeSystem.BARCELONA,
  val provider: String = UserType.COOLTRA.name,
) {

  data class AddressExample(
    val street: String = "street 12",
    val city: String = "city",
    val postCode: String = "08080",
    val country: Country = Country("FR"),
  ) {
    fun toJsonNode(): ObjectNode = JacksonModule.OBJECT_MAPPER.createObjectNode().apply {
      put("street", street)
      put("city", city)
      put("postalCode", postCode)
      put("country", country.iso2Code)
    }
  }

  data class BillingAddressExample(
    val name: String = "Billing address name",
    val street: String = "street 12",
    val city: String = "city",
    val postCode: String = "08080",
    val country: Country = Country("FR"),
  ) {
    fun toJsonNode(): ObjectNode = JacksonModule.OBJECT_MAPPER.createObjectNode().apply {
      put("name", name)
      put("street", street)
      put("city", city)
      put("postalCode", postCode)
      put("country", country.iso2Code)
    }
  }

  fun toJson(): String {
    val jsonNode = JacksonModule.OBJECT_MAPPER.createObjectNode()
    jsonNode.put("name", "$firstName $lastName")
    jsonNode.put("firstName", firstName)
    jsonNode.put("lastName", lastName)
    jsonNode.put("hasDriverLicense", hasDriverLicense)
    jsonNode.put("state", state.name)
    jsonNode.put("cardNumber", "123456")
    jsonNode.put("taxId", taxId)
    jsonNode.put("taxIdCountry", taxIdCountry)
    jsonNode.put(
      "activatedAt",
      if (state == State.ACTIVATED) LocalDateTime.ofInstant(activatedAt, UTC).toString() else null,
    )
    jsonNode.replace("address", address.toJsonNode())
    if (billingAddress != null) {
      jsonNode.replace("billingAddress", billingAddress.toJsonNode())
    } else {
      jsonNode.putNull("billingAddress")
    }
    jsonNode.put("provider", provider)

    return jsonNode.toPrettyString()
  }
}
