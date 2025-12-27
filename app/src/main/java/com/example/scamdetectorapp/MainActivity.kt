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
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
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
import retrofit2.HttpException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.gson.JsonElement
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

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
    val navController = rememberNavController()
    // 1. 定義 Tabs，加入「首頁」
    val tabs = listOf("首頁", "網址", "電話", "簡訊")

    // 取得目前的路由，用來控制底部導覽列的變色
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // 傳入目前的路由給 BottomBar
            CustomBottomBar(
                currentTab = currentDestination?.route ?: "首頁",
                onTabSelected = { newTab ->
                    navController.navigate(newTab) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "首頁", // 2. 預設顯示首頁
            modifier = Modifier.padding(innerPadding)
        ) {
            // 3. 首頁路由：傳入 lambda 讓首頁按鈕可以控制跳轉
            composable("首頁") {
                HomeScreen(
                    onNavigateTo = { targetTab ->
                        navController.navigate(targetTab) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // 其他功能頁面
            composable("網址") {
                GenericDetectionFlow(
                    type = DetectionType.URL, // 傳入 URL 類型
                    title = "檢測詐騙網址",
                    placeholder = "貼上網址，例如 https://...",
                    desc = "支援檢查釣魚網站、假冒連結",
                    keyboardType = KeyboardType.Uri
                )
            }
            composable("電話") {
                GenericDetectionFlow(
                    type = DetectionType.PHONE, // 傳入 PHONE 類型
                    title = "檢測詐騙電話",
                    placeholder = "輸入電話號碼 (如 0912...)",
                    desc = "檢查常見詐騙客服、假警方電話",
                    keyboardType = KeyboardType.Phone
                )
            }
            composable("簡訊") {
                GenericDetectionFlow(
                    type = DetectionType.SMS, // 傳入 SMS 類型
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
        // 更新列表：加入首頁
        val items = listOf(
            Triple("首頁", Icons.Filled.Home, Icons.Outlined.Home),
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

// 定義檢測類型，方便管理
enum class DetectionType { PHONE, URL, SMS }

@Composable
fun GenericDetectionFlow(
    type: DetectionType, // 新增：傳入檢測類型
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
    val gson = remember { com.google.gson.Gson() }

    fun startScan() {
        focusManager.clearFocus()
        step = ScreenStep.SCANNING
    }

    LaunchedEffect(step) {
        if (step == ScreenStep.SCANNING) {
            try {
                // 從 BuildConfig 讀取 API Key
                val apiKey = BuildConfig.CLOUDFLARE_API_KEY

                // ========== 1. 根據類型呼叫對應的 API ==========
                val response = when (type) {
                    DetectionType.PHONE -> RetrofitClient.api.getData(
                        apiKey = apiKey,
                        phoneNumber = inputText
                    )
                    DetectionType.URL -> RetrofitClient.api.getUrlCheck(
                        apiKey = apiKey,
                        url = inputText
                    )
                    DetectionType.SMS -> RetrofitClient.api.postAiCheck(
                        apiKey = apiKey,
                        body = AiCheckRequest(text = inputText)
                    )
                }

                // ========== 2. 解析資料 (通用邏輯) ==========
                val reports = mutableListOf<FraudReport>()
                val jsonData = response.data // 使用你定義的 JsonElement?

                if (response.success && jsonData != null) {
                    when {
                        jsonData.isJsonArray -> {
                            val listType = object : com.google.gson.reflect.TypeToken<List<FraudReport>>() {}.type
                            val list: List<FraudReport> = gson.fromJson(jsonData, listType)
                            reports.addAll(list)
                        }
                        jsonData.isJsonObject -> {
                            val report: FraudReport = gson.fromJson(jsonData, FraudReport::class.java)
                            reports.add(report)
                        }
                    }
                }

                // ========== 3. 轉換為 UI 顯示 ==========
                // 注意：這裡配合你的 FraudReport 欄位名稱 (riskLevel)
                val isSafe = reports.isEmpty() || reports.all {
                    val level = it.riskLevel?.lowercase()
                    level == "low" || level == "safe" || level == null
                }
                val riskCount = reports.size

                val displayReasons = if (reports.isNotEmpty()) {
                    reports.map {
                        // 組合顯示：來源 - 描述
                        "${it.source ?: "未知來源"}: ${it.description ?: "可疑活動"}"
                    }
                } else if (response.success) {
                    listOf("資料庫中無此記錄", "未發現異常")
                } else {
                    listOf("查詢無結果")
                }

                resultData = ScanResult(
                    isSafe = isSafe,
                    score = if (isSafe) 10 else 85 + (riskCount * 2),
                    title = if (isSafe) "未發現威脅" else "發現潛在風險",
                    reasons = displayReasons
                )

            } catch (e: Exception) {
                e.printStackTrace()
                resultData = ScanResult(
                    isSafe = true,
                    score = 0,
                    title = "檢測無法完成",
                    reasons = listOf("網路連線錯誤", e.message ?: "未知錯誤")
                )
            } finally {
                step = ScreenStep.RESULT
            }
        }
    }

    // ... (UI 佈局 Box/AnimatedVisibility 保持不變，直接複製原本的即可) ...
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
// ==================== 6. 首頁實作 ====================
@Composable
fun HomeScreen(onNavigateTo: (String) -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ========== 1. 頂部 Logo 區域 ==========
        Spacer(modifier = Modifier.height(48.dp))
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(surfaceColor, androidx.compose.foundation.shape.CircleShape)
                .border(2.dp, primaryColor.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = "Logo",
                tint = primaryColor,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SCAM GUARD",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = onSurfaceColor
        )
        Text(
            text = "您的全方位防詐護盾",
            fontSize = 14.sp,
            color = androidx.compose.ui.res.colorResource(R.color.scam_text_grey)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // ========== 2. 三大功能卡片 ==========
        Text(
            text = "選擇檢測項目",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = onSurfaceColor,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 網址卡片
        FeatureCard(
            title = "網址檢測",
            desc = "檢查釣魚網站與惡意連結",
            icon = Icons.Outlined.Public,
            onClick = { onNavigateTo("網址") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 電話卡片
        FeatureCard(
            title = "電話檢測",
            desc = "辨識騷擾與詐騙來電",
            icon = Icons.Outlined.Phone,
            onClick = { onNavigateTo("電話") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 簡訊卡片
        FeatureCard(
            title = "簡訊檢測",
            desc = "分析可疑簡訊內容",
            icon = Icons.AutoMirrored.Outlined.Message,
            onClick = { onNavigateTo("簡訊") }
        )
    }
}

// 抽取出來的卡片元件，讓程式碼更乾淨
@Composable
fun FeatureCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 圖示背景
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 文字內容
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = desc, fontSize = 12.sp, color = androidx.compose.ui.res.colorResource(R.color.scam_text_grey))
            }

            // 箭頭
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = androidx.compose.ui.res.colorResource(R.color.scam_text_grey)
            )
        }
    }
}
// ==================== 7. API 與資料結構定義 (補在檔案最下方) ====================
object RetrofitClient {
    // 請確認這是你的 Cloudflare Worker 或後端網址
    private const val BASE_URL = "https://antifraud-gateway.lyc-dev.workers.dev/"

    val api: AntiFraudApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(AntiFraudApi::class.java)
    }
}


