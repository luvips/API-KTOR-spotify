package com.spotify.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.spotify.services.*
import com.spotify.routes.*



// Actualizamos la firma para recibir los 4 servicios
fun Application.configureRouting(
    authService: AuthService,
    artistService: ArtistService,
    albumService: AlbumService,
    songService: SongService
) {
    routing {
        get("/") {
            call.respondText("Spotify Backend")
        }

        // 1. Rutas de Autenticación
        authRoutes(authService)

        // 2. Rutas de Artistas
        artistRoutes(artistService)

        // 3. Rutas de Álbumes
        albumRoutes(albumService)

        // 4. Rutas de Canciones
        songRoutes(songService)
    }
}