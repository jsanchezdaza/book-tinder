package com.cooltra.zeus.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jdbi.v3.core.Jdbi
import javax.sql.DataSource

object Connection {

  fun createJdbi(dataSourceUrl: String, username: String, password: String): Jdbi {
    return Jdbi.create(createDataSource(dataSourceUrl, username, password))
  }

  private fun createDataSource(dataSourceUrl: String, username: String, password: String): DataSource {
    val hikariConfig = HikariConfig()
    hikariConfig.jdbcUrl = dataSourceUrl
    hikariConfig.username = username
    hikariConfig.password = password
    return HikariDataSource(hikariConfig)
  }
}
