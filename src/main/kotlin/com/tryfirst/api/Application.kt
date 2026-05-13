package com.tryfirst.api

import com.tryfirst.api.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.tryfirst.api.routes.configureRouting
fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toInt() ?: 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureSerialization()   // JSON support
    configureCORS()            // Izinkan Android app terhubung
    configureDatabase()        // Setup tabel database
    configureStatusPages()     // Handle error dengan rapi
    configureRouting()         // Daftarkan semua endpoint API
}