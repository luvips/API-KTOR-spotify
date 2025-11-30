package com.spotify.routes

import com.spotify.services.SongService
import com.spotify.models.request.CreateTrackRequest
import com.spotify.models.request.UpdateTrackRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.songRoutes(service: SongService) {
    route("/tracks") {
        post {
            try {
                val req = call.receive<CreateTrackRequest>()
                call.respond(HttpStatusCode.Created, service.create(req))
            } catch (e: Exception) {
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
                val req = call.receive<UpdateTrackRequest>()
                val updated = service.update(id, req)
                if (updated != null) call.respond(updated) else call.respond(HttpStatusCode.NotFound)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }

        delete("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                if (service.delete(id)) {
                    call.respond(HttpStatusCode.OK, mapOf("msg" to "Track deleted"))
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }
    }
}