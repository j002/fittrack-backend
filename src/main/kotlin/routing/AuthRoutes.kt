package com.fittrack.routing

import com.fittrack.model.AuthResponse
import com.fittrack.model.LoginRequest
import com.fittrack.model.RegisterRequest
import com.fittrack.plugins.makeToken
import com.fittrack.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(userRepo: UserRepository) {
    route("/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            val user = userRepo.register(req.email, req.password, req.name)
            val token = application.makeToken(user.id)
            call.respond(HttpStatusCode.Created, AuthResponse(token, user.id, user.name))
        }
        post("/login") {
            val req = call.receive<LoginRequest>()
            val user = userRepo.login(req.email, req.password)
            val token = application.makeToken(user.id)
            call.respond(AuthResponse(token, user.id, user.name))
        }
    }
}