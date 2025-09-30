package com.jotadev.aiapaec.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jotadev.aiapaec.ui.screens.login.LoginScreen
import com.jotadev.aiapaec.ui.screens.main.MainScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.LOGIN
    ) {
        composable(NavigationRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavigationRoutes.MAIN) {
                        popUpTo(NavigationRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        composable(NavigationRoutes.MAIN) {
            MainScreen()
        }
    }
}