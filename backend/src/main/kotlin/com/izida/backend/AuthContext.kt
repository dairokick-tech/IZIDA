package com.izida.backend

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.respond

suspend fun ApplicationCall.requireUser(auth: AuthService): AuthUser? {
    val header = request.headers["Authorization"] ?: run {
        respond(HttpStatusCode.Unauthorized, mapOf("error" to "Autenticación requerida"))
        return null
    }
    val token = header.removePrefix("Bearer ").takeIf { header.startsWith("Bearer ") && it.isNotBlank() } ?: run {
        respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token inválido"))
        return null
    }
    return auth.userForToken(token) ?: run {
        respond(HttpStatusCode.Unauthorized, mapOf("error" to "Sesión inválida o expirada"))
        null
    }
}
