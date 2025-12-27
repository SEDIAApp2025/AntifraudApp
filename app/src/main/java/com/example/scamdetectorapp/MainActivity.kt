package com.example.scamdetectorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource // 匯入這個來讀取 XML 顏色
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.outlined.Message
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.HttpException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScamGuardTheme {
                AppEntry()
            }
        }
    }
}

// ==================== 1. 主題設定 (讀取 XML Colors) ====================
@Composable
fun ScamGuardTheme(content: @Composable () -> Unit) {
    // 從 res/values/colors.xml 讀取顏色
    val darkBackground = colorResource(id = R.color.scam_background)
    val surfaceCard = colorResource(id = R.color.scam_surface)
    val primaryBlue = colorResource(id = R.color.scam_primary)
    val textWhite = colorResource(id = R.color.scam_text_white)

    // 設定 Material Theme 配色
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = darkBackground,
            surface = surfaceCard,
            primary = primaryBlue,
            onBackground = textWhite,
            onSurface = textWhite
        )
    ) {
        content()
    }
}

// ==================== 2. App 入口與閃屏頁邏輯 ====================
@Composable
fun AppEntry() {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
    } else {
        MainAppScreen()
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val darkBg = colorResource(R.color.scam_background)
    val primaryColor = colorResource(R.color.scam_primary)
    val textColor = colorResource(R.color.scam_text_white)
    val textGrey = colorResource(R.color.scam_text_grey)

    LaunchedEffect(Unit) {
        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), darkBg))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = "Logo",
                tint = primaryColor,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "SCAM GUARD",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Real Time Protection", color = textGrey, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(32.dp))
        }
    }
}

