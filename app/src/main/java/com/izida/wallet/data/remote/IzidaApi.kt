package com.izida.wallet.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable data class LoginRequest(val phone:String,val pin:String)
@Serializable data class LoginResponse(val userId:String,val fullName:String,val phone:String,val token:String)
@Serializable data class AccountResponse(val id:String,val userId:String,val currency:String,val status:String,val balance:String)
@Serializable data class MovementResponse(val id:String,val transactionId:String,val type:String,val amount:String,val currency:String,val createdAt:String,val reference:String?)

class IzidaApi(private val baseUrl:String, private val json:Json=Json{ignoreUnknownKeys=true}) {
    @Volatile private var bearerToken:String?=null
    fun setToken(token:String?) { bearerToken=token }
    suspend fun login(phone:String,pin:String):LoginResponse =
        json.decodeFromString(post("/api/v1/auth/login", json.encodeToString(LoginRequest(phone,pin))))
    suspend fun getAccount(accountId:String):AccountResponse = json.decodeFromString(get("/api/v1/accounts/$accountId"))
    suspend fun getMovements(accountId:String,limit:Int=50):List<MovementResponse> = json.decodeFromString(get("/api/v1/accounts/$accountId/movements?limit=$limit"))

    private suspend fun get(path:String):String = request("GET",path,null)
    private suspend fun post(path:String,body:String):String = request("POST",path,body)

    private suspend fun request(method:String,path:String,body:String?):String = withContext(Dispatchers.IO) {
        val connection=URL(baseUrl.trimEnd('/')+path).openConnection() as HttpURLConnection
        connection.requestMethod=method; connection.connectTimeout=8000; connection.readTimeout=8000
        connection.setRequestProperty("Accept","application/json")
        bearerToken?.let { connection.setRequestProperty("Authorization","Bearer $it") }
        if(body!=null){ connection.doOutput=true; connection.setRequestProperty("Content-Type","application/json"); connection.outputStream.use{it.write(body.toByteArray(Charsets.UTF_8))} }
        try {
            val code=connection.responseCode
            val stream=if(code in 200..299) connection.inputStream else connection.errorStream
            val response=stream?.bufferedReader()?.use{it.readText()} ?: ""
            if(code !in 200..299) error("API HTTP $code: $response")
            response
        } finally { connection.disconnect() }
    }
}
