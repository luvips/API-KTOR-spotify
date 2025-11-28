package com.spotify

import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import com.spotify.plugins.*
import com.spotify.routes.*
import com.spotify.services.*




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

    val albumService = AlbumService(s3Service)

    // 3. Rutas
    configureRouting(authService, artistService, albumService)
}

