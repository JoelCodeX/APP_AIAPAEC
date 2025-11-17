package com.jotadev.aiapaec.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Assessment
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
import com.jotadev.aiapaec.R

sealed class BottomNavItem(
    val route: String,
    val iconFilled: Int,
    val iconOutlined: Int,
    val label: String
) {
    data object Home : BottomNavItem(
        route = NavigationRoutes.HOME,
        iconFilled = R.drawable.home_filled,
        iconOutlined = R.drawable.home_outline,
        label = "Inicio"
    )
    data object Exams : BottomNavItem(
        route = NavigationRoutes.EXAMS,
        iconFilled = R.drawable.assignment_filled,
        iconOutlined = R.drawable.assignment_outline,
        label = "Exámenes"
    )
    data object Classes : BottomNavItem(
        route = NavigationRoutes.CLASSES,
        iconFilled = R.drawable.group_filled,
        iconOutlined = R.drawable.group_outline,
        label = "Clases"
    )
    data object Students : BottomNavItem(
        route = NavigationRoutes.STUDENTS,
        iconFilled = R.drawable.school_filled,
        iconOutlined = R.drawable.school_outline,
        label = "Alumnos"
    )
    data object Results : BottomNavItem(
        route = NavigationRoutes.RESULTS,
        iconFilled = R.drawable.leaderboard_filled,
        iconOutlined = R.drawable.leaderboard_outline,
        label = "Resultados"
    )
    companion object {
        fun getAllItems() = listOf(Home, Exams, Classes, Students, Results)
    }
}