// ==================== 3. 主程式 (含底部導覽) ====================
@Composable
fun MainAppScreen() {
    var currentTab by remember { mutableStateOf("簡訊") }
    // 直接使用 Theme 裡的背景色
    val backgroundColor = MaterialTheme.colorScheme.background

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            CustomBottomBar(currentTab) { selected -> currentTab = selected }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                "網址" -> GenericDetectionFlow(
                    mode = DetectionMode.URL,
                    title = "檢測詐騙網址",
                    placeholder = "貼上網址，例如 https://...",
                    desc = "支援檢查釣魚網站、假冒連結",
                    keyboardType = KeyboardType.Uri
                )
                "電話" -> GenericDetectionFlow(
                    mode = DetectionMode.PHONE,
                    title = "檢測詐騙電話",
                    placeholder = "輸入電話號碼 (如 0912...)",
                    desc = "檢查常見詐騙客服、假警方電話",
                    keyboardType = KeyboardType.Phone
                )
                "簡訊" -> GenericDetectionFlow(
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

@Composable
fun CustomBottomBar(currentTab: String, onTabSelected: (String) -> Unit) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val greyColor = colorResource(R.color.scam_text_grey)

    NavigationBar(
        containerColor = surfaceColor,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("網址", Icons.Filled.Public, Icons.Outlined.Public),
            Triple("電話", Icons.Filled.Phone, Icons.Outlined.Phone),
            Triple("簡訊", Icons.AutoMirrored.Filled.Message, Icons.AutoMirrored.Outlined.Message)
        )

        items.forEach { (title, selectedIcon, unselectedIcon) ->
            val isSelected = currentTab == title
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(title) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) selectedIcon else unselectedIcon,
                        contentDescription = title,
                        tint = if (isSelected) primaryColor else greyColor
                    )
                },
                label = {
                    Text(title, color = if (isSelected) primaryColor else greyColor)
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

// ==================== 4. 核心檢測流程 ====================

enum class ScreenStep { INPUT, SCANNING, RESULT }
enum class DetectionMode { URL, PHONE, TEXT }

data class ScanResult(
    val isSafe: Boolean,
    val score: Int,
    val title: String,
    val reasons: List<String>
)

@Composable
fun GenericDetectionFlow(
    mode: DetectionMode,
    title: String,
    placeholder: String,
    desc: String,
    keyboardType: KeyboardType,
    isMultiLine: Boolean = false
) {
    var step by remember { mutableStateOf(ScreenStep.INPUT) }
    var inputText by remember { mutableStateOf("") }
    var resultData by remember { mutableStateOf<ScanResult?>(null) }
    val focusManager = LocalFocusManager.current

    // 模擬檢測邏輯
    fun startScan() {
        focusManager.clearFocus()
        step = ScreenStep.SCANNING
    }

    LaunchedEffect(step) {
        if (step == ScreenStep.SCANNING) {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://antifraud-gateway.lyc-dev.workers.dev/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val antiFraudApi = retrofit.create(AntiFraudApi::class.java)
                val gson = Gson()
                var isRisk = false
                val reasons = mutableListOf<String>()

                val response = when (mode) {
                    DetectionMode.PHONE -> antiFraudApi.getData(
                        apiKey = BuildConfig.CLOUDFLARE_API_KEY,
                        phoneNumber = inputText
                    )
                    DetectionMode.URL -> {
                        var urlToCheck = inputText.trim()
                        if (!urlToCheck.startsWith("http://") && !urlToCheck.startsWith("https://")) {
                            urlToCheck = "https://$urlToCheck"
                        }
                        antiFraudApi.getUrlCheck(
                            apiKey = BuildConfig.CLOUDFLARE_API_KEY,
                            url = urlToCheck
                        )
                    }
                    DetectionMode.TEXT -> antiFraudApi.postAiCheck(
                        apiKey = BuildConfig.CLOUDFLARE_API_KEY,
                        body = AiCheckRequest(text = inputText)
                    )
                }

                if (response.success) {
                    val jsonElement = response.data

                    if (jsonElement != null) {
                        when (mode) {
                            DetectionMode.PHONE -> {
                                // 單筆查詢回傳 Object
                                if (jsonElement.isJsonObject) {
                                    val report: FraudReport = gson.fromJson(jsonElement, FraudReport::class.java)
                                    val rLevel = report.riskLevel ?: "UNKNOWN"

                                    // 只有當 riskLevel 不是 UNKNOWN 且不是 LOW 時才視為風險
                                    if (!rLevel.equals("UNKNOWN", ignoreCase = true) && !rLevel.equals("LOW", ignoreCase = true)) {
                                        isRisk = true
                                        reasons.add("風險等級: $rLevel")
                                        report.description?.let { reasons.add(it) }
                                    }
                                }
                            }
                            DetectionMode.URL -> {
                                if (jsonElement.isJsonObject) {
                                    val jsonObj = jsonElement.asJsonObject
                                    val riskLevel = if (jsonObj.has("riskLevel") && !jsonObj.get("riskLevel").isJsonNull) jsonObj.get("riskLevel").asString else "UNKNOWN"
                                    val description = if (jsonObj.has("description") && !jsonObj.get("description").isJsonNull) jsonObj.get("description").asString else ""
                                    val threatType = if (jsonObj.has("threatType") && !jsonObj.get("threatType").isJsonNull) jsonObj.get("threatType").asString else ""

                                    // 只要不是 UNKNOWN 且不是 LOW，都視為風險 (HIGH, MEDIUM)
                                    if (!riskLevel.equals("UNKNOWN", ignoreCase = true) && !riskLevel.equals("LOW", ignoreCase = true)) {
                                        isRisk = true
                                        reasons.add("風險等級: $riskLevel")
                                        if (threatType.isNotEmpty()) reasons.add("威脅類型: $threatType")
                                        if (description.isNotEmpty()) reasons.add(description)
                                    }
                                }
                            }
                            DetectionMode.TEXT -> {
                                if (jsonElement.isJsonObject) {
                                    val jsonObj = jsonElement.asJsonObject
                                    val riskLevel = if (jsonObj.has("riskLevel") && !jsonObj.get("riskLevel").isJsonNull) jsonObj.get("riskLevel").asString else "UNKNOWN"
                                    val description = if (jsonObj.has("description") && !jsonObj.get("description").isJsonNull) jsonObj.get("description").asString else ""
                                    val suggestion = if (jsonObj.has("suggestion") && !jsonObj.get("suggestion").isJsonNull) jsonObj.get("suggestion").asString else ""

                                    // 只要不是 UNKNOWN 且不是 LOW，都視為風險 (HIGH, MEDIUM)
                                    if (!riskLevel.equals("UNKNOWN", ignoreCase = true) && !riskLevel.equals("LOW", ignoreCase = true)) {
                                        isRisk = true
                                        reasons.add("風險等級: $riskLevel")
                                        if (description.isNotEmpty()) reasons.add(description)
                                        if (suggestion.isNotEmpty()) reasons.add("建議: $suggestion")
                                    }
                                }
                            }
                        }
                    }

                    if (!isRisk) {
                        reasons.add("無詐騙特徵")
                        reasons.add("正規網域/號碼/內容")
                    }
                    
                    if (reasons.isEmpty()) {
                         reasons.add("無詳細資訊")
                    }

                    resultData = ScanResult(
                        isSafe = !isRisk,
                        score = if (isRisk) 30 else 95,
                        title = if (isRisk) "高風險威脅" else "安全內容",
                        reasons = reasons
                    )
                } else {
                    resultData = ScanResult(
                        isSafe = true,
                        score = 0,
                        title = "查詢失敗",
                        reasons = listOf("API 回傳失敗: ${response.version}")
                    )
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("API_ERROR", "HttpException: ${e.code()} $errorBody")
                resultData = ScanResult(
                    isSafe = true,
                    score = 0,
                    title = "伺服器錯誤 (${e.code()})",
                    reasons = listOf("伺服器發生問題", errorBody ?: "無詳細錯誤訊息")
                )
            } catch (e: com.google.gson.JsonSyntaxException) {
                Log.e("API_ERROR", "JsonSyntaxException: ${e.message}")
                resultData = ScanResult(
                    isSafe = true,
                    score = 0,
                    title = "資料格式錯誤",
                    reasons = listOf("API 回傳了非 JSON 格式的資料", e.message ?: "解析失敗")
                )
            } catch (e: java.net.SocketTimeoutException) {
                Log.e("API_ERROR", "SocketTimeoutException: ${e.message}")
                resultData = ScanResult(
                    isSafe = true,
                    score = 0,
                    title = "連線逾時",
                    reasons = listOf("伺服器回應太慢", "請稍後再試")
                )
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error: ${e.message}")
                resultData = ScanResult(
                    isSafe = true,
                    score = 0,
                    title = "連線錯誤",
                    reasons = listOf("無法連線至伺服器", "${e.javaClass.simpleName}: ${e.message}")
                )
            }
            step = ScreenStep.RESULT
        }
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
            ScanningScreen(onCancel = { step = ScreenStep.INPUT })
        }

        AnimatedVisibility(visible = step == ScreenStep.RESULT, enter = fadeIn(), exit = fadeOut()) {
            resultData?.let {
                ResultScreen(
                    originalText = inputText,
                    result = it,
                    onBack = {
                        inputText = ""
                        step = ScreenStep.INPUT
                    }
                )
            }
        }
    }
}

// ==================== 5. 子畫面實作 ====================

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
                    // ========== 這裡修正了 ==========
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
                    // ==============================
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
fun ResultScreen(originalText: String, result: ScanResult, onBack: () -> Unit) {
    // 從 XML 讀取顏色
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
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
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