package com.example.scamdetectorapp.data.remote

import com.example.scamdetectorapp.data.model.AiCheckRequest
import com.example.scamdetectorapp.data.model.AiCheckResult
import com.example.scamdetectorapp.data.model.AntiFraudResponse
import com.example.scamdetectorapp.data.model.FraudReport
import com.example.scamdetectorapp.data.model.UrlCheckResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AntiFraudApi {
    @GET("api/cellphone")
    suspend fun getData(
        @Query("phoneNumber") phoneNumber: String? = null,
        @Query("riskLevel") riskLevel: String? = null
    ): AntiFraudResponse<FraudReport>

    @GET("api/url-check")
    suspend fun getUrlCheck(
        @Query("url") url: String
    ): AntiFraudResponse<UrlCheckResult>

    @POST("api/ai-check")
    suspend fun postAiCheck(
        @Body body: AiCheckRequest
    ): AntiFraudResponse<AiCheckResult>
}
