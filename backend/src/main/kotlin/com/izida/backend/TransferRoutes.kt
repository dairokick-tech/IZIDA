package com.izida.backend

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable data class TransferRequest(val phone:String,val amount:String,val currency:String="PEN",val idempotencyKey:String,val reference:String?=null)
@Serializable data class TransferResponse(val transactionId:String,val amount:String,val currency:String,val status:String)

fun Route.transferRoutes(auth: AuthService, service: TransferService) {
    post("/api/v1/transfers") {
        val user = call.requireUser(auth) ?: return@post
        val request = call.receive<TransferRequest>()
        try {
            val result = service.transfer(user.id,request.phone,BigDecimal(request.amount),request.currency,request.idempotencyKey,request.reference)
            call.respond(TransferResponse(result.transactionId.toString(),result.amount.setScale(2).toPlainString(),result.currency,result.status))
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest,mapOf("error" to (e.message ?: "Transferencia inválida")))
        } catch (e: java.sql.SQLException) {
            call.respond(HttpStatusCode.Conflict,mapOf("error" to "No se pudo procesar la transferencia"))
        }
    }
}
