package com.example.scamdetectorapp.domain.model

data class ScanResult(
    val riskLevel: String?,
    val description: String?,
    val threatType: String? = null,
    val suggestion: String? = null
)
