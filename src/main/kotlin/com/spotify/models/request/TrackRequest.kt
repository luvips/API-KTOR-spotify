package com.spotify.models.request

import java.util.UUID

data class CreateTrackRequest(
    val title: String,
    val duration: Int,
    val albumId: UUID
)

data class UpdateTrackRequest(
    val title: String?,
    val duration: Int?
)
