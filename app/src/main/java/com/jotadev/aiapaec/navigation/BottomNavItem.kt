package com.jotadev.aiapaec.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.rounded.Assignment
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
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.School
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector,
    val label: String
) {
    data object Home : BottomNavItem(
        route = NavigationRoutes.HOME,
        iconFilled = Icons.Rounded.Home,
        iconOutlined = Icons.Rounded.Home,
        label = "Inicio"
    )
    data object Exams : BottomNavItem(
        route = NavigationRoutes.EXAMS,
        iconFilled = Icons.AutoMirrored.Rounded.Assignment,
        iconOutlined = Icons.AutoMirrored.Rounded.Assignment,
        label = "Exámenes"
    )
    data object Classes : BottomNavItem(
        route = NavigationRoutes.CLASSES,
        iconFilled = Icons.Rounded.Groups,
        iconOutlined = Icons.Rounded.Groups,
        label = "Clases"
    )
    data object Students : BottomNavItem(
        route = NavigationRoutes.STUDENTS,
        iconFilled = Icons.Rounded.School,
        iconOutlined = Icons.Rounded.School,
        label = "Alumnos"
    )
    data object Results : BottomNavItem(
        route = NavigationRoutes.RESULTS,
        iconFilled = Icons.Rounded.Leaderboard,
        iconOutlined = Icons.Rounded.Leaderboard,
        label = "Resultados"
    )
    companion object {
        fun getAllItems() = listOf(Home, Exams, Classes, Students, Results)
    }
}