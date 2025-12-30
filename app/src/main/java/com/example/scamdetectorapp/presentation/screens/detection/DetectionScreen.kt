package com.example.scamdetectorapp.presentation.screens.detection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamdetectorapp.R
import com.example.scamdetectorapp.domain.model.DetectionMode
import com.example.scamdetectorapp.presentation.model.ScanUiModel
import com.example.scamdetectorapp.presentation.viewmodel.MainViewModel
import com.example.scamdetectorapp.presentation.viewmodel.ScanUiState

/**
 * 定義畫面顯示的四個階段
 * INPUT: 輸入內容階段
 * SCANNING: 正在連網檢測階段
 * RESULT: 顯示檢測報告階段
 * ERROR: 檢測過程中發生錯誤或失敗時的階段
 */
enum class ScreenStep { INPUT, SCANNING, RESULT, ERROR }

@Composable
fun GenericDetectionFlow(
    mode: DetectionMode,
    title: String,
    placeholder: String,
    desc: String,
    keyboardType: KeyboardType,
    isMultiLine: Boolean = false,
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)
) {
    
    // 【狀態隔離】根據模式監聽獨立的 UI 狀態流，避免三個功能頁面互相干擾
    val stateFlow = viewModel.getState(mode)
    val uiState by stateFlow.collectAsStateWithLifecycle()

    var step by remember(mode) { // 當 mode 改變時，重新計算初始 step
        val initialStep = when (stateFlow.value) {
            is ScanUiState.Loading -> ScreenStep.SCANNING
            is ScanUiState.Success -> ScreenStep.RESULT
            is ScanUiState.Error -> ScreenStep.ERROR
            else -> ScreenStep.INPUT
        }
        mutableStateOf(initialStep)
    }

    // 【狀態提升】監聽輸入內容
    val inputText by viewModel.getInput(mode).collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    /**
     * 副作用監聽：當 ViewModel 的資料狀態改變時，驅動 UI 切換到對應的步驟
     */
    LaunchedEffect(uiState) {
        when (uiState) {
            is ScanUiState.Loading -> step = ScreenStep.SCANNING
            is ScanUiState.Success -> step = ScreenStep.RESULT
            is ScanUiState.Error -> step = ScreenStep.ERROR
            is ScanUiState.Idle -> {
                // 如果狀態回到閒置，且目前不在輸入頁，則強行跳回輸入頁
                if (step != ScreenStep.INPUT) step = ScreenStep.INPUT
            }
        }
    }

    /**
     * 執行檢測：收起鍵盤並觸發 ViewModel 的 scan 邏輯
     */
    fun startScan() {
        focusManager.clearFocus()
        viewModel.scan(mode, inputText)
    }

    /**
     * 重置：清空輸入並通知 ViewModel 回歸閒置狀態
     */
    fun reset() {
        viewModel.setInput(mode, "") // 使用 ViewModel 管理輸入狀態
        viewModel.resetState(mode)
        step = ScreenStep.INPUT
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 第一階段：輸入頁面
        AnimatedVisibility(visible = step == ScreenStep.INPUT, enter = fadeIn(), exit = fadeOut()) {
            InputScreen(
                title = title,
                desc = desc,
                placeholder = placeholder,
                value = inputText,
                onValueChange = { viewModel.setInput(mode, it) }, // 更新 ViewModel 中的狀態
                onScan = { if (inputText.isNotBlank()) startScan() },
                keyboardType = keyboardType,
                isMultiLine = isMultiLine
            )
        }

        // 第二階段：掃描中動畫頁面
        AnimatedVisibility(visible = step == ScreenStep.SCANNING, enter = fadeIn(), exit = fadeOut()) {
            ScanningScreen(onCancel = { viewModel.resetState(mode) })
        }

        // 第三階段：檢測結果頁面
        AnimatedVisibility(visible = step == ScreenStep.RESULT, enter = fadeIn(), exit = fadeOut()) {
            if (uiState is ScanUiState.Success) {
                FraudResultScreen(
                    originalText = inputText,
                    result = (uiState as ScanUiState.Success).result,
                    onBack = { reset() }
                )
            }
        }

        AnimatedVisibility(visible = step == ScreenStep.ERROR, enter = fadeIn(), exit = fadeOut()) {
            if (uiState is ScanUiState.Error) {
                ErrorScreen(
                    title = (uiState as ScanUiState.Error).title,
                    message = (uiState as ScanUiState.Error).message,
                    onBack = { reset() }
                )
            }
        }
    }
}

@Composable
fun ErrorScreen(title: String, message: String, onBack: () -> Unit) {
    val textWhite = MaterialTheme.colorScheme.onBackground
    val textGrey = colorResource(R.color.scam_text_grey)
    val surfaceColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = textWhite)
            }
            Spacer(Modifier.width(8.dp))
            Text("檢測失敗", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textWhite)
        }
        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Text(message, color = textGrey, textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = surfaceColor)
        ) {
            Text("返回", color = textWhite)
        }
    }
}

/**
 * 輸入頁面組件
 */
@Composable
fun InputScreen(
    title: String,
    desc: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    onScan: () -> Unit,
    keyboardType: KeyboardType,
    isMultiLine: Boolean
) {
    val clipboardManager = LocalClipboardManager.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val textWhite = MaterialTheme.colorScheme.onBackground
    val textGrey = colorResource(R.color.scam_text_grey)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val backgroundColor = MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textWhite)
        Spacer(modifier = Modifier.height(8.dp))
        Text(desc, color = textGrey, fontSize = 14.sp, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text(placeholder, color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isMultiLine) 150.dp else 60.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = textWhite,
                        unfocusedTextColor = textWhite,
                        focusedContainerColor = backgroundColor,
                        unfocusedContainerColor = backgroundColor,
                        disabledContainerColor = backgroundColor,
                        errorContainerColor = backgroundColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
                    singleLine = !isMultiLine
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        clipboardManager.getText()?.text?.let { onValueChange(it) }
                    },
                    modifier = Modifier.align(Alignment.End),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
                ) {
                    Icon(Icons.Outlined.ContentPaste, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("貼上內容", color = primaryColor)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onScan,
            enabled = value.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                disabledContainerColor = surfaceColor
            )
        ) {
            Text("立即檢測", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * 掃描動畫頁面組件
 */
@Composable
fun ScanningScreen(onCancel: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val textGrey = colorResource(R.color.scam_text_grey)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(120.dp),
                color = primaryColor,
                strokeWidth = 8.dp
            )
            Icon(Icons.Default.Search, contentDescription = null, tint = primaryColor, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("正在分析威脅...", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("正在比對雲端詐騙資料庫", fontSize = 14.sp, color = textGrey, modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(48.dp))
        TextButton(onClick = onCancel) {
            Text("取消", color = textGrey)
        }
    }
}
