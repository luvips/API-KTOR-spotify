package com.spotify

import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import com.spotify.plugins.*
import com.spotify.routes.artistRoutes
import com.spotify.services.AuthService
import com.spotify.routes.authRoutes
import com.spotify.services.ArtistService
import com.spotify.services.S3Service

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    // 1. Plugins
    configureSerialization()
    configureDatabases()
    configureSecurity()

    // 2. Servicios
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.domain").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val authService = AuthService(secret, issuer, audience)

    val s3Service = S3Service(environment.config)
    val artistService = ArtistService(s3Service)

    // 3. Rutas
    routing {
        authRoutes(authService)
        artistRoutes(artistService)
    }
}

