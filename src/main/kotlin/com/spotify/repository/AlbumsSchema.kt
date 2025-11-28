package com.spotify.repository

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object Albums : Table("albums") {
    val id = uuid("id")
    val name = varchar("name", 100)
    val year = integer("year")
    val albumArt = text("album_art") // URL pública de S3

    // Clave foránea: Si se borra el artista, se borran sus álbumes
    val artistId = uuid("artist_id").references(Artists.id, onDelete = ReferenceOption.CASCADE)

    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}