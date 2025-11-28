package com.spotify.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.spotify.models.LoginRequest
import com.spotify.repository.Users
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class AuthService(
    private val secret: String,
    private val issuer: String,
    private val audience: String
) {
    private fun generateToken(username: String, role: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", username)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + 86400000)) // 24 horas
            .sign(Algorithm.HMAC256(secret))
    }

    suspend fun login(creds: LoginRequest): String? = dbQuery {
        val userRow = Users.selectAll()
            .where { Users.username eq creds.username }
            .singleOrNull() ?: return@dbQuery null

        // TODO: Usar BCrypt para validar password en producción
        if (userRow[Users.password] == creds.password) {
            generateToken(userRow[Users.username], userRow[Users.role])
        } else {
            null
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}