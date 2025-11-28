package com.spotify.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtDomain = environment.config.property("jwt.domain").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()
    val jwtSecret = environment.config.property("jwt.secret").getString()

    // DEBUG: Imprimir configuración al arrancar
    println(">>> SECURITY CONFIG <<<")
    println("Audience esperada: '$jwtAudience'")
    println("Issuer esperado: '$jwtDomain'")
    println("Secret (longitud): ${jwtSecret.length}")

    authentication {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )

            validate { credential ->
                // DEBUG: Ver qué llega en el token
                println(">>> VALIDANDO TOKEN <<<")
                val aud = credential.payload.audience
                println("Audiencia en token: $aud")

                if (credential.payload.audience.contains(jwtAudience)) {
                    println("✅ Token Válido")
                    JWTPrincipal(credential.payload)
                } else {
                    println("❌ Token Inválido: Audiencia no coincide")
                    null
                }
            }
        }
    }
}