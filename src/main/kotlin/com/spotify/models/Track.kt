package com.spotify.models

import java.util.UUID

data class Track(
    val id: UUID,
    val title: String,
    val duration: Int,
    val albumId: UUID
)