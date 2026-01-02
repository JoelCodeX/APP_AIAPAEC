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
import com.jotadev.aiapaec.data.storage.TokenStorage
import com.jotadev.aiapaec.data.storage.UserStorage
import com.jotadev.aiapaec.navigation.NavigationRoutes
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.jotadev.aiapaec.data.api.RetrofitClient
import com.jotadev.aiapaec.data.session.SessionManager
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest
import android.widget.Toast

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // SplashScreen API: instala y anima la salida del splash
        val splash = installSplashScreen()
        
        // Mantener el Splash hasta determinar si el token es válido
        var isCheckingToken = true
        splash.setKeepOnScreenCondition { isCheckingToken }
        
        configureSplashExitAnimation(splash)
        super.onCreate(savedInstanceState)
        
        // Inicializar almacenamiento
        TokenStorage.init(applicationContext)
        UserStorage.init(applicationContext)

        // Estado inicial de navegación
        var startDestination by mutableStateOf(NavigationRoutes.LOGIN)

        // Validar token al inicio
        val token = TokenStorage.getToken()
        if (!token.isNullOrBlank()) {
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.verifyToken()
                    if (response.isSuccessful && response.body() != null) {
                        startDestination = NavigationRoutes.MAIN
                    } else {
                        // Token inválido o expirado
                        TokenStorage.clear()
                        startDestination = NavigationRoutes.LOGIN
                    }
                } catch (e: Exception) {
                    // Error de red u otro, asumir inválido por seguridad
                    startDestination = NavigationRoutes.LOGIN
                } finally {
                    isCheckingToken = false
                }
            }
        } else {
            startDestination = NavigationRoutes.LOGIN
            isCheckingToken = false
        }

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
                
                val navController = rememberNavController()
                
                // Observador global de sesión
                LaunchedEffect(Unit) {
                    SessionManager.logoutEvent.collectLatest {
                        TokenStorage.clear()
                        // UserStorage.clear() // Opcional: limpiar datos de usuario si se desea
                        Toast.makeText(applicationContext, "Tu sesión ha expirado", Toast.LENGTH_LONG).show()
                        navController.navigate(NavigationRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    // Solo renderizar navegación cuando terminemos de chequear el token
                    if (!isCheckingToken) {
                        AppNavigation(
                            navController = navController,
                            startDestination = startDestination
                        )
                    }
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
