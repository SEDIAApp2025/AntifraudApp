package com.example.scamdetectorapp.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.DataThresholding
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scamdetectorapp.R
import com.example.scamdetectorapp.data.repository.NewsRepository
import com.example.scamdetectorapp.data.repository.NewsType

/**
 * 首頁螢幕組件
 * 提供應用程式的主要入口，讓使用者選擇不同的檢測功能，並查看最新的防詐新聞預覽。
 *
 * @param onNavigateTo 導覽回呼函式，傳入目標頁面名稱
 */
@Composable
fun HomeScreen(onNavigateTo: (String) -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 讓首頁可以捲動，以容納下方的新聞預覽
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 頂部 Logo 區域 ---
        Spacer(modifier = Modifier.height(48.dp))
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(surfaceColor, CircleShape)
                .border(2.dp, primaryColor.copy(alpha = 0.5f), CircleShape),
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
            color = colorResource(R.color.scam_text_grey)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- 功能選擇區域 ---
        Text(
            text = "快速檢測",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = onSurfaceColor,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeatureCard(
            title = "網址檢測",
            desc = "檢查釣魚網站與惡意連結",
            icon = Icons.Outlined.Public,
            onClick = { onNavigateTo("網址") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureCard(
            title = "電話檢測",
            desc = "辨識騷擾與詐騙來電",
            icon = Icons.Outlined.Phone,
            onClick = { onNavigateTo("電話") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureCard(
            title = "簡訊檢測",
            desc = "分析可疑簡訊內容",
            icon = Icons.AutoMirrored.Outlined.Message,
            onClick = { onNavigateTo("簡訊") }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- 防詐新聞預覽區塊 ---
        NewsPreviewSection(onClick = { onNavigateTo("新聞") })
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * 功能卡片組件
 */
@Composable
private fun FeatureCard(
    title: String,
    desc: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = desc, fontSize = 12.sp, color = colorResource(R.color.scam_text_grey))
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = colorResource(R.color.scam_text_grey)
            )
        }
    }
}

/**
 * 首頁底部的新聞預覽區塊
 */
@Composable
private fun NewsPreviewSection(onClick: () -> Unit) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val textGrey = colorResource(R.color.scam_text_grey)

    // 修改：從共用 Repository 取得前兩則新聞
    val previewNews = NewsRepository.getPreviewNews()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "防詐資訊專區",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            )
            TextButton(onClick = onClick) {
                Text("查看更多", color = primaryColor, fontSize = 14.sp)
            }
        }

        // 根據取得的資料動態渲染卡片
        previewNews.forEachIndexed { index, news ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(10.dp))
            }

            Card(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                primaryColor.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // 根據新聞類型顯示不同的圖示
                        val icon = if (news.type == NewsType.TREND) Icons.Default.DataThresholding else Icons.Default.Newspaper
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = news.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = news.summary,
                            fontSize = 13.sp,
                            color = textGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
