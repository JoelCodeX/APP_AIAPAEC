package com.jotadev.aiapaec.navigation

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
        label = "Admisión"
    )
    data object Grades : BottomNavItem(
        route = NavigationRoutes.GRADES,
        iconFilled = R.drawable.group_filled,
        iconOutlined = R.drawable.group_outline,
        label = "Grados"
    )
    data object Students : BottomNavItem(
        route = NavigationRoutes.STUDENTS,
        iconFilled = R.drawable.school_filled,
        iconOutlined = R.drawable.school_outline,
        label = "Alumnos"
    )
    data object Formats : BottomNavItem(
        route = NavigationRoutes.FORMATS,
        iconFilled = R.drawable.format_filled,
        iconOutlined = R.drawable.format_outline,
        label = "Semanales"
    )
    companion object {
        fun getAllItems() = listOf(Home, Formats, Grades, Students, Exams)
    }
}
