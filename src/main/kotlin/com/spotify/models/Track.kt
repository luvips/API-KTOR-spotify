package com.spotify.models

import java.util.UUID

data class Track(
    val id: UUID,
    val name: String,
    val duration: Int,
    val previewUrl: String?,
    val albumId: UUID?,
    val artistId: UUID
)