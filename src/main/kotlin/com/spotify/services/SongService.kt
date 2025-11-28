package com.spotify.services

import com.spotify.models.Track
import com.spotify.repository.Tracks
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class SongService(private val s3Service: S3Service) {

    // crear cancion nueva
    suspend fun createTrack(name: String, duration: Int, albumId: UUID, artistId: UUID, audioBytes: ByteArray?): Track {
        var audioKey: String? = null

        // subir archivo de audio a S3
        if (audioBytes != null) {
            audioKey = s3Service.uploadFile("track-$name.mp3", audioBytes, "audio/mpeg")
        }

        // guardar en base de datos
        val newId = dbQuery {
            val id = UUID.randomUUID()
            Tracks.insert {
                it[Tracks.id] = id
                it[Tracks.name] = name
                it[Tracks.duration] = duration
                it[Tracks.previewUrl] = audioKey
                it[Tracks.albumId] = albumId
                it[Tracks.artistId] = artistId
            }
            id
        }

        // generar url firmada para devolver
        val signedUrl = if (audioKey != null) s3Service.getPresignedUrl(audioKey) else null
        return Track(newId, name, duration, signedUrl, albumId, artistId)
    }

    // obtener todas las canciones
    suspend fun getAllTracks(): List<Track> {
        val rawList = dbQuery {
            Tracks.selectAll().map {
                RawTrackData(
                    id = it[Tracks.id],
                    name = it[Tracks.name],
                    duration = it[Tracks.duration],
                    previewKey = it[Tracks.previewUrl],
                    albumId = it[Tracks.albumId],
                    artistId = it[Tracks.artistId]
                )
            }
        }

        // generar urls firmadas para cada cancion
        return rawList.map { raw ->
            val signedUrl = if (raw.previewKey != null) {
                if (raw.previewKey.startsWith("http")) raw.previewKey else s3Service.getPresignedUrl(raw.previewKey)
            } else {
                null
            }
            Track(raw.id, raw.name, raw.duration, signedUrl, raw.albumId, raw.artistId)
        }
    }

    // buscar cancion por id
    suspend fun getTrackById(id: UUID): Track? {
        val raw = dbQuery {
            Tracks.select { Tracks.id eq id }.singleOrNull()?.let {
                RawTrackData(
                    id = it[Tracks.id],
                    name = it[Tracks.name],
                    duration = it[Tracks.duration],
                    previewKey = it[Tracks.previewUrl],
                    albumId = it[Tracks.albumId],
                    artistId = it[Tracks.artistId]
                )
            }
        } ?: return null

        val signedUrl = if (raw.previewKey != null) {
            if (raw.previewKey.startsWith("http")) raw.previewKey else s3Service.getPresignedUrl(raw.previewKey)
        } else {
            null
        }

        return Track(raw.id, raw.name, raw.duration, signedUrl, raw.albumId, raw.artistId)
    }

    // actualizar cancion
    suspend fun updateTrack(id: UUID, name: String?, duration: Int?, albumId: UUID?, artistId: UUID?, audioBytes: ByteArray?): Track? {
        // verificar que existe
        val exists = dbQuery {
            Tracks.select { Tracks.id eq id }.singleOrNull()
        } ?: return null

        // subir nuevo audio si viene
        var newAudioKey: String? = null
        if (audioBytes != null) {
            newAudioKey = s3Service.uploadFile("track-${UUID.randomUUID()}.mp3", audioBytes, "audio/mpeg")
        }

        // actualizar campos
        dbQuery {
            Tracks.update({ Tracks.id eq id }) {
                if (name != null) it[Tracks.name] = name
                if (duration != null) it[Tracks.duration] = duration
                if (albumId != null) it[Tracks.albumId] = albumId
                if (artistId != null) it[Tracks.artistId] = artistId
                if (newAudioKey != null) it[Tracks.previewUrl] = newAudioKey
            }
        }

        return getTrackById(id)
    }

    // eliminar cancion
    suspend fun deleteTrack(id: UUID): Boolean {
        val deleted = dbQuery {
            Tracks.deleteWhere { Tracks.id eq id }
        }
        return deleted > 0
    }

    // datos temporales de la cancion
    private data class RawTrackData(
        val id: UUID,
        val name: String,
        val duration: Int,
        val previewKey: String?,
        val albumId: UUID?,
        val artistId: UUID
    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}