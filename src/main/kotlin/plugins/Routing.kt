package com.fittrack.plugins

import com.fittrack.repository.*
import com.fittrack.routing.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(CallLogging)

    val userRepo = UserRepository()
    val programRepo = ProgramRepository()
    val sessionRepo = SessionRepository()

    routing {
        route("/api/v1") {
            authRoutes(userRepo)
            programRoutes(programRepo)
            sessionRoutes(sessionRepo)
        }
    }
}