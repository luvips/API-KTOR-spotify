package com.spotify.repository

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object Tracks : Table("tracks") {
    val id = uuid("id")
    val title = varchar("title", 150)
    val duration = integer("duration")
    val albumId = uuid("album_id").references(Albumes.id, onDelete = ReferenceOption.RESTRICT)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}