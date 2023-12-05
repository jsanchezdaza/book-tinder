package learning.kotlin

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlin.system.measureTimeMillis

class CoroutinesShould : StringSpec({
  "be sequential by default" {
    val elements = mutableListOf<String>()

    val elapsedTime = measureTimeMillis {
      async {
        elements.add(getResultAfter("a", 100))
        elements.add(getResultAfter("b", 100))
      }.await()
    }

    elements shouldBe listOf("a", "b")
    elapsedTime shouldBeGreaterThanOrEqual 200
  }

  "support parallel execution" {
    val elements = mutableListOf<String>()

    val elapsedTime = measureTimeMillis {
      val a = async {
        elements.add(getResultAfter("a", 100))
      }
      val b = async {
        elements.add(getResultAfter("b", 100))
      }
      awaitAll(a, b)
    }

    elements shouldContainAll listOf("a", "b")
    elapsedTime shouldBeLessThan 200
  }
},)

suspend fun getResultAfter(value: String, time: Long): String {
  delay(time)

  return value
}
