package com.izida.backend

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class HealthResponse(
    val service: String,
    val status: String,
    val timestamp: String
)

fun Application.module() {
    install(ContentNegotiation) { json() }

    val accountRepository = AccountRepository()

    routing {
        accountRoutes(accountRepository)
        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                HealthResponse("izida-backend", "ok", Instant.now().toString())
            )
        }
    }
}

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)
