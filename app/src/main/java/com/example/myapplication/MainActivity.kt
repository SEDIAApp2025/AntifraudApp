package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ApiTestScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ApiTestScreen(modifier: Modifier = Modifier) {
    val gson = Gson()

    LaunchedEffect(Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://antifraud-gateway.lyc-dev.workers.dev/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val antiFraudApi = retrofit.create(AntiFraudApi::class.java)

        try {
            val response = antiFraudApi.getData(
                BuildConfig.CLOUDFLARE_API_KEY,
                    phoneNumber = "0987064507079"
                )

            if (response.success) {
                Log.d("AntiFraudApi", "Success! Version: ${response.version}")
                
                val jsonData = response.data
                
                when {
                    // 判斷是否為陣列 []
                    jsonData.isJsonArray -> {
                        val listType = object : TypeToken<List<FraudReport>>() {}.type
                        val reports: List<FraudReport> = gson.fromJson(jsonData, listType)
                        
                        Log.d("AntiFraudApi", "檢測到【多筆資料】，共 ${reports.size} 筆")
                        reports.forEachIndexed { index, report ->
                            Log.d("AntiFraudApi", " -> [#${index + 1}] $report")
                        }
                    }
                    // 判斷是否為單一物件 {}
                    jsonData.isJsonObject -> {
                        val report: FraudReport = gson.fromJson(jsonData, FraudReport::class.java)
                        Log.d("AntiFraudApi", "檢測到【單筆資料】: $report")
                    }
                    // 其他情況 (例如 null 或字串)
                    else -> {
                        Log.d("AntiFraudApi", "收到的資料格式非物件也非陣列: $jsonData")
                    }
                }
            } else {
                Log.e("AntiFraudApi", "API returned success=false")
            }

        } catch (e: Exception) {
            Log.e("AntiFraudApi", "Failed to fetch data: ${e.message}", e)
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Enhanced API Test Running... \nChecking for Object vs Array")
    }
}
