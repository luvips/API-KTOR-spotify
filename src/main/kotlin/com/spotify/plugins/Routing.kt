package com.spotify.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.spotify.services.AuthService
import com.spotify.services.ArtistService
import com.spotify.services.AlbumService // <--- Nuevo Import
import com.spotify.routes.authRoutes
import com.spotify.routes.artistRoutes
import com.spotify.routes.albumRoutes // <--- Nuevo Import

// Actualizamos la firma para recibir los 3 servicios
fun Application.configureRouting(
    authService: AuthService,
    artistService: ArtistService,
    albumService: AlbumService
) {
    routing {
        get("/") {
            call.respondText("Spotify Backend")
        }

        // 1. Rutas de Autenticación
        authRoutes(authService)

        // 2. Rutas de Artistas
        artistRoutes(artistService)

        // 3. Rutas de Álbumes (NUEVO)
        albumRoutes(albumService)
    }
}