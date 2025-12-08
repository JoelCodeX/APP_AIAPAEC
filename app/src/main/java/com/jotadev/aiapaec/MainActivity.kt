package com.jotadev.aiapaec

import android.os.Bundle
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.view.View
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreen
import com.jotadev.aiapaec.navigation.AppNavigation
import com.jotadev.aiapaec.ui.theme.AIAPAECTheme

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // SplashScreen API: instala y anima la salida del splash
        val splash = installSplashScreen()
        configureSplashExitAnimation(splash)
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

    // Anima la salida del splash: escala el ícono y desvanece el fondo
    private fun configureSplashExitAnimation(splashScreen: SplashScreen) {
        splashScreen.setOnExitAnimationListener { provider ->
            val iconView = provider.iconView
            val rootView = provider.view

            val fadeOut = ObjectAnimator.ofFloat(rootView, View.ALPHA, 1f, 0f).apply {
                duration = 300
            }

            val scaleX = ObjectAnimator.ofFloat(iconView, View.SCALE_X, 1f, 0.9f, 1.15f)
                .apply { duration = 350 }
            val scaleY = ObjectAnimator.ofFloat(iconView, View.SCALE_Y, 1f, 0.9f, 1.15f)
                .apply { duration = 350 }

            AnimatorSet().apply {
                playTogether(fadeOut, scaleX, scaleY)
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        provider.remove()
                    }
                })
                start()
            }
        }
    }
}
