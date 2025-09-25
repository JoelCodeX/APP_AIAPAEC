package com.jotadev.aiapaec.ui.screens.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

    // Rutas donde no se debe mostrar la barra de navegación inferior
    val routesWithoutBottomBar = listOf(
        NavigationRoutes.GROUP_CLASSES,
        NavigationRoutes.EXAM_DETAIL,
        NavigationRoutes.CREATE_EXAM,
        NavigationRoutes.SCAN_CARD
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.tertiary,
        bottomBar = {
            if (currentRoute !in routesWithoutBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    items = bottomNavItems
                )
            }
        }
    ) { innerPadding ->
        MainNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}