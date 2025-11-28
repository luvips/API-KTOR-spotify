plugins {
    kotlin("jvm") version "2.2.20"
    id("io.ktor.plugin") version "3.0.3"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "com.spotify"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    // --- Ktor Core & Server ---
    implementation("io.ktor:ktor-server-core-jvm:3.0.3")
    implementation("io.ktor:ktor-server-netty-jvm:3.0.3")
    implementation("io.ktor:ktor-server-cors-jvm:3.0.3")
    implementation("io.ktor:ktor-server-status-pages-jvm:3.0.3")
    implementation("io.ktor:ktor-server-call-logging-jvm:3.0.3")
    implementation("io.ktor:ktor-server-host-common-jvm:3.0.3")

    // --- Auth & JWT (Seguridad) ---
    implementation("io.ktor:ktor-server-auth-jvm:3.0.3")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:3.0.3")

    // --- Serialización (JSON) ---
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.0.3")
    implementation("io.ktor:ktor-serialization-jackson-jvm:3.0.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2") // Para fechas

    // --- Base de Datos (Exposed + Postgres) ---
    implementation("org.jetbrains.exposed:exposed-core:0.50.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.50.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.50.1")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("com.zaxxer:HikariCP:5.1.0") // Pool de conexiones

    // --- AWS S3 (Para subir imágenes) ---
    implementation("aws.sdk.kotlin:s3:1.0.0")
    implementation("aws.smithy.kotlin:aws-signing-default:1.0.0")

    // --- Ktor Client (Para subir a S3 con URLs prefirmadas) ---
    implementation("io.ktor:ktor-client-core:3.0.3")
    implementation("io.ktor:ktor-client-cio:3.0.3")

    // --- Logging ---
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // --- Tests ---
    testImplementation("io.ktor:ktor-server-test-host-jvm:3.0.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.1.0")
}

tasks {
    shadowJar {
        mergeServiceFiles()
        archiveFileName.set("mi-api2.jar")
    }
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}