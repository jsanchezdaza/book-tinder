package com.booktinder.api.ratpack.db

class DataSourceConfig(
  val dataSourceUrl: String,
  val username: String,
  val password: String?,
)

class ReadWriteDataSourceConfig(
  val readDataSourceUrl: String,
  val writeDataSourceUrl: String,
  val username: String,
  val password: String?,
)

class FlywayDataSourceConfig(
  val dataSourceUrl: String,
  val username: String,
  val password: String?,
)
