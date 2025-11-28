package com.spotify.models

import java.util.UUID

data class User(
    val id: UUID,
    val username: String,
    val role: String
)

data class LoginRequest(val username: String, val password: String)
data class AuthResponse(val token: String)