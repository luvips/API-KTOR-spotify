package com.spotify.services

import com.spotify.models.*
import com.spotify.models.request.CreateAlbumRequest
import com.spotify.models.request.UpdateAlbumRequest
import com.spotify.repository.Albumes
import com.spotify.repository.Tracks
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class AlbumService {

    suspend fun create(req: CreateAlbumRequest): Album = dbQuery {
        val newId = UUID.randomUUID()
        // req.artistId is already a UUID, we don't need to convert it
        Albumes.insert {
            it[id] = newId
            it[title] = req.title
            it[releaseYear] = req.releaseYear
            it[artistId] = req.artistId
        }
        Album(newId, req.title, req.releaseYear, req.artistId)
    }

    suspend fun getAll(): List<Album> = dbQuery {
        Albumes.selectAll().map {
            Album(it[Albumes.id], it[Albumes.title], it[Albumes.releaseYear], it[Albumes.artistId])
        }
    }

    suspend fun getById(id: UUID): Album? = dbQuery {
        Albumes.select { Albumes.id eq id }.singleOrNull()?.let {
            Album(it[Albumes.id], it[Albumes.title], it[Albumes.releaseYear], it[Albumes.artistId])
        }
    }

    suspend fun update(id: UUID, req: UpdateAlbumRequest): Album? = dbQuery {
        Albumes.update({ Albumes.id eq id }) {
            req.title?.let { t -> it[title] = t }
            req.releaseYear?.let { r -> it[releaseYear] = r }
        }
        getById(id)
    }

    suspend fun delete(id: UUID): Int = dbQuery {
        // Validation: Do not delete if it has child tracks
        if (Tracks.select { Tracks.albumId eq id }.count() > 0) return@dbQuery -1

        val deleted = Albumes.deleteWhere { Albumes.id eq id }
        if (deleted > 0) 1 else 0
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}