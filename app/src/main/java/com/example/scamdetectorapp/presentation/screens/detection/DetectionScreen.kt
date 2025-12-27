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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamdetectorapp.R
import com.example.scamdetectorapp.domain.model.DetectionMode
import com.example.scamdetectorapp.presentation.model.ScanUiModel
import com.example.scamdetectorapp.presentation.viewmodel.MainViewModel
import com.example.scamdetectorapp.presentation.viewmodel.ScanUiState

enum class ScreenStep { INPUT, SCANNING, RESULT }

@Composable
fun GenericDetectionFlow(
    mode: DetectionMode,
    title: String,
    placeholder: String,
    desc: String,
    keyboardType: KeyboardType,
    isMultiLine: Boolean = false,
    viewModel: MainViewModel = viewModel()
) {
    var step by remember { mutableStateOf(ScreenStep.INPUT) }
    var inputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()

    // 監聽 UI State 變化來決定 Step
    LaunchedEffect(uiState) {
        when (uiState) {
            is ScanUiState.Loading -> step = ScreenStep.SCANNING
            is ScanUiState.Success -> step = ScreenStep.RESULT
            is ScanUiState.Error -> {
                // 錯誤處理，這裡簡單跳到結果頁顯示錯誤，或者可以彈出 Toast
                // 為了配合現有 UI，我們將錯誤視為一種結果顯示
                step = ScreenStep.RESULT
            }
            is ScanUiState.Idle -> {
                if (step != ScreenStep.INPUT) step = ScreenStep.INPUT
            }
        }
    }

    fun startScan() {
        focusManager.clearFocus()
        viewModel.scan(mode, inputText)
    }

    fun reset() {
        inputText = ""
        viewModel.resetState()
        step = ScreenStep.INPUT
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(visible = step == ScreenStep.INPUT, enter = fadeIn(), exit = fadeOut()) {
            InputScreen(
                title = title,
                desc = desc,
                placeholder = placeholder,
                value = inputText,
                onValueChange = { inputText = it },
                onScan = { if (inputText.isNotBlank()) startScan() },
                keyboardType = keyboardType,
                isMultiLine = isMultiLine
            )
        }

        AnimatedVisibility(visible = step == ScreenStep.SCANNING, enter = fadeIn(), exit = fadeOut()) {
            ScanningScreen(onCancel = { viewModel.resetState() })
        }

        AnimatedVisibility(visible = step == ScreenStep.RESULT, enter = fadeIn(), exit = fadeOut()) {
            when (val state = uiState) {
                is ScanUiState.Success -> {
                    ResultScreen(
                        originalText = inputText,
                        result = state.result,
                        onBack = { reset() }
                    )
                }
                is ScanUiState.Error -> {
                    // 顯示錯誤畫面，這裡復用 ResultScreen 但顯示錯誤資訊
                    ResultScreen(
                        originalText = inputText,
                        result = ScanUiModel(
                            isSafe = true, // 錯誤時預設顯示安全或中立
                            score = 0,
                            title = state.title,
                            reasons = listOf(state.message)
                        ),
                        onBack = { reset() }
                    )
                }
                else -> {}
            }
        }
    }
}

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

@Composable
fun ResultScreen(originalText: String, result: ScanUiModel, onBack: () -> Unit) {
    val safeColor = colorResource(R.color.scam_safe_green)
    val riskColor = colorResource(R.color.scam_risk_red)
    val warningColor = colorResource(R.color.scam_gold_warning)
    val textWhite = MaterialTheme.colorScheme.onBackground
    val textGrey = colorResource(R.color.scam_text_grey)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val backgroundColor = MaterialTheme.colorScheme.background

    val statusColor = if (result.isSafe) safeColor else riskColor
    val icon = if (result.isSafe) Icons.Default.CheckCircle else Icons.Default.Warning

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = textWhite)
            }
            Spacer(Modifier.width(8.dp))
            Text("檢測結果", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textWhite)
        }

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text(result.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = statusColor)
                Spacer(Modifier.height(8.dp))

                Text(if(result.isSafe) "安全指數" else "風險指數", color = textGrey, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { result.score / 100f },
                    color = statusColor,
                    trackColor = backgroundColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Text("${result.score}%", color = textWhite, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("分析詳情", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textWhite)
        Spacer(Modifier.height(16.dp))

        result.reasons.forEach { reason ->
            Row(modifier = Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = if(result.isSafe) safeColor else warningColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(reason, color = textGrey, fontSize = 15.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("原始內容", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textWhite)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceColor, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(originalText, color = textGrey, fontSize = 14.sp)
        }

        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = surfaceColor)
            ) {
                Text("再測一次", color = textWhite)
            }

            Button(
                onClick = { },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if(result.isSafe) safeColor else riskColor)
            ) {
                Text(if(result.isSafe) "完成" else "回報詐騙", color = Color.White)
            }
        }
    }
}
