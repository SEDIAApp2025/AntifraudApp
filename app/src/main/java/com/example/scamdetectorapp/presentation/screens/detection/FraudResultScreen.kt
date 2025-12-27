package com.example.scamdetectorapp.presentation.screens.detection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scamdetectorapp.R
import com.example.scamdetectorapp.presentation.model.ScanUiModel

@Composable
fun FraudResultScreen(originalText: String, result: ScanUiModel, onBack: () -> Unit) {
    val statusData = when {
        result.score > 75 -> Triple(
            "高風險威脅",
            colorResource(id = R.color.scam_risk_red),
            Icons.Default.Warning
        )
        result.score in 41..74 -> Triple(
            "中風險威脅",
            colorResource(id = R.color.scam_orange),
            Icons.Default.Warning
        )
        result.score in 1..40 -> Triple(
            "低風險威脅",
            colorResource(id = R.color.scam_safe_green),
            Icons.Filled.VerifiedUser
        )
        else -> Triple(
            "查無資料",
            colorResource(id = R.color.scam_neutral_gray),
            Icons.Default.Search
        )
    }

    val statusText = statusData.first
    val statusColor = statusData.second
    val statusIcon = statusData.third

    val textWhite = MaterialTheme.colorScheme.onBackground
    val textGrey = colorResource(R.color.scam_text_grey)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val backgroundColor = MaterialTheme.colorScheme.background

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
                Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text(statusText, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = statusColor)
                Spacer(Modifier.height(8.dp))

                Text(if(result.score > 0) "風險指數" else "", color = textGrey, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                if (result.score >= 0) {
                    LinearProgressIndicator(
                        progress = { result.score / 100f },
                        color = statusColor,
                        trackColor = backgroundColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                } else {
                    LinearProgressIndicator(
                        color = statusColor,
                        trackColor = backgroundColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }

                if(result.score >= 0) {
                    Text("${result.score}%", color = textWhite, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("分析詳情", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textWhite)
        Spacer(Modifier.height(16.dp))

        result.reasons.forEach { reason ->
            Row(modifier = Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
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
                colors = ButtonDefaults.buttonColors(containerColor = statusColor)
            ) {
                Text(if(result.score > 0) "回報詐騙" else "完成", color = Color.White)
            }
        }
    }
}
