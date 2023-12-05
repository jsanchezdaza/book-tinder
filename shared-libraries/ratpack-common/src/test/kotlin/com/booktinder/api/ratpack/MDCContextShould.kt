package com.booktinder.api.ratpack

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ThreadContextElement
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.MDC
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * The value of [MDC] context map.
 * See [MDC.getCopyOfContextMap].
 */
typealias MDCContextMap = Map<String, String>?

/**
 * [MDC] context element for [CoroutineContext].
 *
 * Example:
 *
 * ```
 * MDC.put("kotlin", "rocks") // Put a value into the MDC context
 *
 * launch(MDCContext()) {
 *     logger.info { "..." }   // The MDC context contains the mapping here
 * }
 * ```
 *
 * Note that you cannot update MDC context from inside the coroutine simply
 * using [MDC.put]. These updates are going to be lost on the next suspension and
 * reinstalled to the MDC context that was captured or explicitly specified in
 * [contextMap] when this object was created on the next resumption.
 * Use `withContext(MDCContext()) { ... }` to capture updated map of MDC keys and values
 * for the specified block of code.
 *
 * @param contextMap the value of [MDC] context map.
 * Default value is the copy of the current thread's context map that is acquired via
 * [MDC.getCopyOfContextMap].
 */
class MyCustomMDCContext(
  /**
   * The value of [MDC] context map.
   */
  @Suppress("MemberVisibilityCanBePrivate") val contextMap: MDCContextMap = MDC.getCopyOfContextMap(),
) : ThreadContextElement<MDCContextMap>, AbstractCoroutineContextElement(Key) {
  /**
   * Key of [MDCContext] in [CoroutineContext].
   */
  companion object Key : CoroutineContext.Key<MyCustomMDCContext>

  /** @suppress */
  override fun updateThreadContext(context: CoroutineContext): MDCContextMap {
    val oldState = MDC.getCopyOfContextMap()
    setCurrent(contextMap)
    return oldState
  }

  /** @suppress */
  override fun restoreThreadContext(context: CoroutineContext, oldState: MDCContextMap) {
    // we do not restore the state because we would like to keep the MDC values
    // setCurrent(oldState)
  }

  private fun setCurrent(contextMap: MDCContextMap) {
    if (contextMap == null) {
      MDC.clear()
    } else {
      MDC.setContextMap(contextMap)
    }
  }
}

class MDCShould : StringSpec({
  "basic" {
    MDC.put("1", "true")
    MDC.get("1") shouldBe "true"
  }

  "calling a coroutine".config(enabled = false) {
    MDC.put("1", "true")
    launch {
      MDC.get("1") shouldBe "true"
      MDC.put("2", "true")
    }
    MDC.get("1") shouldBe "true"
    MDC.get("2") shouldBe "true"
  }

  "calling a coroutine using context" {
    MDC.put("1", "true")
    withContext(MyCustomMDCContext()) {
      MDC.get("1") shouldBe "true"
      MDC.put("2", "true")
    }
    MDC.get("1") shouldBe "true"
    MDC.get("2") shouldBe "true"
  }
},)
