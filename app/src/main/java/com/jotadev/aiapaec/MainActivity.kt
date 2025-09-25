package com.jotadev.aiapaec

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.jotadev.aiapaec.navigation.AppNavigation
import com.jotadev.aiapaec.ui.screens.main.MainScreen
import com.jotadev.aiapaec.ui.theme.AIAPAECTheme
import com.jotadev.aiapaec.ui.theme.Crimson60

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // ✅ Establecer color de barra de estado sin usar statusBarColor directamente
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = Crimson60.toArgb() // si también quieres nav bar
            window.statusBarColor = Crimson60.toArgb() // aún necesario para fondo
        }
        // ✅ Controlar íconos (blancos o negros)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false // false = íconos blancos

        setContent {
            AIAPAECTheme(dynamicColor = false) {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = false // 👈 iconos blancos
                val statusBarColor = MaterialTheme.colorScheme.primary // 👈 tu color global

                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = statusBarColor,
                        darkIcons = useDarkIcons
                    )

                }
                AppNavigation()
//                MainScreen()
            }
        }
    }
}