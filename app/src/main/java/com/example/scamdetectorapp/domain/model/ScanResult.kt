package com.example.scamdetectorapp.domain.model

data class ScanResult(
    val riskLevel: String,
    val description: String? = null,
    val threatType: String? = null,
    val suggestion: String? = null
) {
    val isRisk: Boolean
        get() = !riskLevel.equals("UNKNOWN", ignoreCase = true) && !riskLevel.equals("LOW", ignoreCase = true)
}
