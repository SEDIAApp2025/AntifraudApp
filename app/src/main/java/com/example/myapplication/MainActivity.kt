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
import okhttp3.OkHttpClient
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

    LaunchedEffect(Unit) {
        // The new API token from your screenshot
        val apiToken = "cMR52aectW6PpYRZZEnXeSBBrLK3rNnBEvwuzXxVXTL9gTTRRinqG4q6oFx7WTSYw7smSUXsKWx956WipXbvCKB9JncoPWUtdYw9"

        // Create an OkHttp client with an interceptor to add the x-api-key header
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("x-api-key", apiToken) // Use the correct header key: x-api-key
                    .method(original.method, original.body)

                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()

        // Configure Retrofit with the new base URL and the custom OkHttp client
        val retrofit = Retrofit.Builder()
            .baseUrl("https://antifraud-gateway.lyc-dev.workers.dev/") // New base URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create an instance of our new API interface
        val antiFraudApi = retrofit.create(AntiFraudApi::class.java)

        try {
            // Call the API
            val reports: List<FraudReport> = antiFraudApi.getData()

            // Log the result to the console (Logcat)
            Log.d("AntiFraudApi", "Successfully fetched ${reports.size} reports.")
            reports.forEachIndexed { index, report ->
                Log.d("AntiFraudApi", "Report #${index + 1}: $report")
            }

        } catch (e: Exception) {
            Log.e("AntiFraudApi", "Error fetching data", e)
        }
    }

    // Simple UI to show that the app is running
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Fetching data... Check Logcat for results.")
    }
}
