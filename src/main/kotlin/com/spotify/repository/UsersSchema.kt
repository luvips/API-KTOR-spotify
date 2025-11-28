package com.spotify.repository

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = uuid("id")
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("password", 255)
    val role = varchar("role", 20).default("USER")

    override val primaryKey = PrimaryKey(id)
}