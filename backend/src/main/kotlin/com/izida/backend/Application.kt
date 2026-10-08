package com.izida.backend

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.Serializable
import io.ktor.server.request.receive
import java.time.Instant

@Serializable
data class HealthResponse(
    val service: String,
    val status: String,
    val timestamp: String
)

@Serializable data class LoginRequest(val phone:String,val pin:String)
@Serializable data class RegisterRequest(val fullName:String,val phone:String,val pin:String)
@Serializable data class LoginResponse(val userId:String,val fullName:String,val phone:String,val token:String)

fun Application.module() {
    install(ContentNegotiation) { json() }

    val accountRepository = AccountRepository()
    val authService = AuthService()

    routing {
        post("/api/v1/auth/register") {
            val request=call.receive<RegisterRequest>()
            try {
                val result=authService.register(request.fullName,request.phone,request.pin)
                call.respond(HttpStatusCode.Created,LoginResponse(result.user.id.toString(),result.user.fullName,result.user.phone,result.token))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest,mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: java.sql.SQLException) {
                call.respond(HttpStatusCode.Conflict,mapOf("error" to "El celular ya está registrado o no se pudo crear la cuenta"))
            }
        }
        post("/api/v1/auth/login") {
            val request=call.receive<LoginRequest>()
            val result=authService.login(request.phone,request.pin)
            if(result==null) call.respond(HttpStatusCode.Unauthorized,mapOf("error" to "Credenciales invalidas"))
            else call.respond(LoginResponse(result.user.id.toString(),result.user.fullName,result.user.phone,result.token))
        }
        accountRoutes(accountRepository, authService)
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
