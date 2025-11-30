package com.spotify.services

import com.spotify.models.*
import com.spotify.models.request.CreateArtistRequest
import com.spotify.models.request.UpdateArtistRequest
import com.spotify.repository.Artistas
import com.spotify.repository.Albumes
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ArtistService {

    suspend fun create(req: CreateArtistRequest): Artist = dbQuery {
        val newId = UUID.randomUUID()
        Artistas.insert {
            it[id] = newId
            it[name] = req.name
            it[genre] = req.genre
        }
        Artist(newId, req.name, req.genre)
    }

    suspend fun getAll(): List<Artist> = dbQuery {
        Artistas.selectAll().map {
            Artist(it[Artistas.id], it[Artistas.name], it[Artistas.genre])
        }
    }

    suspend fun getById(id: UUID): Artist? = dbQuery {
        Artistas.select { Artistas.id eq id }.singleOrNull()?.let {
            Artist(it[Artistas.id], it[Artistas.name], it[Artistas.genre])
        }
    }

    suspend fun update(id: UUID, req: UpdateArtistRequest): Artist? = dbQuery {
        Artistas.update({ Artistas.id eq id }) {
            req.name?.let { n -> it[name] = n }
            req.genre?.let { g -> it[genre] = g }
        }
        getById(id)
    }

    suspend fun delete(id: UUID): Int = dbQuery {
        if (Albumes.select { Albumes.artistId eq id }.count() > 0) return@dbQuery -1

        val deleted = Artistas.deleteWhere { Artistas.id eq id }
        if (deleted > 0) 1 else 0
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}