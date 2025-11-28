package com.spotify.models

import java.util.UUID

data class Album(
    val id: UUID,
    val name: String,
    val year: Int,
    val albumArt: String,
    val artistId: UUID
)