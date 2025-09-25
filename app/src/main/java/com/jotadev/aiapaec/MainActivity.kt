package com.jotadev.aiapaec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jotadev.aiapaec.navigation.AppNavigation
import com.jotadev.aiapaec.ui.screens.main.MainScreen
import com.jotadev.aiapaec.ui.theme.AIAPAECTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIAPAECTheme(dynamicColor = false) {
                //AppNavigation()
                MainScreen()
            }
        }
    }
}