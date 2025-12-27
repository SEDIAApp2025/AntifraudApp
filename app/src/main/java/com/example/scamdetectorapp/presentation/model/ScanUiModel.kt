package com.example.scamdetectorapp.presentation.model

data class ScanUiModel(
    val isSafe: Boolean,
    val score: Int,
    val title: String,
    val reasons: List<String>
)
