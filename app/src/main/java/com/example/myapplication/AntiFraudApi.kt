package com.example.myapplication

import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * 每一筆詐騙資料的內容
 */
data class FraudReport(
    val id: String? = null,
    val phoneNumber: String? = null,
    val riskLevel: String? = null,
    val description: String? = null
)

/**
 * 統一的 Response 包裝
 * 使用 JsonElement 讓 data 欄位可以接收 [] (陣列) 或 {} (物件)
 */
data class AntiFraudResponse(
    val success: Boolean,
    val version: String,
    val data: JsonElement
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
}
