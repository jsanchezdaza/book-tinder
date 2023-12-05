package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import io.kotest.core.test.TestCaseOrder
import kotlin.time.Duration.Companion.minutes

object ProjectConfig : AbstractProjectConfig() {
  override val isolationMode = IsolationMode.InstancePerLeaf
  override val testCaseOrder = TestCaseOrder.Sequential
  override val timeout = 10.minutes
}
