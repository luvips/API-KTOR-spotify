package com.spotify

import io.ktor.server.application.*
import io.ktor.server.netty.*
import com.spotify.plugins.*
import com.spotify.services.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    configureSerialization()
    configureDatabases()

    val artistService = ArtistService()
    val albumService = AlbumService()
    val songService = SongService()

    // Llama a la función de enrutamiento principal
    configureRouting(artistService, albumService, songService)
}