package com.fittrack.routing

import com.fittrack.model.SessionLogRequest
import com.fittrack.repository.SessionRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.sessionRoutes(sessionRepo: SessionRepository) {
    authenticate("auth-jwt") {
        route("/sessions") {
            get {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                call.respond(sessionRepo.getSessions(userId))
            }
            post {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val req = call.receive<SessionLogRequest>()
                call.respond(HttpStatusCode.Created, sessionRepo.logSession(userId, req))
            }
            get("/{id}") {
                val id = call.parameters["id"]!!
                call.respond(sessionRepo.getSession(id))
            }
            delete("/{id}") {
                val id = call.parameters["id"]!!
                sessionRepo.deleteSession(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }
        get("/stats") {
            val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
            call.respond(sessionRepo.getStats(userId))
        }
    }
}