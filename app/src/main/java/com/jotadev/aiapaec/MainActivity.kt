package com.jotadev.aiapaec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jotadev.aiapaec.navigation.AppNavigation
import com.jotadev.aiapaec.ui.theme.AIAPAECTheme

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AIAPAECTheme(dynamicColor = false) {
                val primaryColor = MaterialTheme.colorScheme.primary
                LaunchedEffect(primaryColor) {
                    window.statusBarColor = primaryColor.toArgb()
                    window.navigationBarColor = primaryColor.toArgb()
                    WindowInsetsControllerCompat(window, window.decorView).apply {
                        isAppearanceLightStatusBars = false
                        isAppearanceLightNavigationBars = false
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    AppNavigation()
//                    ApplyExam( navController = androidx.navigation.compose.rememberNavController(), examId = "exam123" )
                }
            }
        }
    }
}