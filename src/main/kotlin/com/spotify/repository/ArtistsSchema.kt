package com.spotify.repository

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object Artists : Table("artists") {
    val id = uuid("id")
    val name = varchar("name", 100)
    val genre = varchar("genre", 50).nullable()
    val image = text("image") // URL de S3
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}