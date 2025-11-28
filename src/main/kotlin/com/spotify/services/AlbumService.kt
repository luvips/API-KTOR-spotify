package com.spotify.services

import com.spotify.models.Album
import com.spotify.repository.Albums
import io.ktor.server.plugins.* // Para NotFoundException si usas eso, o usa null
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class AlbumService(private val s3Service: S3Service) {

    // CREAR: Sube -> Guarda Key -> Firma URL
    suspend fun create(name: String, year: Int, artistId: UUID, imageBytes: ByteArray): Album {
        // 1. Subir a S3 y obtener solo el NOMBRE (Key)
        // Usamos un nombre único: "album-{uuid}-{nombre}.jpg"
        val imageKey = s3Service.uploadFile("album-${UUID.randomUUID()}-$name.jpg", imageBytes, "image/jpeg")

        // 2. Guardar Key en BD
        val newId = dbQuery {
            val id = UUID.randomUUID()
            Albums.insert {
                it[Albums.id] = id
                it[Albums.name] = name
                it[Albums.year] = year
                it[Albums.albumArt] = imageKey // Guardamos solo la referencia (Key)
                it[Albums.artistId] = artistId
            }
            id
        }

        // 3. Generar URL firmada para devolver al cliente inmediatamente
        // (Asumiendo que tu S3Service tiene el método getPresignedUrl como en tu ejemplo de Artist)
        val signedUrl = s3Service.getPresignedUrl(imageKey)

        return Album(newId, name, year, signedUrl, artistId)
    }

    // LISTAR TODOS: Obtiene Keys -> Firma URLs
    suspend fun getAll(): List<Album> {
        // 1. Obtener datos crudos
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

        // 2. Transformar generando URLs firmadas
        return rawList.map { raw ->
            // Si por alguna razón la key ya es http (datos viejos), la dejamos, si no, firmamos
            val signedUrl = if (raw.imageKey.startsWith("http")) raw.imageKey else s3Service.getPresignedUrl(raw.imageKey)

            Album(raw.id, raw.name, raw.year, signedUrl, raw.artistId)
        }
    }

    // LISTAR POR ARTISTA
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

    // DTO interno para sacar datos de la transacción
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