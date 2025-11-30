package com.spotify.services

import com.spotify.models.*
import com.spotify.models.request.CreateTrackRequest
import com.spotify.models.request.UpdateTrackRequest
import com.spotify.repository.Tracks
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SongService {

    suspend fun create(req: CreateTrackRequest): Track = dbQuery {
        val newId = UUID.randomUUID()
        // req.albumId is already a UUID
        Tracks.insert {
            it[id] = newId
            it[title] = req.title
            it[duration] = req.duration
            it[albumId] = req.albumId
        }
        Track(newId, req.title, req.duration, req.albumId)
    }

    suspend fun getAll(): List<Track> = dbQuery {
        Tracks.selectAll().map {
            Track(it[Tracks.id], it[Tracks.title], it[Tracks.duration], it[Tracks.albumId])
        }
    }

    suspend fun getById(id: UUID): Track? = dbQuery {
        Tracks.select { Tracks.id eq id }.singleOrNull()?.let {
            Track(it[Tracks.id], it[Tracks.title], it[Tracks.duration], it[Tracks.albumId])
        }
    }

    suspend fun update(id: UUID, req: UpdateTrackRequest): Track? = dbQuery {
        Tracks.update({ Tracks.id eq id }) {
            req.title?.let { t -> it[title] = t }
            req.duration?.let { d -> it[duration] = d }
        }
        getById(id)
    }

    suspend fun delete(id: UUID): Boolean = dbQuery {
        Tracks.deleteWhere { Tracks.id eq id } > 0
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}