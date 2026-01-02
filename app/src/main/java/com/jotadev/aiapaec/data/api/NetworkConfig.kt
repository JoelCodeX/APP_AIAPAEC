package com.jotadev.aiapaec.data.api

import android.os.Build

object NetworkConfig {
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

    private const val LOCAL_IP = "192.168.50.5"
    private const val PORT = 5000

    // PRODUCCIÓN
    val baseRoot: String = "https://web-production-3c6a3.up.railway.app"
    // DESARROLLO
//     val baseRoot: String = if (isEmulator()) "http://10.0.2.2:$PORT" else "http://$LOCAL_IP:$PORT"
    val apiBase: String = "$baseRoot/api/"
}

