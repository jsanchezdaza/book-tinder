package com.cooltra.zeus.ratpack

import com.cooltra.zeus.api.ratpack.yield
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import ratpack.exec.Promise

class RatpackCoroutinesExecShould : StringSpec({

  "return the value from the suspend function on yieldSingle" {
    val result = yieldSingle { functionReturningAValue() }

    result shouldBe "test"
  }

  "throw an expected exception on yieldSingle for a suspend function" {
    shouldThrow<IllegalArgumentException> {
      yieldSingle { functionThrowsException() }
    }
  }

  "be able to call a suspend function on executeSingle" {
    shouldNotThrow<Exception> {
      executeSingle { functionWithoutReturn() }
    }
  }

  "not throw an expected exception on executeSingle for a suspend function" {
    shouldNotThrow<IllegalArgumentException> {
      executeSingle { functionThrowsException() }
    }
  }
},)

private suspend fun functionReturningAValue(): String = Promise.value("test").yield()

private suspend fun functionWithoutReturn() = Promise.value(Unit).yield()

private suspend fun functionThrowsException() =
  Promise.error<IllegalArgumentException>(IllegalArgumentException()).yield()
