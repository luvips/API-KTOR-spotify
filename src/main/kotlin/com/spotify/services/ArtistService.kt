package com.spotify.services

import com.spotify.models.Artist
import com.spotify.repository.Artists
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ArtistService(private val s3Service: S3Service) {

    // CREAR: Sube limpio -> Guarda Key -> Firma URL
    suspend fun createArtist(name: String, genre: String, imageBytes: ByteArray): Artist {
        // 1. Subir a S3 (Obtenemos solo la Key "uuid-foto.jpg")
        val imageKey = s3Service.uploadFile("artist-$name.jpg", imageBytes, "image/jpeg")

        // 2. Guardar Key en BD
        val newId = dbQuery {
            val id = UUID.randomUUID()
            Artists.insert {
                it[Artists.id] = id
                it[Artists.name] = name
                it[Artists.genre] = genre
                it[Artists.image] = imageKey // Guardamos solo la referencia (nombre del archivo)
            }
            id
        }

        // 3. Generar URL firmada para devolver al cliente inmediatamente
        val signedUrl = s3Service.getPresignedUrl(imageKey)

        return Artist(newId, name, genre, signedUrl)
    }

    // LISTAR: Obtiene Keys -> Firma URLs
    suspend fun getAllArtists(): List<Artist> {
        val rawList = dbQuery {
            Artists.selectAll().map {
                Triple(it[Artists.id], it[Artists.name], it[Artists.image])
            }
        }

        return rawList.map { (id, name, key) ->
            // Si la key ya es una URL completa (de pruebas viejas), la devolvemos tal cual.
            // Si es solo el nombre del archivo, generamos la firma fresca.
            val signedUrl = if (key.startsWith("http")) key else s3Service.getPresignedUrl(key)

            Artist(id, name, "Género", signedUrl)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}