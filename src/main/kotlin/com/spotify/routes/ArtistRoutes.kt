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

        // GET: Listar todos los artistas (Público)
        // Devuelve la lista con las URLs directas de las imágenes
        get {
            try {
                val artists = artistService.getAllArtists()
                call.respond(HttpStatusCode.OK, artists)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error al obtener artistas: ${e.message}")
            }
        }

        // POST: Crear nuevo artista (Protegido - Requiere Token)
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                if (role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, "No tienes permisos de administrador")
                    return@post
                }


                // Procesamiento del Multipart (Formulario con archivo)
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

                    // Validación básica
                    if (name.isNotEmpty() && imageBytes != null) {
                        // Llamamos al servicio (que sube a S3 y guarda en BD)
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
        }
    }
}