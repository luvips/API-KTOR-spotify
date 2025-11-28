package com.spotify.routes

import com.spotify.services.ArtistService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.artistRoutes(artistService: ArtistService) {
    route("/artists") {

        // obtener todos los artistas
        get("/all") {
            try {
                val artists = artistService.getAllArtists()
                call.respond(HttpStatusCode.OK, artists)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error al obtener artistas: ${e.message}")
            }
        }

        // buscar artista por id
        get("/{id}") {
            val idParam = call.parameters["id"]
            if (idParam != null) {
                try {
                    val artistId = java.util.UUID.fromString(idParam)
                    val artist = artistService.getArtistById(artistId)
                    if (artist != null) {
                        call.respond(HttpStatusCode.OK, artist)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Artista no encontrado")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, "ID inválido")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Falta el ID del artista")
            }
        }

        // crear artista (solo admin)
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                if (role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, "No tienes permisos de administrador")
                    return@post
                }

                // leer datos del formulario
                try {
                    val multipart = call.receiveMultipart()
                    var name = ""
                    var genre = ""
                    var imageBytes: ByteArray? = null

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                if (part.name == "name") name = part.value
                                if (part.name == "genre") genre = part.value
                            }
                            is PartData.FileItem -> {
                                if (part.name == "image") {
                                    // Leemos los bytes del archivo en memoria
                                    imageBytes = part.streamProvider().readBytes()
                                }
                            }
                            else -> part.dispose()
                        }
                        part.dispose()
                    }

                    // validar y crear
                    if (name.isNotEmpty() && imageBytes != null) {
                        val createdArtist = artistService.createArtist(name, genre, imageBytes!!)
                        call.respond(HttpStatusCode.Created, createdArtist)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Faltan datos obligatorios: 'name' o 'image'")
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, "Error al procesar la subida: ${e.message}")
                }
            }

            // actualizar artista (solo admin)
            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                if (role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, "No tienes permisos de administrador")
                    return@put
                }

                val idParam = call.parameters["id"]
                if (idParam == null) {
                    call.respond(HttpStatusCode.BadRequest, "Falta el ID del artista")
                    return@put
                }

                try {
                    val artistId = java.util.UUID.fromString(idParam)
                    val multipart = call.receiveMultipart()
                    var name: String? = null
                    var genre: String? = null
                    var imageBytes: ByteArray? = null

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                if (part.name == "name") name = part.value
                                if (part.name == "genre") genre = part.value
                            }
                            is PartData.FileItem -> {
                                if (part.name == "image") {
                                    imageBytes = part.streamProvider().readBytes()
                                }
                            }
                            else -> part.dispose()
                        }
                        part.dispose()
                    }

                    val updatedArtist = artistService.updateArtist(artistId, name, genre, imageBytes)
                    if (updatedArtist != null) {
                        call.respond(HttpStatusCode.OK, updatedArtist)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Artista no encontrado")
                    }

                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, "ID inválido")
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, "Error al actualizar artista: ${e.message}")
                }
            }

            // eliminar artista (solo admin)
            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                if (role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, "No tienes permisos de administrador")
                    return@delete
                }

                val idParam = call.parameters["id"]
                if (idParam == null) {
                    call.respond(HttpStatusCode.BadRequest, "Falta el ID del artista")
                    return@delete
                }

                try {
                    val artistId = java.util.UUID.fromString(idParam)
                    val deleted = artistService.deleteArtist(artistId)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Artista eliminado exitosamente"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Artista no encontrado")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, "ID inválido")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error al eliminar artista: ${e.message}")
                }
            }
        }
    }
}