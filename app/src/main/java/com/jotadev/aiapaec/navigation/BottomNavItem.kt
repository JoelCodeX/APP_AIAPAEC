package com.jotadev.aiapaec.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    data object Home : BottomNavItem(
        route = NavigationRoutes.HOME,
        icon = Icons.Default.Home,
        label = "Inicio"
    )

    data object Exams : BottomNavItem(
        route = NavigationRoutes.EXAMS,
        icon = Icons.AutoMirrored.Filled.Assignment,
        label = "Exámenes"
    )

    data object Results : BottomNavItem(
        route = NavigationRoutes.RESULTS,
        icon = Icons.Default.Leaderboard,
        label = "Resultados"
    )
    
    data object Settings : BottomNavItem(
        route = NavigationRoutes.SETTINGS,
        icon = Icons.Default.Settings,
        label = "Configuración"
    )
    
    companion object {
        fun getAllItems() = listOf(Home, Exams, Results, Settings)
    }
}