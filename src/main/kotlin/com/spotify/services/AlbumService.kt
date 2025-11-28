package com.spotify.services

import com.spotify.models.Album
import com.spotify.repository.Albums
import io.ktor.server.plugins.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class AlbumService(private val s3Service: S3Service) {

    // crear album nuevo
    suspend fun create(name: String, year: Int, artistId: UUID, imageBytes: ByteArray): Album {
        // subir portada del album a S3
        val imageKey = s3Service.uploadFile("album-${UUID.randomUUID()}-$name.jpg", imageBytes, "image/jpeg")

        // guardar en base de datos
        val newId = dbQuery {
            val id = UUID.randomUUID()
            Albums.insert {
                it[Albums.id] = id
                it[Albums.name] = name
                it[Albums.year] = year
                it[Albums.albumArt] = imageKey
                it[Albums.artistId] = artistId
            }
            id
        }

        // devolver con url firmada
        val signedUrl = s3Service.getPresignedUrl(imageKey)
        return Album(newId, name, year, signedUrl, artistId)
    }

    // obtener todos los albums
    suspend fun getAll(): List<Album> {
        val rawList = dbQuery {
            Albums.selectAll().map {
                RawAlbumData(
                    id = it[Albums.id],
                    name = it[Albums.name],
                    year = it[Albums.year],
                    imageKey = it[Albums.albumArt],
                    artistId = it[Albums.artistId]
                )
            }
        }

        // generar urls firmadas
        return rawList.map { raw ->
            val signedUrl = if (raw.imageKey.startsWith("http")) raw.imageKey else s3Service.getPresignedUrl(raw.imageKey)
            Album(raw.id, raw.name, raw.year, signedUrl, raw.artistId)
        }
    }

    // filtrar albums por artista
    suspend fun getByArtistId(artistId: UUID): List<Album> {
        val rawList = dbQuery {
            Albums.select { Albums.artistId eq artistId }.map {
                RawAlbumData(
                    id = it[Albums.id],
                    name = it[Albums.name],
                    year = it[Albums.year],
                    imageKey = it[Albums.albumArt],
                    artistId = it[Albums.artistId]
                )
            }
        }

        return rawList.map { raw ->
            val signedUrl = if (raw.imageKey.startsWith("http")) raw.imageKey else s3Service.getPresignedUrl(raw.imageKey)
            Album(raw.id, raw.name, raw.year, signedUrl, raw.artistId)
        }
    }

    // buscar album por id
    suspend fun getById(id: UUID): Album? {
        val raw = dbQuery {
            Albums.select { Albums.id eq id }.singleOrNull()?.let {
                RawAlbumData(
                    id = it[Albums.id],
                    name = it[Albums.name],
                    year = it[Albums.year],
                    imageKey = it[Albums.albumArt],
                    artistId = it[Albums.artistId]
                )
            }
        } ?: return null

        val signedUrl = if (raw.imageKey.startsWith("http")) raw.imageKey else s3Service.getPresignedUrl(raw.imageKey)
        return Album(raw.id, raw.name, raw.year, signedUrl, raw.artistId)
    }

    // actualizar album
    suspend fun update(id: UUID, name: String?, year: Int?, artistId: UUID?, imageBytes: ByteArray?): Album? {
        // verificar que existe
        val exists = dbQuery {
            Albums.select { Albums.id eq id }.singleOrNull()
        } ?: return null

        // subir nueva portada si viene
        var newImageKey: String? = null
        if (imageBytes != null) {
            newImageKey = s3Service.uploadFile("album-${UUID.randomUUID()}.jpg", imageBytes, "image/jpeg")
        }

        // actualizar campos
        dbQuery {
            Albums.update({ Albums.id eq id }) {
                if (name != null) it[Albums.name] = name
                if (year != null) it[Albums.year] = year
                if (artistId != null) it[Albums.artistId] = artistId
                if (newImageKey != null) it[Albums.albumArt] = newImageKey
            }
        }

        return getById(id)
    }

    // eliminar album
    suspend fun delete(id: UUID): Boolean {
        val deleted = dbQuery {
            Albums.deleteWhere { Albums.id eq id }
        }
        return deleted > 0
    }

    // datos temporales del album
    private data class RawAlbumData(
        val id: UUID,
        val name: String,
        val year: Int,
        val imageKey: String,
        val artistId: UUID
    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}