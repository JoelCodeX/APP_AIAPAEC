package com.jotadev.aiapaec.ui.screens.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import android.net.Uri
import com.jotadev.aiapaec.navigation.NavigationRoutes
import com.jotadev.aiapaec.ui.screens.classes.ClassesScreen
import com.jotadev.aiapaec.ui.screens.classes.DetailsClasses
import com.jotadev.aiapaec.ui.screens.exams.ExamsScreen
import com.jotadev.aiapaec.ui.screens.exams.ApplyExam
import com.jotadev.aiapaec.ui.screens.exams.AnswersScreen
import com.jotadev.aiapaec.ui.screens.home.HomeScreen
import com.jotadev.aiapaec.ui.screens.results.ResultsScreen
import com.jotadev.aiapaec.ui.screens.settings.SettingsScreen
import com.jotadev.aiapaec.ui.screens.students.StudentsScreen
import com.jotadev.aiapaec.ui.screens.students.DetailsStudent
import com.jotadev.aiapaec.ui.screens.scan.ScanScreen
import com.jotadev.aiapaec.ui.screens.scan.ScanUploadScreen
import com.jotadev.aiapaec.ui.screens.scan.CropPreviewScreen
import com.jotadev.aiapaec.ui.screens.scan.ScanResultScreen

@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.HOME,
        modifier = modifier
    ) {
        composable(NavigationRoutes.HOME) {
            HomeScreen(navController = navController, onOpenSettings = onOpenSettings)
        }
        composable(NavigationRoutes.EXAMS) {
            ExamsScreen(navController = navController)
        }
        composable(NavigationRoutes.CLASSES) {
            ClassesScreen(navController = navController)
        }
        composable(NavigationRoutes.STUDENTS) {
            StudentsScreen(navController = navController)
        }
        composable(NavigationRoutes.RESULTS) {
            ResultsScreen(navController = navController)
        }
        // La configuración se abre como panel lateral, no navegamos a una ruta aparte

        
        composable(NavigationRoutes.GROUP_CLASSES) {
            // GroupClassesScreen(navController = navController)
        }
        
        composable(NavigationRoutes.CREATE_EXAM) {
            // CreateExamScreen(navController = navController)
        }
        
        composable(NavigationRoutes.SCAN_CARD) {
            ScanScreen(navController = navController)
        }
        composable(NavigationRoutes.SCAN_UPLOAD) {
            ScanUploadScreen(navController = navController)
        }
        composable("${NavigationRoutes.CROP_PREVIEW}?path={path}") { backStackEntry ->
            val raw = backStackEntry.arguments?.getString("path") ?: ""
            val path = Uri.decode(raw)
            CropPreviewScreen(navController = navController, filePath = path)
        }
        composable("${NavigationRoutes.SCAN_RESULT}?run_id={run_id}&overlay={overlay}&tipo={tipo}") { backStackEntry ->
            val runId = backStackEntry.arguments?.getString("run_id") ?: ""
            val rawOverlay = backStackEntry.arguments?.getString("overlay") ?: ""
            val overlayUrl = Uri.decode(rawOverlay)
            val tipoStr = backStackEntry.arguments?.getString("tipo") ?: "0"
            val tipoInit = tipoStr.toIntOrNull() ?: 0
            ScanResultScreen(navController = navController, runId = runId, overlayUrl = overlayUrl, tipoInit = tipoInit)
        }
        
        composable(NavigationRoutes.EXAM_DETAIL) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: ""
            // ExamDetailScreen(examId = examId, navController = navController)
        }

        composable(NavigationRoutes.APPLY_EXAM) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: ""
            ApplyExam(navController = navController, examId = examId)
        }

        composable(NavigationRoutes.QUIZ_ANSWERS) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: ""
            AnswersScreen(navController = navController, examId = examId)
        }

        composable(NavigationRoutes.DETAILS_STUDENT) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            val idInt = studentId.toIntOrNull() ?: 0
            DetailsStudent(navController = navController, studentId = idInt)
        }

        composable(NavigationRoutes.DETAILS_CLASS) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId") ?: ""
            DetailsClasses(navController = navController, classId = classId)
        }
    }
}