package com.cooltra.zeus.kogiven

import com.cooltra.zeus.api.ratpack.App
import com.cooltra.zeus.ratpack.AcceptanceErrorHandler
import ratpack.guice.BindingsImposition
import ratpack.guice.BindingsSpec
import ratpack.impose.ForceDevelopmentImposition
import ratpack.impose.ImpositionsSpec
import ratpack.impose.ServerConfigImposition
import ratpack.impose.UserRegistryImposition
import ratpack.registry.Registry
import ratpack.registry.RegistrySpec
import ratpack.server.BaseDir
import ratpack.server.ServerConfigBuilder
import ratpack.test.MainClassApplicationUnderTest
import java.net.URISyntaxException
import java.net.URL

open class Application(mainClass: App) : MainClassApplicationUnderTest(mainClass.javaClass) {
  lateinit var registry: Registry
  private val appName: String

  init {
    appName = mainClass.name
    address
  }

  override fun addImpositions(impositions: ImpositionsSpec) {
    impositions.add(ForceDevelopmentImposition.of(false))
    impositions.add(
      UserRegistryImposition.of { r ->
        val extraRegistry = Registry.of { spec -> addRegistryImposition(spec) }
        registry = r.join(extraRegistry)
        extraRegistry
      },
    )

    impositions.add(
      ServerConfigImposition.of { serverConfigBuilder ->
        serverConfigBuilder.baseDir(BaseDir.find("application.yaml"))
          .yaml(findConfig("application-component-test.yaml"))
          .sysProps()
          .env()
        addServerConfigImpositions(serverConfigBuilder)
      },
    )

    impositions.add(
      BindingsImposition.of { bindingSpec ->
        addBindingImpositions(bindingSpec)
        bindingSpec
      },
    )
  }

  private fun findConfig(filename: String): URL {
    try {
      return Thread.currentThread().contextClassLoader
        .getResources(filename)
        .toList()
        .find { url -> url.path.contains(appName) } as URL
    } catch (e: URISyntaxException) {
      throw IllegalArgumentException("Error reading properties", e)
    }
  }

  protected open fun addServerConfigImpositions(spec: ServerConfigBuilder) {}

  protected open fun addBindingImpositions(spec: BindingsSpec) {}

  protected open fun addRegistryImposition(spec: RegistrySpec) {
    spec.add(AcceptanceErrorHandler())
  }
}
