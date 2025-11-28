package com.spotify.models

import java.util.UUID

data class Artist(
    val id: UUID,
    val name: String,
    val genre: String?,
    val image: String
)