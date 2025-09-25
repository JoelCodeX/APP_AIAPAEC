package com.jotadev.aiapaec.ui.screens.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jotadev.aiapaec.navigation.NavigationRoutes
import com.jotadev.aiapaec.ui.screens.home.HomeScreen
import com.jotadev.aiapaec.ui.screens.exams.ExamsScreen
import com.jotadev.aiapaec.ui.screens.results.ResultsScreen
import com.jotadev.aiapaec.ui.screens.settings.SettingsScreen

@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.HOME,
        modifier = modifier
    ) {
        composable(NavigationRoutes.HOME) {
            HomeScreen(navController = navController)
        }
        
        composable(NavigationRoutes.EXAMS) {
            ExamsScreen(navController = navController)
        }
        
        composable(NavigationRoutes.RESULTS) {
            ResultsScreen(navController = navController)
        }
        
        composable(NavigationRoutes.SETTINGS) {
            SettingsScreen(navController = navController)
        }
        
        // Pantallas secundarias
        composable(NavigationRoutes.CLASSES) {
            // ClassesScreen(navController = navController)
        }
        
        composable(NavigationRoutes.GROUP_CLASSES) {
            // GroupClassesScreen(navController = navController)
        }
        
        composable(NavigationRoutes.CREATE_EXAM) {
            // CreateExamScreen(navController = navController)
        }
        
        composable(NavigationRoutes.SCAN_CARD) {
            // ScanCardScreen(navController = navController)
        }
        
        composable(NavigationRoutes.EXAM_DETAIL) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: ""
            // ExamDetailScreen(examId = examId, navController = navController)
        }
    }
}