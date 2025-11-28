package com.spotify.plugins

import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature // Importante importar esto
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson {
            // Para formatear el JSON de salida (pretty print)
            enable(SerializationFeature.INDENT_OUTPUT)

            // Para evitar errores si el frontend manda campos que no existen en el backend
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            // Para manejar fechas de Java 8+ correctamente
            registerModule(JavaTimeModule())
        }
    }
}