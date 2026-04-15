package com.fittrack

import com.fittrack.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    configureSerialization()
    configureDatabase()
    configureSecurity()
    configureStatusPages()
    configureRouting()
}