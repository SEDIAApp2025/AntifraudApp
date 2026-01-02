package com.example.scamdetectorapp.presentation.screens.detection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamdetectorapp.R
import com.example.scamdetectorapp.domain.model.DetectionMode
import com.example.scamdetectorapp.presentation.viewmodel.MainViewModel
import com.example.scamdetectorapp.presentation.viewmodel.ScanUiState

/**
 * 定義畫面顯示的四個階段
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
    val stateFlow = viewModel.getState(mode)
    val uiState by stateFlow.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var step by remember(mode) {
        val initialStep = when (stateFlow.value) {
            is ScanUiState.Loading -> ScreenStep.SCANNING
            is ScanUiState.Success -> ScreenStep.RESULT
            is ScanUiState.Error -> ScreenStep.ERROR
            else -> ScreenStep.INPUT
        }
        mutableStateOf(initialStep)
    }

    val viewModelInput by viewModel.getInput(mode).collectAsStateWithLifecycle()

    // 【關鍵修正】使用 TextFieldValue 替代 String，以支援實體鍵盤中文組合輸入
    var localTextValue by remember(mode) { mutableStateOf(TextFieldValue(viewModelInput)) }

    LaunchedEffect(viewModelInput) {
        if (localTextValue.text != viewModelInput) {
            localTextValue = TextFieldValue(viewModelInput)
        }
    }

    // 當分頁切換（mode 改變）或元件銷毀時，確保收起鍵盤
    DisposableEffect(mode) {
        onDispose {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ScanUiState.Loading -> step = ScreenStep.SCANNING
            is ScanUiState.Success -> step = ScreenStep.RESULT
            is ScanUiState.Error -> step = ScreenStep.ERROR
            is ScanUiState.Idle -> {
                if (step != ScreenStep.INPUT) step = ScreenStep.INPUT
            }
        }
    }

    fun startScan() {
        focusManager.clearFocus()
        keyboardController?.hide()
        viewModel.scan(mode, localTextValue.text)
    }

    fun reset() {
        viewModel.setInput(mode, "")
        localTextValue = TextFieldValue("")
        viewModel.resetState(mode)
        step = ScreenStep.INPUT
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // 點擊背景收起鍵盤
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
    ) {
        AnimatedVisibility(visible = step == ScreenStep.INPUT, enter = fadeIn(), exit = fadeOut()) {
            InputScreen(
                title = title,
                desc = desc,
                placeholder = placeholder,
                value = localTextValue,
                onValueChange = {
                    localTextValue = it
                    viewModel.setInput(mode, it.text)
                },
                onScan = { if (localTextValue.text.isNotBlank()) startScan() },
                keyboardType = when (mode) {
                    DetectionMode.URL -> KeyboardType.Uri
                    DetectionMode.PHONE -> KeyboardType.Number
                    DetectionMode.TEXT -> KeyboardType.Text
                },
                isMultiLine = isMultiLine
            )
        }

        AnimatedVisibility(visible = step == ScreenStep.SCANNING, enter = fadeIn(), exit = fadeOut()) {
            ScanningScreen(onCancel = { viewModel.resetState(mode) })
        }

        AnimatedVisibility(visible = step == ScreenStep.RESULT, enter = fadeIn(), exit = fadeOut()) {
            if (uiState is ScanUiState.Success) {
                FraudResultScreen(
                    originalText = localTextValue.text,
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

@Composable
fun InputScreen(
    title: String,
    desc: String,
    placeholder: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onScan: () -> Unit,
    keyboardType: KeyboardType,
    isMultiLine: Boolean
) {
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val textWhite = MaterialTheme.colorScheme.onBackground
    val textGrey = colorResource(R.color.scam_text_grey)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val backgroundColor = MaterialTheme.colorScheme.background
    val focusRequester = remember { FocusRequester() }


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
                        .height(if (isMultiLine) 150.dp else 60.dp)
                        .focusRequester(focusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = textWhite,
                        unfocusedTextColor = textWhite,
                        focusedContainerColor = backgroundColor,
                        unfocusedContainerColor = backgroundColor,
                        disabledContainerColor = backgroundColor,
                        errorContainerColor = backgroundColor,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = if(isMultiLine) ImeAction.Default else ImeAction.Done,
                        autoCorrectEnabled = true
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (value.text.isNotBlank()) onScan()
                            else {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        }
                    ),
                    singleLine = !isMultiLine
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        clipboardManager.getText()?.text?.let { onValueChange(TextFieldValue(it)) }
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
            enabled = value.text.isNotBlank(),
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
