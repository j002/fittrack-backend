package com.fittrack.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

class NotFoundException(message: String) : Exception(message)
class ConflictException(message: String) : Exception(message)
class BadRequestException(message: String) : Exception(message)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, mapOf("error" to cause.message))
        }
        exception<ConflictException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, mapOf("error" to cause.message))
        }
        exception<BadRequestException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.message))
        }
        exception<Throwable> { call, _ ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Erreur interne"))
        }
    }
}