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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import android.widget.Toast

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Inicializar almacenamiento
        TokenStorage.init(applicationContext)
        UserStorage.init(applicationContext)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            // Estado para controlar la carga inicial y el destino
            var isLoading by remember { mutableStateOf(true) }
            var startDestination by remember { mutableStateOf(NavigationRoutes.LOGIN) }

            // Mantener el Splash visible mientras isLoading sea true
            splash.setKeepOnScreenCondition { isLoading }
            configureSplashExitAnimation(splash)

            // Efecto de carga inicial
            LaunchedEffect(Unit) {
                val token = TokenStorage.getToken()
                if (!token.isNullOrBlank()) {
                    try {
                        val response = RetrofitClient.apiService.verifyToken()
                        if (response.isSuccessful && response.body() != null) {
                            startDestination = NavigationRoutes.MAIN
                        } else {
                            TokenStorage.clear()
                            startDestination = NavigationRoutes.LOGIN
                        }
                    } catch (e: Exception) {
                        // Ante cualquier error, al login
                        startDestination = NavigationRoutes.LOGIN
                    }
                } else {
                    startDestination = NavigationRoutes.LOGIN
                }
                // Al terminar la verificación, liberamos la carga
                isLoading = false
            }

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
                        // Aseguramos que corra en el hilo principal
                        withContext(Dispatchers.Main) {
                            TokenStorage.clear()
                            Toast.makeText(applicationContext, "Tu sesión ha expirado", Toast.LENGTH_LONG).show()
                            
                            // Navegación segura al Login
                            try {
                                navController.navigate(NavigationRoutes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    // Solo mostramos el contenido cuando ya no está cargando
                    if (!isLoading) {
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
