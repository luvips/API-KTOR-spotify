package com.spotify.services

import com.spotify.models.Artist
import com.spotify.repository.Artists
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ArtistService(private val s3Service: S3Service) {

    // crear artista con imagen
    suspend fun createArtist(name: String, genre: String, imageBytes: ByteArray): Artist {
        // subir imagen a S3 primero
        val imageKey = s3Service.uploadFile("artist-$name.jpg", imageBytes, "image/jpeg")

        // insertar en la base de datos
        val newId = dbQuery {
            val id = UUID.randomUUID()
            Artists.insert {
                it[Artists.id] = id
                it[Artists.name] = name
                it[Artists.genre] = genre
                it[Artists.image] = imageKey
            }
            id
        }

        // generar url firmada para responder
        val signedUrl = s3Service.getPresignedUrl(imageKey)
        return Artist(newId, name, genre, signedUrl)
    }

    // obtener todos los artistas
    suspend fun getAllArtists(): List<Artist> {
        val rawList = dbQuery {
            Artists.selectAll().map {
                Triple(it[Artists.id], it[Artists.name], it[Artists.image])
            }
        }

        // generar urls firmadas para cada artista
        return rawList.map { (id, name, key) ->
            val signedUrl = if (key.startsWith("http")) key else s3Service.getPresignedUrl(key)
            Artist(id, name, "Género", signedUrl)
        }
    }

    // buscar artista por id
    suspend fun getArtistById(id: UUID): Artist? {
        val raw = dbQuery {
            Artists.select { Artists.id eq id }.singleOrNull()?.let {
                Triple(it[Artists.id], it[Artists.name], it[Artists.genre])
            }
        } ?: return null

        val (artistId, name, genre) = raw
        val imageKey = dbQuery {
            Artists.select { Artists.id eq id }.singleOrNull()?.get(Artists.image)
        } ?: return null

        val signedUrl = if (imageKey.startsWith("http")) imageKey else s3Service.getPresignedUrl(imageKey)
        return Artist(artistId, name, genre, signedUrl)
    }

    // actualizar datos del artista
    suspend fun updateArtist(id: UUID, name: String?, genre: String?, imageBytes: ByteArray?): Artist? {
        // verificar que existe
        val exists = dbQuery {
            Artists.select { Artists.id eq id }.singleOrNull()
        } ?: return null

        // si hay nueva imagen, subirla
        var newImageKey: String? = null
        if (imageBytes != null) {
            newImageKey = s3Service.uploadFile("artist-$name-${UUID.randomUUID()}.jpg", imageBytes, "image/jpeg")
        }

        // actualizar solo los campos que vienen
        dbQuery {
            Artists.update({ Artists.id eq id }) {
                if (name != null) it[Artists.name] = name
                if (genre != null) it[Artists.genre] = genre
                if (newImageKey != null) it[Artists.image] = newImageKey
            }
        }

        return getArtistById(id)
    }

    // eliminar artista
    suspend fun deleteArtist(id: UUID): Boolean {
        val deleted = dbQuery {
            Artists.deleteWhere { Artists.id eq id }
        }
        return deleted > 0
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}