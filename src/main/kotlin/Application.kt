package com.spotify

import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import com.spotify.plugins.*
import com.spotify.services.AuthService
import com.spotify.routes.authRoutes

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    // 1. Plugins
    configureSerialization()
    configureDatabases()

    // 2. Servicios
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.domain").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val authService = AuthService(secret, issuer, audience)

    // 3. Rutas
    routing {
        authRoutes(authService)
    }
}