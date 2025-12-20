
package com.example.myapplication

import retrofit2.http.GET
import retrofit2.http.Query

interface CloudflareApi {
    @GET("")
    suspend fun getZones(
        @Query("name") zoneName: String? = null // Removed the authorization header
    ): ZonesResponse
}

data class ZonesResponse(
    val result: List<Zone>
)

data class Zone(
    val id: String,
    val name: String
)
