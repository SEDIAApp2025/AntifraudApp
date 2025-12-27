package com.example.scamdetectorapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.scamdetectorapp.data.repository.AntiFraudRepository
import com.example.scamdetectorapp.domain.model.DetectionMode
import com.example.scamdetectorapp.domain.model.ScanResult
import com.example.scamdetectorapp.presentation.model.ScanUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Loading : ScanUiState
    data class Success(val result: ScanUiModel) : ScanUiState
    data class Error(val message: String, val title: String = "錯誤") : ScanUiState
}

class MainViewModel(
    private val repository: AntiFraudRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = ScanUiState.Idle
    }

    fun scan(mode: DetectionMode, input: String) {
        _uiState.value = ScanUiState.Loading
        val trimmedInput = input.trim()
        viewModelScope.launch {
            val result = repository.scan(mode, trimmedInput)
            result.fold(
                onSuccess = { scanResult ->
                    val uiModel = mapToUiModel(scanResult)
                    _uiState.value = ScanUiState.Success(uiModel)
                },
                onFailure = { e ->
                    val (title, message) = when (e) {
                        is HttpException -> "伺服器錯誤 (${e.code()})" to (e.response()?.errorBody()?.string() ?: "無詳細錯誤訊息")
                        is com.google.gson.JsonSyntaxException -> "資料格式錯誤" to "API 回傳了非 JSON 格式的資料"
                        is SocketTimeoutException -> "連線逾時" to "伺服器回應太慢，請稍後再試"
                        else -> "錯誤" to (e.message ?: "發生未知錯誤")
                    }
                    _uiState.value = ScanUiState.Error(message, title)
                }
            )
        }
    }

    private fun mapToUiModel(result: ScanResult): ScanUiModel {
        val rLevel = result.riskLevel

        val score = when (rLevel?.uppercase()) {
            "HIGH" -> 85
            "MEDIUM" -> 60
            "LOW" -> 20
            "SAFE" -> 10
            "NODATA" -> 0
            else -> 0
        }

        val reasons = mutableListOf<String>()
        val title: String

        when (rLevel?.uppercase()) {
            "HIGH", "MEDIUM", "LOW" -> {
                title = when (rLevel.uppercase()) {
                    "HIGH" -> "高風險威脅"
                    "MEDIUM" -> "中風險威脅"
                    else -> "低風險威脅"
                }
                reasons.add("風險等級: $rLevel")
                result.description?.takeIf { it.isNotEmpty() }?.let { reasons.add(it) }
                result.threatType?.takeIf { it.isNotEmpty() }?.let { reasons.add("威脅類型: $it") }
                result.suggestion?.takeIf { it.isNotEmpty() }?.let { reasons.add("建議: $it") }
            }
            "SAFE" -> {
                title = "安全內容"
                reasons.add("無詐騙特徵")
                reasons.add("正規網域/號碼/內容")
            }
            else -> { // Catches NODATA and any other case
                title = "查無資料"
                reasons.add("資料庫暫無此紀錄")
            }
        }

        return ScanUiModel(
            isSafe = rLevel == "SAFE" || rLevel == "NODATA",
            score = score,
            title = title,
            reasons = reasons
        )
    }


    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(AntiFraudRepository()) as T
            }
        }
    }
}
