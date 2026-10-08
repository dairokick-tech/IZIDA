package com.izida.backend

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.math.RoundingMode

@Serializable data class AccountResponse(val id:String,val userId:String,val currency:String,val status:String,val balance:String,val fullName:String)
@Serializable data class MovementResponse(val id:String,val transactionId:String,val type:String,val amount:String,val currency:String,val createdAt:String,val reference:String?)

fun Route.accountRoutes(repository: AccountRepository, auth: AuthService) {
    route("/api/v1/me") {
        get("/account") {
            val user = call.requireUser(auth) ?: return@get
            val a = repository.findAccountByUserId(user.id)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Cuenta no encontrada"))
            call.respond(AccountResponse(a.id.toString(),a.userId.toString(),a.currency,a.status,a.balance.setScale(2,RoundingMode.HALF_UP).toPlainString(),a.fullName))
        }
        get("/account/movements") {
            val user = call.requireUser(auth) ?: return@get
            val a = repository.findAccountByUserId(user.id)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Cuenta no encontrada"))
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
            call.respond(repository.movements(a.id,limit).map {
                MovementResponse(it.id.toString(),it.transactionId.toString(),it.type,it.amount.setScale(2,RoundingMode.HALF_UP).toPlainString(),it.currency,it.createdAt,it.reference)
            })
        }
    }
}