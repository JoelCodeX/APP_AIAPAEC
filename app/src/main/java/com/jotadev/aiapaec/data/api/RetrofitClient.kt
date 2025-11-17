package com.jotadev.aiapaec.data.api

import android.os.Build
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.jotadev.aiapaec.data.storage.TokenStorage
import okhttp3.Interceptor

object RetrofitClient {
    // URL DINÁMICA SEGÚN SI ES EMULADOR O DISPOSITIVO REAL
    private fun isEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        val brand = Build.BRAND
        val device = Build.DEVICE
        val product = Build.PRODUCT
        return fingerprint.startsWith("generic") || fingerprint.contains("emulator") ||
                model.contains("Emulator", ignoreCase = true) || model.contains("Android SDK built for", ignoreCase = true) ||
                (brand.startsWith("generic", ignoreCase = true) && device.startsWith("generic", ignoreCase = true)) ||
                product.equals("google_sdk", ignoreCase = true) || product.equals("sdk", ignoreCase = true) || product.equals("sdk_gphone", ignoreCase = true) ||
                manufacturer.contains("Genymotion", ignoreCase = true) || manufacturer.contains("unknown", ignoreCase = true) ||
                device.contains("ranchu", ignoreCase = true) || device.contains("goldfish", ignoreCase = true)
    }

    private val BASE_URL: String = if (isEmulator()) {
        // IP EMULADOR ANDROID STUDIO
        "http://10.0.2.2:5000/api/"
    } else {
        // IP LOCAL RED WIFI + PUERTO FLASK (CAMBIAR SEGÚN TU IPv4)
        // EJEMPLO: http://<TU_IP_LOCAL>:5000/api/
        "http://192.168.1.12:5000/api/"
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = TokenStorage.getToken()
        val requestBuilder = original.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}