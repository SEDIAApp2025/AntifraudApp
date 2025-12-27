package com.example.scamdetectorapp.data.model

/**
 * 每一筆詐騙資料的內容
 */
data class FraudReport(
    val id: String? = null,
    val phoneNumber: String? = null,
    val riskLevel: String? = null,
    val description: String? = null
)
