package com.spotify.plugins

import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson {
            // Para formatear el JSON de salida (pretty print)
            enable(SerializationFeature.INDENT_OUTPUT)

            // Para manejar fechas de Java 8+ correctamente
            registerModule(JavaTimeModule())
        }
    }
}