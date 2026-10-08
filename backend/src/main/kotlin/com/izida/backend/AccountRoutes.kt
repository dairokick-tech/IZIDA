package com.izida.backend

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.UUID
import java.math.RoundingMode

@Serializable data class AccountResponse(val id:String,val userId:String,val currency:String,val status:String,val balance:String)
@Serializable data class MovementResponse(val id:String,val transactionId:String,val type:String,val amount:String,val currency:String,val createdAt:String,val reference:String?)

fun Route.accountRoutes(repository: AccountRepository) {
    route("/api/v1/accounts") {
        get("/{accountId}") {
            val id = runCatching { UUID.fromString(call.parameters["accountId"]) }.getOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "accountId inválido"))
            val a = repository.findAccount(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Cuenta no encontrada"))
            call.respond(AccountResponse(a.id.toString(),a.userId.toString(),a.currency,a.status,a.balance.setScale(2,RoundingMode.HALF_UP).toPlainString()))
        }
        get("/{accountId}/movements") {
            val id = runCatching { UUID.fromString(call.parameters["accountId"]) }.getOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "accountId inválido"))
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
            call.respond(repository.movements(id,limit).map {
                MovementResponse(it.id.toString(),it.transactionId.toString(),it.type,it.amount.setScale(2,RoundingMode.HALF_UP).toPlainString(),it.currency,it.createdAt,it.reference)
            })
        }
    }
}
