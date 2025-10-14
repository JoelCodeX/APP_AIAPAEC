package com.jotadev.aiapaec.ui.screens.classes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jotadev.aiapaec.ui.components.ScreenTopAppBar
import com.jotadev.aiapaec.ui.components.StudentsList
import com.jotadev.aiapaec.navigation.NavigationRoutes

@Composable
fun DetailsClasses(
    navController: NavController,
    classId: String
) {
    val vm: ClassStudentsViewModel = viewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(classId) {
        classId.toIntOrNull()?.let { vm.load(it) }
    }

    Scaffold(
        topBar = {
            ScreenTopAppBar(screenTitle = "Estudiantes de la clase")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            StudentsList(
                students = uiState.students,
                onStudentClick = { student ->
                    navController.navigate(NavigationRoutes.detailsStudent(student.id))
                }
            )
        }
    }
}