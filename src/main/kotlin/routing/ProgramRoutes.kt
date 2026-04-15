package com.fittrack.routing

import com.fittrack.model.TrainingDayRequest
import com.fittrack.repository.ProgramRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.programRoutes(programRepo: ProgramRepository) {
    authenticate("auth-jwt") {
        route("/program") {
            get {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                call.respond(programRepo.getProgram(userId))
            }
            put {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val days = call.receive<List<TrainingDayRequest>>()
                call.respond(programRepo.upsertProgram(userId, days))
            }
        }
    }
}