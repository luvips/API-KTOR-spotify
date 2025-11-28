package com.spotify.services

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.presigners.presignGetObject
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.collections.Attributes
import aws.smithy.kotlin.runtime.content.ByteStream
import io.ktor.server.config.*
import java.util.UUID
import kotlin.time.Duration.Companion.hours

class S3Service(config: ApplicationConfig) {
    private val bucketName = config.property("aws.bucketName").getString()
    private val region = config.property("aws.region").getString()
    private val accessKey = config.property("aws.accessKey").getString()
    private val secretKey = config.property("aws.secretKey").getString()
    private val sessionToken = config.propertyOrNull("aws.sessionToken")?.getString()

    // Helper de credenciales
    private fun getCredentialsProvider(): CredentialsProvider {
        return object : CredentialsProvider {
            override suspend fun resolve(attributes: Attributes): Credentials {
                return Credentials(accessKey, secretKey, sessionToken)
            }
        }
    }

    // 1. SUBIR (Sin ACLs)
    suspend fun uploadFile(fileName: String, fileBytes: ByteArray, contentType: String): String {
        val uniqueName = "${UUID.randomUUID()}-$fileName"

        S3Client.fromEnvironment {
            this.region = this@S3Service.region
            this.credentialsProvider = getCredentialsProvider()
        }.use { s3 ->
            val request = PutObjectRequest {
                bucket = bucketName
                key = uniqueName
                body = ByteStream.fromBytes(fileBytes)
                this.contentType = contentType
            }
            s3.putObject(request)
        }

        return uniqueName
    }

    // 2. OBTENER URL FIRMADA (Esta es la función que te faltaba)
    suspend fun getPresignedUrl(objectKey: String): String {
        S3Client.fromEnvironment {
            this.region = this@S3Service.region
            this.credentialsProvider = getCredentialsProvider()
        }.use { s3 ->
            val request = GetObjectRequest {
                bucket = bucketName
                key = objectKey
            }

            // Generamos la firma válida por 12 horas
            val presignedRequest = s3.presignGetObject(request, duration = 12.hours)
            return presignedRequest.url.toString()
        }
    }
}