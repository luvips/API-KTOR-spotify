package com.spotify.models

import java.util.UUID

data class Album(
    val id: UUID,
    val title: String,
    val releaseYear: Int?,
    val artistId: UUID
)