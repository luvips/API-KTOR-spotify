package com.spotify.routes

import com.spotify.models.LoginRequest
import com.spotify.models.AuthResponse
import com.spotify.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/login") {
            try {
                val creds = call.receive<LoginRequest>()
                val token = authService.login(creds)

                if (token != null) {
                    call.respond(AuthResponse(token))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Credenciales inválidas")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Error en la solicitud")
            }
        }
    }
}