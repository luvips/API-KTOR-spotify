package com.spotify.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.spotify.repository.Users
import com.spotify.repository.Artists

fun Application.configureDatabases() {
    val config = environment.config.config("storage")
    val driverClassName = config.property("driverClassName").getString()
    val jdbcUrl = config.property("jdbcUrl").getString()
    val username = config.property("username").getString()
    val password = config.property("password").getString()

    val hikariConfig = HikariConfig().apply {
        this.driverClassName = driverClassName
        this.jdbcUrl = jdbcUrl
        this.username = username
        this.password = password
        maximumPoolSize = 3
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val database = Database.connect(HikariDataSource(hikariConfig))

    // Crear tabla Users si no existe
    transaction(database) {
        SchemaUtils.create(Artists)
        SchemaUtils.create(Users)
    }
}