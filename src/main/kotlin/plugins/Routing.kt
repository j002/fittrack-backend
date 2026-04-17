package com.fittrack.plugins

import com.fittrack.repository.ProgramRepository
import com.fittrack.repository.SessionRepository
import com.fittrack.repository.UserRepository
import com.fittrack.routing.authRoutes
import com.fittrack.routing.programRoutes
import com.fittrack.routing.sessionRoutes
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