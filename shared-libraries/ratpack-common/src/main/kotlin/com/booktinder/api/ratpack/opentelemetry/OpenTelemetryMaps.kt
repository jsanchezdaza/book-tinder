package com.booktinder.api.ratpack.opentelemetry

import io.opentelemetry.context.propagation.TextMapSetter

object MapCustomSetter : TextMapSetter<MutableMap<String, String>> {
  override fun set(carrier: MutableMap<String, String>?, key: String, value: String) {
    carrier?.put(key, value)
  }
}

object MapSetter : TextMapSetter<MutableMap<String, Any>> {

  override fun set(carrier: MutableMap<String, Any>?, key: String, value: String) {
    carrier?.put(key, value)
  }
}
