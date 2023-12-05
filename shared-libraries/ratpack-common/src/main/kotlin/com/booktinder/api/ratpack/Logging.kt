package com.booktinder.api.ratpack

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import kotlin.reflect.full.companionObject

fun <R : Any> R.logger(): Lazy<Logger> =
  lazy { LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass).name) }

// unwrap companion class to enclosing class given a Java Class
fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> =
  ofClass.enclosingClass
    ?.takeIf { ofClass.enclosingClass.kotlin.companionObject?.java == ofClass }
    ?: ofClass

fun addToLogContext(key: String, value: Any): Unit = MDC.put(key, value.toString())
