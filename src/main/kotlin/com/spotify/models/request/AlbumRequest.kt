package com.spotify.models.request

import java.util.UUID

data class CreateAlbumRequest(
    val title: String,
    val releaseYear: Int,
    val artistId: UUID
)

data class UpdateAlbumRequest(
    val title: String?,
    val releaseYear: Int?
)
