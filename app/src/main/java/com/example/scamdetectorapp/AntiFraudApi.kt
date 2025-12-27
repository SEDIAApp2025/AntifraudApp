package com.example.scamdetectorapp

import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

// 1. 定義後端回傳的基本結構
//使用 JsonElement 讓 data 欄位可以接收 [] (陣列) 或 {} (物件)
data class AntiFraudResponse(
    val success: Boolean,
    val version: String,
    val data: JsonElement?  // 使用 JsonElement 來處理不固定的格式
)
// 2. 定義詐騙報告的詳細內容
data class FraudReport(
    val id: String? = null,
    val phoneNumber: String? = null,
    val riskLevel: String? = null,
    val description: String? = null,
    val source: String?
)

data class AiCheckRequest(
    val text: String
)

interface AntiFraudApi {
    /**
     * @param apiKey 從 Header 傳入 API Key
     * @param phoneNumber 從 Query String 傳入電話號碼參數 (?phoneNumber=...)
     */
    @GET("api/cellphone")
    suspend fun getData(
        @Header("x-api-key") apiKey: String,
        @Query("phoneNumber") phoneNumber: String? = null,
        @Query("riskLevel") riskLevel: String? = null
    ): AntiFraudResponse

    @GET("api/url-check")
    suspend fun getUrlCheck(
        @Header("x-api-key") apiKey: String,
        @Query("url") url: String
    ): AntiFraudResponse

    @POST("api/ai-check")
    suspend fun postAiCheck(
        @Header("x-api-key") apiKey: String,
        @Body body: AiCheckRequest
    ): AntiFraudResponse
}
