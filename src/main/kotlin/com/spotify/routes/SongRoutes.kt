package com.spotify.routes

import com.spotify.services.SongService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.songRoutes(songService: SongService) {
    route("/tracks") {

        // obtener todas las canciones
        get("/all") {
            try {
                val tracks = songService.getAllTracks()
                call.respond(HttpStatusCode.OK, tracks)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error al obtener canciones: ${e.message}")
            }
        }

        // buscar cancion por id
        get("/{id}") {
            val idParam = call.parameters["id"]
            if (idParam != null) {
                try {
                    val trackId = UUID.fromString(idParam)
                    val track = songService.getTrackById(trackId)
                    if (track != null) {
                        call.respond(HttpStatusCode.OK, track)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Canción no encontrada")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, "ID inválido")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Falta el ID de la canción")
            }
        }

        // crear cancion (solo admin)
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()

                if (role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, "No tienes permisos de administrador")
                    return@post
                }

                try {
                    val multipart = call.receiveMultipart()
                    var name = ""
                    var duration = 0
                    var albumId: UUID? = null
                    var artistId: UUID? = null
                    var audioBytes: ByteArray? = null

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                when (part.name) {
                                    "name" -> name = part.value
                                    "duration" -> duration = part.value.toIntOrNull() ?: 0
                                    "albumId" -> albumId = try { UUID.fromString(part.value) } catch (e: Exception) { null }
                                    "artistId" -> artistId = try { UUID.fromString(part.value) } catch (e: Exception) { null }
                                }
                            }
                            is PartData.FileItem -> {
                                if (part.name == "audio") { // Campo 'audio' en el form-data
                                    audioBytes = part.streamProvider().readBytes()
                                }
                            }
                            else -> part.dispose()
                        }
                        part.dispose()
                    }

                    // validar y crear
                    if (name.isNotEmpty() && artistId != null && audioBytes != null) {
                        val createdTrack = songService.createTrack(name, duration, albumId!!, artistId!!, audioBytes!!)
                        call.respond(HttpStatusCode.Created, createdTrack)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Faltan datos obligatorios: 'name', 'artistId', 'albumId' o 'audio'")
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, "Error al procesar la subida: ${e.message}")
                }
            }

            // actualizar cancion (solo admin)
            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                if (role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, "No tienes permisos de administrador")
                    return@put
                }

                val idParam = call.parameters["id"]
                if (idParam == null) {
                    call.respond(HttpStatusCode.BadRequest, "Falta el ID de la canción")
                    return@put
                }

                try {
                    val trackId = UUID.fromString(idParam)
                    val multipart = call.receiveMultipart()
                    var name: String? = null
                    var duration: Int? = null
                    var albumId: UUID? = null
                    var artistId: UUID? = null
                    var audioBytes: ByteArray? = null

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                when (part.name) {
                                    "name" -> name = part.value
                                    "duration" -> duration = part.value.toIntOrNull()
                                    "albumId" -> albumId = try { UUID.fromString(part.value) } catch (e: Exception) { null }
                                    "artistId" -> artistId = try { UUID.fromString(part.value) } catch (e: Exception) { null }
                                }
                            }
                            is PartData.FileItem -> {
                                if (part.name == "audio") {
                                    audioBytes = part.streamProvider().readBytes()
                                }
                            }
                            else -> part.dispose()
                        }
                        part.dispose()
                    }

                    val updatedTrack = songService.updateTrack(trackId, name, duration, albumId, artistId, audioBytes)
                    if (updatedTrack != null) {
                        call.respond(HttpStatusCode.OK, updatedTrack)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Canción no encontrada")
                    }

                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, "ID inválido")
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, "Error al actualizar canción: ${e.message}")
                }
            }

            // eliminar cancion (solo admin)
            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                if (role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, "No tienes permisos de administrador")
                    return@delete
                }

                val idParam = call.parameters["id"]
                if (idParam == null) {
                    call.respond(HttpStatusCode.BadRequest, "Falta el ID de la canción")
                    return@delete
                }

                try {
                    val trackId = UUID.fromString(idParam)
                    val deleted = songService.deleteTrack(trackId)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Canción eliminada exitosamente"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Canción no encontrada")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, "ID inválido")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error al eliminar canción: ${e.message}")
                }
            }
        }
    }
}