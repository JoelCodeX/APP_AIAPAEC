package com.jotadev.aiapaec.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import com.jotadev.aiapaec.navigation.BottomNavItem
import com.jotadev.aiapaec.navigation.BottomNavigationBar
import com.jotadev.aiapaec.navigation.NavigationRoutes

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController()
) {
    val bottomNavItems = BottomNavItem.getAllItems()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val routesWithoutBottomBar = listOf(
        NavigationRoutes.GROUP_CLASSES,
        NavigationRoutes.EXAM_DETAIL,
        NavigationRoutes.CREATE_EXAM,
        NavigationRoutes.SCAN_CARD,
        NavigationRoutes.APPLY_EXAM,
        NavigationRoutes.DETAILS_STUDENT,
        NavigationRoutes.DETAILS_CLASS
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // CONTENIDO PRINCIPAL QUE OCUPA TODA LA PANTALLA
        MainNavGraph(navController = navController)

        // BARRA DE NAVEGACIÓN FLOTANTE
        if (currentRoute !in routesWithoutBottomBar) {
            BottomNavigationBar(
                navController = navController,
                items = bottomNavItems,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}