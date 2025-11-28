package com.spotify.repository

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object Tracks : Table("tracks") {
    val id = uuid("id")
    val name = varchar("name", 150)
    val duration = integer("duration") // Duración en ms
    val previewUrl = text("preview_url").nullable() // URL del MP3

    // Relaciones con borrado en cascada
    val albumId = uuid("album_id").references(Albums.id, onDelete = ReferenceOption.CASCADE).nullable()
    val artistId = uuid("artist_id").references(Artists.id, onDelete = ReferenceOption.CASCADE)

    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}