package com.example.scamdetectorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.scamdetector.ui.theme.ScamDetectorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScamDetectorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val tabs = listOf("網址", "電話", "簡訊")
    val icons = listOf(Icons.Filled.Link, Icons.Filled.Phone, Icons.Default.Message)

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title) },
                        selected = false, // 這裡簡化，實際可用狀態管理
                        onClick = { navController.navigate(title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "簡訊", modifier = Modifier.padding(innerPadding)) {
            composable("網址") { UrlDetectionScreen() }
            composable("電話") { PhoneDetectionScreen() }
            composable("簡訊") { SmsDetectionScreen() }
        }
    }
}

// ==================== 網址檢測 ====================
@Composable
fun UrlDetectionScreen() {
    var url by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Result?>(null) }

    DetectionLayout(
        title = "檢測詐騙網址",
        placeholder = "貼上網址，例如 https://...",
        value = url,
        onValueChange = { url = it },
        keyboardType = KeyboardType.Uri,
        onDetect = {
            // 模擬檢測結果（實際呼叫你的 API）
            result = Result(
                isSafe = url.contains("google.com"), // 示範用
                confidence = if (url.contains("google.com")) 95 else 87,
                reasons = if (url.contains("google.com")) listOf("知名正規域名")
                else listOf("可疑重導向", "不在白名單", "相似釣魚域名")
            )
        },
        result = result,
        onBack = { result = null }
    )
}

// ==================== 電話檢測 ====================
@Composable
fun PhoneDetectionScreen() {
    var phone by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Result?>(null) }

    DetectionLayout(
        title = "檢測詐騙電話",
        placeholder = "輸入電話號碼，例如 0912345678",
        value = phone,
        onValueChange = { phone = it },
        keyboardType = KeyboardType.Phone,
        onDetect = {
            result = Result(
                isSafe = phone.contains("1234"), // 示範
                confidence = 80,
                reasons = if (phone.contains("1234")) listOf("正常號碼")
                else listOf("常見詐騙客服號碼", "大量負面回報")
            )
        },
        result = result,
        onBack = { result = null }
    )
}

// ==================== 簡訊檢測 ====================
@Composable
fun SmsDetectionScreen() {
    var sms by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Result?>(null) }

    DetectionLayout(
        title = "檢測詐騙簡訊",
        placeholder = "貼上簡訊內容",
        value = sms,
        onValueChange = { sms = it },
        keyboardType = KeyboardType.Text,
        multiline = true,
        onDetect = {
            result = Result(
                isSafe = !sms.contains("匯款") && !sms.contains("驗證碼"),
                confidence = 90,
                reasons = if (sms.contains("匯款") || sms.contains("驗證碼"))
                    listOf("包含催款關鍵字", "要求提供驗證碼", "假冒銀行簡訊")
                else listOf("無可疑關鍵字", "正常語法")
            )
        },
        result = result,
        onBack = { result = null }
    )
}

// ==================== 共用檢測畫面 ====================
data class Result(val isSafe: Boolean, val confidence: Int, val reasons: List<String>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionLayout(
    title: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    multiline: Boolean = false,
    onDetect: () -> Unit,
    result: Result?,
    onBack: () -> Unit
) {
    if (result != null) {
        // 結果畫面
        ResultScreen(result = result, originalText = value, onBack = onBack)
    } else {
        // 輸入畫面
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)

                Spacer(Modifier.height(32.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text(placeholder) },
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = !multiline,
                    minLines = if (multiline) 6 else 1,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { if (value.isNotBlank()) onDetect() },
                    enabled = value.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("立即檢測", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ResultScreen(result: Result, originalText: String, onBack: () -> Unit) {
    val riskColor = if (result.isSafe) Color(0xFF388E3C) else Color(0xFFD32F2F)
    val riskText = if (result.isSafe) "安全" else "高風險"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(riskText, fontSize = 32.sp, color = riskColor, textAlign = TextAlign.Center)

            LinearProgressIndicator(
                progress = result.confidence / 100f,
                color = riskColor,
                modifier = Modifier.fillMaxWidth()
            )
            Text("${result.confidence}% 信心度", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("檢測說明", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    result.reasons.forEach {
                        Row {
                            Text("• ", color = riskColor)
                            Text(it)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("原始內容", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(originalText, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text("再測一次")
                }
                Button(
                    onClick = { /* 回報功能未來實作 */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("回報詐騙")
                }
            }
        }
    }
}
