package com.izida.backend

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

object Database {
    private val dataSource: HikariDataSource by lazy {
        val config = HikariConfig().apply {
            jdbcUrl = required("DATABASE_URL")
            username = required("DATABASE_USER")
            password = required("DATABASE_PASSWORD")
            maximumPoolSize = (System.getenv("DATABASE_POOL_SIZE") ?: "10").toInt()
            isAutoCommit = false
        }
        HikariDataSource(config)
    }
    fun connection() = dataSource.connection
    private fun required(name: String): String =
        System.getenv(name)?.takeIf { it.isNotBlank() } ?: error("Missing environment variable: $name")
}
