package com.example.myapplication

import retrofit2.http.GET

/**
 * Data class to match the JSON response structure from the anti-fraud API.
 */
data class FraudReport(
    val id: String,
    val source_number: String,
    val category: String,
    val description: String
)

/**
 * Retrofit interface for the anti-fraud API.
 */
interface AntiFraudApi {
    @GET("api/data")
    suspend fun getData(): List<FraudReport>
}
