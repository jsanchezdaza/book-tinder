package com.booktinder.api.ratpack

import com.booktinder.extensions.toUUID
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.text.SimpleDateFormat
import java.util.UUID

object JacksonModule {

  @JvmField
  val OBJECT_MAPPER: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .registerModule(JavaTimeModule())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    .setDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))

  @JvmField
  val OBJECT_MAPPER_WITHOUT_NULLS: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .registerModule(JavaTimeModule())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    .setSerializationInclusion(Include.NON_NULL)
    .setDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))

  @JvmField
  val OBJECT_MAPPER_SNAKE_CASE: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .registerModule(JavaTimeModule())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    .setDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))

  fun <T : Any> T.toJson(): String = OBJECT_MAPPER.writeValueAsString(this)

  inline fun <reified T : Any> String.from(): T = OBJECT_MAPPER.readValue(this, T::class.java)
  inline fun <reified T : Any> String.fromSnakeCase(): T = OBJECT_MAPPER_SNAKE_CASE.readValue(this, T::class.java)
  inline fun <reified T : List<*>> String.fromList(): T = OBJECT_MAPPER.readValue(this, object : TypeReference<T>() {})
  inline fun <reified T : Map<Any, Any>> String.toMap(): T =
    OBJECT_MAPPER.readValue(this, object : TypeReference<T>() {})

  fun createSnakeCaseObjectNode(): ObjectNode = OBJECT_MAPPER_SNAKE_CASE.createObjectNode()
  fun createSnakeCaseArrayNode(): ArrayNode = OBJECT_MAPPER_SNAKE_CASE.createArrayNode()

  fun <T : Any> T.toSnakeCaseJson(): String = OBJECT_MAPPER_SNAKE_CASE.writeValueAsString(this)
  inline fun <reified T : Any> String.fromSnakeCaseJson(): T = OBJECT_MAPPER_SNAKE_CASE.readValue(this, T::class.java)

  fun JsonNode.asUUID(): UUID = asText().toUUID()
}
