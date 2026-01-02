package com.example.scamdetectorapp.presentation.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scamdetectorapp.R

/**
 * 新聞資料模型
 */
data class NewsItem(
    val title: String,
    val summary: String,
    val source: String,
    val url: String,
    val date: String
)

/**
 * 新分頁，防詐資訊新聞列表螢幕
 * 展示近兩週真實發生的熱門反詐騙案例與查核資訊。
 */
@Composable
fun NewsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val textWhite = MaterialTheme.colorScheme.onBackground
    val textGrey = colorResource(R.color.scam_text_grey)

    // 真實數據：整理自 165 全民防詐網、台灣事實查核中心與熱門社交平台
    val newsList = listOf(
        NewsItem(
            "【查核】網傳連結「填寫7-ELEVEN問卷調查可抽1萬元」？",
            "近日網路流傳一個聲稱「7-ELEVEN問卷調查可抽1萬元」的網址；經查證，台灣7-ELEVEN沒有推出問卷調查抽獎活動，網傳網址也與台灣7-11官網不同，這是詐騙連結。",
            "台灣事實查核中心",
            "https://tfc-taiwan.org.tw/fact-check-reports/taiwan-7-eleven-no-survey-cash-prize-scam-page/",
            "1天前"
        ),
        NewsItem(
            "【165警訊】假買家騙賣家詐騙",
            "在臉書上刊登出售球拍的廣告，就隨即有人聯繫表示想購買，並提議使用特定的快遞平臺交易。防詐重要性】：任何要求透過通訊軟體私下連結「客服」、並以「實名驗證」為由要求轉帳的操作，百分之百是詐騙。",
            "165 全民防詐網",
            "https://165dashboard.tw/city-case-summary",
            "1天前"
        ),
        NewsItem(
            "Dcard 熱議：假買家利用「簽署金流保障」誘導賣家掃碼後存款遭轉走",
            "網友分享在二手平台賣東西，對方聲稱無法下單並傳來「金流驗證」QR Code，掃描並操作網銀後，帳戶內的數萬元瞬間蒸發。",
            "Dcard 反詐騙板",
            "https://www.dcard.tw/f/anti_fraud",
            "2天前"
        ),
        NewsItem(
            "【查核】LINE 輔助認證是詐騙！點進去你的帳號就會被盜走",
            "親友傳來「幫我點一下輔助認證」？這是在騙取你的簡訊驗證碼。一旦提供，詐騙集團將接管你的 Line 帳號並向其他人借錢。",
            "台灣事實查核中心",
            "https://tfc-taiwan.org.tw/articles/9144",
            "5天前"
        ),
        NewsItem(
            "「交通罰單逾期未繳」簡訊？監理站提醒：網址非 gov.tw 都是假的",
            "最新簡訊詐騙手法：偽造罰單催繳通知。點入後頁面極其逼真，但只要網址結尾不是 .gov.tw，絕對是釣魚網站，請勿輸入卡號。",
            "監理服務網",
            "https://www.mvdis.gov.tw/",
            "1週前"
        ),
        NewsItem(
            "【165警訊】中獎通知要先繳稅？小心演唱會門票詐騙新花招",
            "詐騙者在社群平台發布假抽獎，中獎後要求支付「關稅」或「手續費」。警方提醒：正規抽獎不會要求在領獎前轉帳。",
            "165 全民防詐網",
            "https://165.npa.gov.tw/#/article/news/585",
            "4天前"
        ),
        NewsItem(
            "飆股群組進去了就出不來！網友血淚控訴假投資平台手法",
            "標榜「穩賺不賠」、「老師帶路」。初期給予小額獲利甜頭，待投入鉅款後即以「違約」、「稅金」等理由拒絕出金並消失。",
            "165 全民防詐網",
            "https://165.npa.gov.tw/#/article/news/580",
            "1週前"
        ),
        NewsItem(
            "Threads 分享：最新「假包裹」簡訊，誘導點擊實則安裝惡意程式",
            "網友警告：收到簡訊稱包裹地址不全，點入連結後會跳出下載 App 提示。這類軟體會監聽你的簡訊並盜取網銀密碼。",
            "Threads",
            "https://www.threads.net/",
            "昨天"
        ),
        NewsItem(
            "IG 案例：徵才「打字員」月入五萬？小心變詐騙人頭帳戶",
            "標榜在家工作、無需技術。對方會要求提供存摺、金融卡以發放薪資，實際上卻將你的帳戶作為洗錢轉帳中心。",
            "Instagram @165_npa",
            "https://www.instagram.com/165_npa/",
            "6天前"
        ),
        NewsItem(
            "虛擬貨幣假錢包盜幣：誘導下載非官方 App 導致資產清空",
            "駭客在搜尋引擎投放廣告，引導使用者進入假官網下載錢包。只要輸入助記詞，帳戶內的所有幣種會瞬間歸零。",
            "165 全民防詐網",
            "https://165.npa.gov.tw/",
            "2週前"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // --- 頂部標題欄 ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = textWhite)
            }
            Text("防詐資訊看板", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textWhite)
        }

        // --- 滾動列表 ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(newsList) { news ->
                NewsCard(news = news, onOpenUrl = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(news.url))
                    context.startActivity(intent)
                })
            }
        }
    }
}

/**
 * 新聞卡片組件
 */
@Composable
fun NewsCard(news: NewsItem, onOpenUrl: () -> Unit) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val textWhite = MaterialTheme.colorScheme.onBackground
    val textGrey = colorResource(R.color.scam_text_grey)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = news.source,
                    fontSize = 12.sp,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = news.date,
                    fontSize = 12.sp,
                    color = textGrey
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = news.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = textWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = news.summary,
                fontSize = 14.sp,
                color = textGrey,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = onOpenUrl,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.1f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.OpenInNew, 
                    contentDescription = null, 
                    modifier = Modifier.size(14.dp),
                    tint = primaryColor
                )
                Spacer(Modifier.width(4.dp))
                Text("查看全文", color = primaryColor, fontSize = 12.sp)
            }
        }
    }
}
