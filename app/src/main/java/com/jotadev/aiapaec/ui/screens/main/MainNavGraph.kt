package com.jotadev.aiapaec.ui.screens.main

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jotadev.aiapaec.navigation.NavigationRoutes
import com.jotadev.aiapaec.ui.screens.grades.GradesScreen
import com.jotadev.aiapaec.ui.screens.exams.answers.AnswersScreen
import com.jotadev.aiapaec.ui.screens.exams.applyexam.ApplyExam
import com.jotadev.aiapaec.ui.screens.exams.general_exams.ExamsScreen
import com.jotadev.aiapaec.ui.screens.home.HomeScreen
import com.jotadev.aiapaec.ui.screens.format.FormatScreen
import com.jotadev.aiapaec.ui.screens.format.weekly.WeeklyScreen
import com.jotadev.aiapaec.ui.screens.scan.ScanResultScreen
import com.jotadev.aiapaec.ui.screens.scan.ScanUploadScreen
import com.jotadev.aiapaec.ui.screens.students.DetailsStudent
import com.jotadev.aiapaec.ui.screens.students.StudentsScreen

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
            HomeScreen(
                navController = navController, 
                onOpenSettings = onOpenSettings,
                onSessionExpired = {
                    navController.navigate(NavigationRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(NavigationRoutes.EXAMS) {
            ExamsScreen(navController = navController)
        }
        composable(NavigationRoutes.GRADES) {
            GradesScreen(navController = navController)
        }
        composable(NavigationRoutes.STUDENTS) {
            StudentsScreen(navController = navController)
        }
        composable(NavigationRoutes.FORMATS) {
            FormatScreen(navController = navController)
        }
        
        // Full Screen Variants
        composable(NavigationRoutes.EXAMS_FULL) {
            ExamsScreen(navController = navController)
        }
        composable(NavigationRoutes.GRADES_FULL) {
            GradesScreen(navController = navController)
        }
        composable(NavigationRoutes.STUDENTS_FULL) {
            StudentsScreen(navController = navController)
        }
        composable("${NavigationRoutes.FORMATS_FULL}?openDialog={openDialog}") { backStackEntry ->
            val openDialog = backStackEntry.arguments?.getString("openDialog")?.toBoolean() ?: false
            FormatScreen(navController = navController, openDialogInitially = openDialog)
        }

        composable("${NavigationRoutes.WEEKLY}?title={title}&assignmentId={assignmentId}") { backStackEntry ->
            val assignmentIdArg = backStackEntry.arguments?.getString("assignmentId")?.toIntOrNull()
            WeeklyScreen(navController = navController, assignmentId = assignmentIdArg)
        }
        // La configuración se abre como panel lateral, no navegamos a una ruta aparte

        
        composable(NavigationRoutes.GROUP_CLASSES) {
            // GroupClassesScreen(navController = navController)
        }
        
        composable(NavigationRoutes.CREATE_EXAM) {
            // CreateExamScreen(navController = navController)
        }
        composable(NavigationRoutes.SCAN_UPLOAD) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: ""
            val studentId = backStackEntry.arguments?.getString("studentId")?.toIntOrNull() ?: 0
            val numQuestions = backStackEntry.arguments?.getString("numQuestions")?.toIntOrNull() ?: 0
            ScanUploadScreen(navController = navController, examId = examId, studentId = studentId, numQuestions = numQuestions)
        }
        composable("${NavigationRoutes.SCAN_RESULT}?run_id={run_id}&overlay={overlay}&tipo={tipo}&quiz_id={quiz_id}&student_id={student_id}&read_only={read_only}") { backStackEntry ->
            val runId = backStackEntry.arguments?.getString("run_id") ?: ""
            val rawOverlay = backStackEntry.arguments?.getString("overlay") ?: ""
            val overlayUrl = Uri.decode(rawOverlay)
            val tipoStr = backStackEntry.arguments?.getString("tipo") ?: "0"
            val tipoInit = tipoStr.toIntOrNull() ?: 0
            val quizId = backStackEntry.arguments?.getString("quiz_id")?.toIntOrNull() ?: 0
            val studentId = backStackEntry.arguments?.getString("student_id")?.toIntOrNull() ?: 0
            val readOnly = backStackEntry.arguments?.getString("read_only")?.toBoolean() ?: false
            ScanResultScreen(
                navController = navController,
                runId = runId,
                overlayUrl = overlayUrl,
                tipoInit = tipoInit,
                quizId = quizId,
                studentId = studentId
            )
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

        // Ruta DETAILS_CLASS eliminada al remover funcionalidad de clases
    }
}
