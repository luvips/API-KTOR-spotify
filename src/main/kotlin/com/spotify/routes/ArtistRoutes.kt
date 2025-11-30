package com.spotify.routes

import com.spotify.services.ArtistService
import com.spotify.models.request.CreateArtistRequest
import com.spotify.models.request.UpdateArtistRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.artistRoutes(service: ArtistService) {
    route("/artistas") {
        post {
            // Jackson converts the JSON to the object with the correct types
            val req = call.receive<CreateArtistRequest>()
            call.respond(HttpStatusCode.Created, service.create(req))
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
                val req = call.receive<UpdateArtistRequest>()
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
                    1 -> call.respond(HttpStatusCode.OK, mapOf("msg" to "Artist deleted"))
                    0 -> call.respond(HttpStatusCode.NotFound)
                    -1 -> call.respond(HttpStatusCode.Conflict, "Cannot delete: Has associated albums")
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }
    }
}