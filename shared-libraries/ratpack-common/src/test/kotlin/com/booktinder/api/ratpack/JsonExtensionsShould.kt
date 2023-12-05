package com.booktinder.api.ratpack

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class JsonExtensionsShould : StringSpec({
  "return null when boolean node does not exist" {
    val jsonNode = ObjectMapper().createObjectNode()

    jsonNode.asBooleanOrNull() shouldBe null
  }

  "return null when boolean node does not contain a boolean value" {
    val jsonNode = ObjectMapper().createObjectNode()
    jsonNode.put("node", "invalid")

    jsonNode["node"].asBooleanOrNull() shouldBe null
  }

  "return boolean value" {
    val jsonNode = ObjectMapper().createObjectNode()
    jsonNode.put("node", "true")

    jsonNode["node"].asBooleanOrNull() shouldBe true
  }
},)
