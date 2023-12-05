package com.booktinder.api.ratpack

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import org.testcontainers.containers.PostgreSQLContainer
import ratpack.jdbctx.Transaction
import javax.sql.DataSource

abstract class RepositoryIntegrationTest(body: StringSpec.(DataSource) -> Unit) :
  StringSpec() {

  private val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:14.7-alpine").apply {
    withDatabaseName("learning-integration-tests")
    withUsername("username")
    withPassword("password")
    withReuse(true)
    start()
  }

  private val dataSource by lazy { dataSource() }

  init {
    body(dataSource)
  }

  override suspend fun beforeSpec(spec: Spec) {
    super.beforeSpec(spec)
    postgreSQLContainer.start()
  }

  override suspend fun afterSpec(spec: Spec) {
    super.afterSpec(spec)
    postgreSQLContainer.stop()
  }

  private fun dataSource(): DataSource {
    val config = HikariConfig()
    config.jdbcUrl = postgreSQLContainer.jdbcUrl
    config.username = postgreSQLContainer.username
    config.password = postgreSQLContainer.password
    return Transaction.dataSource(HikariDataSource(config))
  }
}
