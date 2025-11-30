package com.spotify.models.request

data class CreateArtistRequest(
    val name: String,
    val genre: String
)

data class UpdateArtistRequest(
    val name: String?,
    val genre: String?
)
