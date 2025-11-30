package com.spotify.routes

import com.spotify.services.AlbumService
import com.spotify.models.request.CreateAlbumRequest
import com.spotify.models.request.UpdateAlbumRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.albumRoutes(service: AlbumService) {
    route("/albumes") {
        post {
            try {
                val req = call.receive<CreateAlbumRequest>()
                call.respond(HttpStatusCode.Created, service.create(req))
            } catch (e: Exception) {
                // Catches conversion errors if the JSON UUID is malformed
                call.respond(HttpStatusCode.BadRequest, "Invalid data: ${e.localizedMessage}")
            }
        }

        get {
            call.respond(service.getAll())
        }

        get("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                val item = service.getById(id)
                if (item != null) call.respond(item) else call.respond(HttpStatusCode.NotFound)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }

        put("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                val req = call.receive<UpdateAlbumRequest>()
                val updated = service.update(id, req)
                if (updated != null) call.respond(updated) else call.respond(HttpStatusCode.NotFound)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }

        delete("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                when (service.delete(id)) {
                    1 -> call.respond(HttpStatusCode.OK, mapOf("msg" to "Album deleted"))
                    0 -> call.respond(HttpStatusCode.NotFound)
                    -1 -> call.respond(HttpStatusCode.Conflict, "Cannot delete: Has associated tracks")
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }
    }
}