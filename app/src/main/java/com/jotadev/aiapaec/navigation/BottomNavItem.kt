package com.jotadev.aiapaec.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector,
    val label: String
) {
    data object Home : BottomNavItem(
        route = NavigationRoutes.HOME,
        iconFilled = Icons.Default.Home,
        iconOutlined = Icons.Outlined.Home,
        label = "Inicio"
    )
    data object Exams : BottomNavItem(
        route = NavigationRoutes.EXAMS,
        iconFilled = Icons.AutoMirrored.Filled.Assignment,
        iconOutlined = Icons.AutoMirrored.Outlined.Assignment,
        label = "Exámenes"
    )
    data object Classes : BottomNavItem(
        route = NavigationRoutes.CLASSES,
        iconFilled = Icons.Filled.Groups,
        iconOutlined = Icons.Outlined.Groups,
        label = "Clases"
    )
    data object Students : BottomNavItem(
        route = NavigationRoutes.STUDENTS,
        iconFilled = Icons.Filled.School,
        iconOutlined = Icons.Outlined.School,
        label = "Alumnos"
    )
    data object Results : BottomNavItem(
        route = NavigationRoutes.RESULTS,
        iconFilled = Icons.Default.Leaderboard,
        iconOutlined = Icons.Outlined.Leaderboard,
        label = "Resultados"
    )
    companion object {
        fun getAllItems() = listOf(Home, Exams, Classes, Students, Results)
    }
}