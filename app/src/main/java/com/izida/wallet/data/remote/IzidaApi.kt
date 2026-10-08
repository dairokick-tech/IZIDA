package com.izida.wallet.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
data class AccountResponse(val id:String,val userId:String,val currency:String,val status:String,val balance:String)

@Serializable
data class MovementResponse(val id:String,val transactionId:String,val type:String,val amount:String,val currency:String,val createdAt:String,val reference:String?)

class IzidaApi(private val baseUrl:String, private val json:Json=Json{ignoreUnknownKeys=true}) {
    suspend fun getAccount(accountId:String):AccountResponse =
        json.decodeFromString(get("/api/v1/accounts/$accountId"))
    suspend fun getMovements(accountId:String,limit:Int=50):List<MovementResponse> =
        json.decodeFromString(get("/api/v1/accounts/$accountId/movements?limit=$limit"))

    private suspend fun get(path:String):String = withContext(Dispatchers.IO) {
        val connection=URL(baseUrl.trimEnd('/')+path).openConnection() as HttpURLConnection
        connection.requestMethod="GET"
        connection.connectTimeout=8000
        connection.readTimeout=8000
        connection.setRequestProperty("Accept","application/json")
        try {
            val code=connection.responseCode
            val stream=if(code in 200..299) connection.inputStream else connection.errorStream
            val body=stream.bufferedReader().use{it.readText()}
            if(code !in 200..299) error("API HTTP $code: $body")
            body
        } finally { connection.disconnect() }
    }
}
