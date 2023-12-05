package learning.ratpack

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import ratpack.exec.Blocking
import ratpack.exec.Promise
import ratpack.exec.util.retry.AttemptRetryPolicy
import ratpack.exec.util.retry.FixedDelay
import ratpack.exec.util.retry.IndexedDelay
import ratpack.exec.util.retry.RetryPolicy
import ratpack.test.exec.ExecHarness
import java.time.Duration

class PromiseRetryShould : StringSpec({

  "Retry 3 attempts with backoff" {
    val retryPolicy: RetryPolicy = AttemptRetryPolicy
      .of {
        it.delay(
          IndexedDelay.of { i: Int ->
            Duration.ofMillis(5).multipliedBy(i.toLong())
          },
        )
          .maxAttempts(3)
      }

    var time: Duration = Duration.ZERO

    ExecHarness.yieldSingle {
      Blocking.get {
        if (!retryPolicy.isExhausted) {
          throw NullPointerException()
        }
        Promise.value("a")
      }.retry(retryPolicy) { i, _ ->
        println("This is the $i")
      }.time { time = it }
    }

    time.toMillis() shouldBeGreaterThan 30
  }

  "Fail when limit surpassed of retries" {
    val retryPolicy: RetryPolicy = AttemptRetryPolicy.of { it.delay(FixedDelay.of(Duration.ofMillis(10))).maxAttempts(2) }

    shouldThrow<NullPointerException> {
      ExecHarness.yieldSingle {
        Promise.async<String> {
          throw NullPointerException()
        }.retry(retryPolicy) { i, _ ->
          println("This is the $i")
        }
      }.valueOrThrow
    }

    retryPolicy.isExhausted shouldBe true
  }

  "Respect retry with promises hierarchy" {
    val retryOnce: RetryPolicy = AttemptRetryPolicy.of { it.delay(FixedDelay.of(Duration.ofMillis(10))).maxAttempts(1) }

    ExecHarness.yieldSingle {
      Promise.async<String> {
        println("FIRST")
        it.success("a")
      }.flatMap {
        Blocking.get {
          println("INNER")
          if (!retryOnce.isExhausted) {
            throw IllegalStateException()
          }
          "PACO"
        }.retry(retryOnce) { _, _ -> }
      }
    }.valueOrThrow shouldBe "PACO"
  }

  "Use all retries" {
    val retryTwice: RetryPolicy = AttemptRetryPolicy.of { it.delay(FixedDelay.of(Duration.ofMillis(10))).maxAttempts(2) }
    val retryOnce: RetryPolicy = AttemptRetryPolicy.of { it.delay(FixedDelay.of(Duration.ofMillis(10))).maxAttempts(1) }

    shouldThrow<IllegalStateException> {
      ExecHarness.yieldSingle {
        Promise.async<String> {
          throw IllegalStateException()
        }.retry(retryTwice) { i, _ ->
          println("This is the A $i")
        }.retry(retryOnce) { i, _ ->
          println("This is the B $i")
        }
      }.valueOrThrow
    }

    retryTwice.isExhausted shouldBe true
    retryOnce.isExhausted shouldBe true
  }
},)
