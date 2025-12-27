package com.example.scamdetectorapp.data.repository

import com.example.scamdetectorapp.data.model.*
import com.example.scamdetectorapp.data.remote.RetrofitClient
import com.example.scamdetectorapp.domain.model.DetectionMode
import com.example.scamdetectorapp.domain.model.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AntiFraudRepository {
    private val api = RetrofitClient.instance

    suspend fun scan(mode: DetectionMode, input: String): Result<ScanResult> = withContext(Dispatchers.IO) {
        try {
            val result = when (mode) {
                DetectionMode.PHONE -> {
                    val response = api.getData(phoneNumber = input)
                    if (response.success) {
                        val data = response.data
                        val riskLevel = data?.riskLevel ?: "NODATA"
                        ScanResult(
                            isRisk = riskLevel != "NODATA" && riskLevel != "SAFE",
                            riskLevel = riskLevel,
                            description = data?.description
                        )
                    } else throwApiException(response.toString())
                }
                DetectionMode.URL -> {
                    var urlToCheck = input.trim()
                    if (!urlToCheck.startsWith("http://") && !urlToCheck.startsWith("https://")) {
                        urlToCheck = "https://$urlToCheck"
                    }
                    val response = api.getUrlCheck(url = urlToCheck)
                    if (response.success) {
                        val data = response.data
                        val riskLevel = data?.riskLevel ?: "NODATA"
                        ScanResult(
                            isRisk = riskLevel != "NODATA" && riskLevel != "SAFE",
                            riskLevel = riskLevel,
                            description = data?.description,
                            threatType = data?.threatType
                        )
                    } else throwApiException(response.toString())
                }
                DetectionMode.TEXT -> {
                    val response = api.postAiCheck(body = AiCheckRequest(text = input))
                    if (response.success) {
                        val data = response.data
                        val riskLevel = data?.riskLevel ?: "NODATA"
                        ScanResult(
                            isRisk = riskLevel != "NODATA" && riskLevel != "SAFE",
                            riskLevel = riskLevel,
                            description = data?.description,
                            suggestion = data?.suggestion
                        )
                    } else throwApiException(response.toString())
                }
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun throwApiException(response: String): Nothing {
        throw Exception("API 回傳失敗: $response")
    }
}
