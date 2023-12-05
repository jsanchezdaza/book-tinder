package com.booktinder.api.ratpack.metrics

import com.google.inject.AbstractModule
import com.google.inject.Provides
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.core.instrument.config.MeterFilter
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import javax.inject.Singleton

data class MetricsConfig @JvmOverloads constructor(
  val application: String,
  val groups: Map<String, String> = mapOf(),
  val usePathBindings: Boolean = true,
)

class MetricsModule : AbstractModule() {

  override fun configure() {
    bind(RequestTimingHandler::class.java)
    bind(MetricsHandler::class.java)
  }

  @Singleton
  @Provides
  fun prometheusMeterRegistry(config: MetricsConfig): PrometheusMeterRegistry {
    val prometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    prometheusMeterRegistry.config().meterFilter(
      object : MeterFilter {
        override fun configure(id: Meter.Id, config: DistributionStatisticConfig) =
          DistributionStatisticConfig.builder()
            .serviceLevelObjectives(50.0, 95.0, 99.0)
            .build()
            .merge(config)
      },
    )
    prometheusMeterRegistry.config().commonTags(
      "application",
      config.application,
      "environment",
      System.getenv("COPILOT_ENVIRONMENT_NAME") ?: "local",
    )

    ClassLoaderMetrics().bindTo(prometheusMeterRegistry)
    JvmMemoryMetrics().bindTo(prometheusMeterRegistry)
    JvmGcMetrics().bindTo(prometheusMeterRegistry)
    JvmThreadMetrics().bindTo(prometheusMeterRegistry)
    ProcessorMetrics().bindTo(prometheusMeterRegistry)
    // TODO review the issue with LogbackMetrics that makes acceptance run by kotest to fail
    // LogbackMetrics().bindTo(prometheusMeterRegistry)
    UptimeMetrics().bindTo(prometheusMeterRegistry)

    publishVersion(prometheusMeterRegistry)

    return prometheusMeterRegistry
  }

  private fun publishVersion(meterRegistry: PrometheusMeterRegistry) {
    val version: String = javaClass.`package`?.implementationVersion ?: "dev"
    Counter.builder("app.version").tag("version", version).register(meterRegistry).increment()
  }
}
