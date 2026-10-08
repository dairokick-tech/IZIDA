package com.izida.backend

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable data class RecipientResponse(val userId:String,val accountId:String,val phone:String,val displayName:String)

fun Route.recipientRoutes(auth: AuthService) {
    get("/api/v1/recipients/{phone}") {
        if (call.requireUser(auth) == null) return@get
        val phone = call.parameters["phone"] ?: return@get call.respond(HttpStatusCode.BadRequest,mapOf("error" to "Celular requerido"))
        if (!phone.matches(Regex("[0-9]{9}"))) return@get call.respond(HttpStatusCode.BadRequest,mapOf("error" to "Celular inválido"))
        Database.connection().use { c ->
            c.prepareStatement("""
                SELECT u.id user_id,a.id account_id,u.phone,u.full_name
                FROM izida_users u JOIN accounts a ON a.user_id=u.id
                WHERE u.phone=? AND u.status='ACTIVE' AND a.status='ACTIVE'
                LIMIT 1
            """.trimIndent()).use { s ->
                s.setString(1,phone)
                s.executeQuery().use { rs ->
                    if (!rs.next()) return@get call.respond(HttpStatusCode.NotFound,mapOf("error" to "Destinatario no encontrado"))
                    call.respond(RecipientResponse(
                        rs.getObject("user_id").toString(),
                        rs.getObject("account_id").toString(),
                        rs.getString("phone"),
                        rs.getString("full_name")
                    ))
                }
            }
        }
    }
}
