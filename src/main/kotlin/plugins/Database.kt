package com.fittrack.plugins

import com.fittrack.db.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val config = environment.config

    val hikariConfig = HikariConfig().apply {
        jdbcUrl = config.property("database.url").getString()
        username = config.property("database.user").getString()
        password = config.property("database.password").getString()
        maximumPoolSize = config.property("database.maxPoolSize").getString().toInt()
        driverClassName = "org.postgresql.Driver"
        validate()
    }

    Database.connect(HikariDataSource(hikariConfig))

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            UsersTable,
            TrainingDaysTable,
            ExercisesTable,
            SessionLogsTable,
            SetLogsTable
        )
    }
}