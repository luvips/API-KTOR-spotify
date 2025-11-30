package com.spotify.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.spotify.services.*
import com.spotify.routes.*



// Actualizamos la firma para recibir los 4 servicios
fun Application.configureRouting(
    artistService: ArtistService,
    albumService: AlbumService,
    songService: SongService
) {
    routing {
        get("/api") {
            call.respondText("Spotify Backend")
        }

        route("/api") {
            // 1. Rutas de Artistas
            artistRoutes(artistService)

            // 2. Rutas de Álbumes
            albumRoutes(albumService)

            // 3. Rutas de Canciones
            songRoutes(songService)
        }
    }
}