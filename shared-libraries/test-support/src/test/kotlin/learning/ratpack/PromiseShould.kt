package learning.ratpack

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import ratpack.exec.Blocking
import ratpack.exec.Operation
import ratpack.exec.Promise
import ratpack.test.exec.ExecHarness

class PromiseShould : StringSpec({

  "next requires to be subscribed" {
    val elements = mutableListOf<String>()
    ExecHarness.yieldSingle {
      Promise.value("a").next {
        elements.add(it)
        Promise.async<String> { down ->
          elements.add("b")
          down.success("b")
        }
      }.flatMap {
        elements.add("c")
        Promise.value("c")
      }
    }

    elements shouldBe listOf("a", "c")
  }

  "next requires to be subscribed  to be triggered" {
    val elements = mutableListOf<String>()
    ExecHarness.yieldSingle {
      Promise.value("a").next {
        elements.add(it)
        Promise.async<String> { down ->
          elements.add("b")
          down.success("b")
        }.then { }
      }.flatMap {
        elements.add("c")
        Promise.value("c")
      }
    }

    elements shouldBe listOf("a", "b", "c")
  }

  "catch the exception when there is an on error" {
    val elements = mutableListOf<String>()
    ExecHarness.yieldSingle {
      Blocking.get {
        elements.add("a")
      }.flatMap {
        Promise.value("b")
      }.next {
        Operation.of {
          elements.add(it + 1)
          throw IllegalStateException()
        }.onError { }.then()
      }.next {
        elements.add(it + 2)
      }
    }.valueOrThrow

    elements shouldBe listOf("a", "b1", "b2")
  }

  "on null skip next actions" {
    val elements = mutableListOf<String>()

    ExecHarness.yieldSingle {
      Promise.sync { null }.onNull { elements.add("1") }.map { elements.add("2") }
    }.valueOrThrow

    elements shouldBe listOf("1")
  }

  "flatRight is subscribed" {
    val elements = mutableListOf<String>()

    ExecHarness.yieldSingle {
      Promise.sync { null }.onNull { elements.add("1") }.map { elements.add("2") }
    }.valueOrThrow

    elements shouldBe listOf("1")
  }

  "must handle the onError if you apply then() in a nested Promise to stop the execution" {
    val elements = mutableListOf<String>()
    shouldThrow<Exception> {
      ExecHarness.yieldSingle {
        Promise.value("").map {
          Blocking.get {
            elements.add("1")
          }.flatMap {
            Promise.error<Exception>(Exception())
          }.onError {
            throw it
          }.then { }
        }.flatMap {
          Blocking.get { elements.add("2") }
        }
      }.valueOrThrow
    }
    elements.size shouldBe 1
  }

  "onError would handle error and continue" {
    val elements = mutableListOf<String>()
    ExecHarness.yieldSingle {
      Promise.value("")
        .next {
          elements.add("1")
          Promise.error<Exception>(Exception()).onError { elements.add("2") }.then {}
        }.flatMap {
          elements.add("3")
          Promise.value("ok")
        }
    }.valueOrThrow

    elements.size shouldBe 3
  }
},)
