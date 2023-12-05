package com.cooltra.zeus.stubs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.patch
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import ratpack.http.Status
import ratpack.http.Status.NO_CONTENT
import ratpack.http.Status.UNPROCESSABLE_ENTITY

class AuthStub(configuration: WireMockConfiguration = WireMockConfiguration.options().dynamicPort()) {

  val server: WireMockServer

  init {
    configuration
      .notifier(Slf4jNotifier(false))
    server = WireMockServer(configuration)
  }

  fun start() {
    server.start()
    defaultMappings()
  }

  fun stop() {
    server.stop()
  }

  fun reset() {
    server.resetAll()
    defaultMappings()
  }

  private fun defaultMappings() {
    operatorUpdateEmail()
    deleteUser()
    signUpFirebase()
    disableCredentials()
  }

  private fun deleteUser() {
    server.addStubMapping(
      delete(urlMatching("/operators/accounts"))
        .willReturn(aResponse().withStatus(204))
        .build(),
    )
  }

  private fun operatorUpdateEmail() {
    server.addStubMapping(
      post(urlEqualTo("/operators/accounts/update-email"))
        .willReturn(aResponse().withStatus(204))
        .build(),
    )
  }

  fun operatorUpdateEmailReturnsError() {
    server.addStubMapping(
      post(urlEqualTo("/operators/accounts/update-email"))
        .willReturn(aResponse().withStatus(500))
        .build(),
    )
  }

  fun userUpdateEmailReturnsUnprocessableEntity() {
    server.addStubMapping(
      patch(urlEqualTo("/accounts"))
        .willReturn(
          aResponse()
            .withStatus(422)
            .withBody("""{ "error": "data_invalid", "details": { "current_password": ["is_invalid"] } }"""),
        )
        .build(),
    )
  }

  fun signUpFirebase() {
    server.addStubMapping(
      post(urlEqualTo("/oauth/sign-up"))
        .withRequestBody(matchingJsonPath("username"))
        .withRequestBody(matchingJsonPath("password"))
        .withRequestBody(matchingJsonPath("user_id"))
        .willReturn(
          aResponse().withStatus(200)
            .withBody(
              """{
          "refresh_token" : "zeus::firebase-refresh-token",
          "access_token" : "firebase-access-token"
          }
              """.trimIndent(),
            ),
        )
        .build(),
    )
  }

  fun deleteUserError() {
    server.addStubMapping(
      delete(urlMatching("/operators/accounts"))
        .willReturn(aResponse().withStatus(500))
        .build(),
    )
  }

  fun stubGetUserSessionsByEmail(status: Status, body: String) {
    server.addStubMapping(
      post(urlMatching("/accounts/get-sessions"))
        .willReturn(
          aResponse()
            .withBody(body)
            .withStatus(status.code),
        )
        .build(),
    )
  }

  fun revokeSessions() {
    server.addStubMapping(
      post(urlMatching("/operators/accounts/revoke-sessions"))
        .willReturn(aResponse().withStatus(NO_CONTENT.code))
        .build(),
    )
  }

  fun revokeSessionsFails() {
    server.addStubMapping(
      post(urlMatching("/operators/accounts/revoke-sessions"))
        .willReturn(
          aResponse()
            .withStatus(UNPROCESSABLE_ENTITY.code)
            .withHeader("Content-Type", "application/json")
            .withBody("""{ "error": "user_not_found" }"""),
        )
        .build(),
    )
  }

  fun disableCredentials() {
    server.addStubMapping(
      post(urlMatching("/operators/accounts/disable-credentials"))
        .willReturn(aResponse().withStatus(NO_CONTENT.code))
        .build(),
    )
  }

  fun disableCredentialsFails() {
    server.addStubMapping(
      post(urlMatching("/operators/accounts/disable-credentials"))
        .willReturn(
          aResponse()
            .withStatus(UNPROCESSABLE_ENTITY.code)
            .withHeader("Content-Type", "application/json")
            .withBody("""{ "error": "user_not_found" }"""),
        )
        .build(),
    )
  }
}
