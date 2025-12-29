package com.example.scamdetectorapp.presentation.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.scamdetectorapp.domain.model.DetectionMode
import com.example.scamdetectorapp.presentation.components.CustomBottomBar
import com.example.scamdetectorapp.presentation.screens.detection.GenericDetectionFlow

@Composable
fun MainAppScreen() {
    var currentTab by remember { mutableStateOf("網址") }
    val backgroundColor = MaterialTheme.colorScheme.background

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            CustomBottomBar(currentTab) { selected -> currentTab = selected }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                "網址" -> key(DetectionMode.URL) { // 使用 key 來重置狀態
                    GenericDetectionFlow(
                        mode = DetectionMode.URL,
                        title = "檢測詐騙網址",
                        placeholder = "貼上網址，例如 https://...",
                        desc = "支援檢查釣魚網站、假冒連結",
                        keyboardType = KeyboardType.Uri
                    )
                }
                "電話" -> key(DetectionMode.PHONE) { // 使用 key 來重置狀態
                    GenericDetectionFlow(
                        mode = DetectionMode.PHONE,
                        title = "檢測詐騙電話",
                        placeholder = "輸入電話號碼 (如 0912...)",
                        desc = "檢查常見詐騙客服、假警方電話",
                        keyboardType = KeyboardType.Phone
                    )
                }
                "簡訊" -> key(DetectionMode.TEXT) { // 使用 key 來重置狀態
                    GenericDetectionFlow(
                        mode = DetectionMode.TEXT,
                        title = "檢測詐騙簡訊",
                        placeholder = "貼上簡訊內容...",
                        desc = "分析關鍵字、假連結、催款語法",
                        keyboardType = KeyboardType.Text,
                        isMultiLine = true
                    )
                }
            }
        }
    }
}
