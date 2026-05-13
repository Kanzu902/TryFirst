package com.tryfirst.api.plugins

import com.tryfirst.api.models.ApiResponse
import com.tryfirst.api.services.PracticeTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

// ─── JSON Serialization ───────────────────────────────────────────────────────
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true  // Android tidak perlu kirim semua field
        })
    }
}

// ─── CORS (izinkan Android app terhubung) ─────────────────────────────────────
fun Application.configureCORS() {
    install(CORS) {
        anyHost()  // Development: izinkan semua
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Put)
    }
}

// ─── Database Setup ───────────────────────────────────────────────────────────
fun Application.configureDatabase() {
    val dbUrl  = environment.config.propertyOrNull("database.url")?.getString()
        ?: "jdbc:h2:mem:tryfirst;DB_CLOSE_DELAY=-1"
    val driver = environment.config.propertyOrNull("database.driver")?.getString()
        ?: "org.h2.Driver"
    val user   = environment.config.propertyOrNull("database.user")?.getString() ?: ""
    val pass   = environment.config.propertyOrNull("database.password")?.getString() ?: ""

    val hikariConfig = HikariConfig().apply {
        jdbcUrl         = dbUrl
        driverClassName = driver
        username        = user
        password        = pass
        maximumPoolSize = 10
    }

    Database.connect(HikariDataSource(hikariConfig))

    // Auto-create tabel saat server start (sama seperti Room auto-migrate)
    transaction {
        SchemaUtils.create(PracticeTable)
    }

    log.info("✅ Database connected: $dbUrl")
}

// ─── Error Handling ───────────────────────────────────────────────────────────
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled error", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(
                    success = false,
                    error = "Terjadi kesalahan: ${cause.message}"
                )
            )
        }
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponse<Nothing>(success = false, error = "Endpoint tidak ditemukan")
            )
        }
    }
}
