package com.jotadev.aiapaec.ui.screens.grades

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jotadev.aiapaec.data.api.SectionDto
import com.jotadev.aiapaec.domain.models.Grade
import com.jotadev.aiapaec.navigation.NavigationRoutes
import com.jotadev.aiapaec.ui.components.grades.GradesList
import com.jotadev.aiapaec.ui.components.grades.GradesSearchAndFilterBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun GradesScreen(navController: NavController) {
    val vm: GradesViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    // selectedLevel moved to ViewModel

    // Función para manejar clicks en secciones
    val onSectionClick = { grade: Grade, section: SectionDto ->
        navController.navigate(
            NavigationRoutes.sectionStudents(
                gradeId = grade.id,
                sectionId = section.id,
                gradeName = grade.nombre,
                sectionName = section.nombre,
                studentCount = section.studentCount
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        // topBar unificado en MainScreen
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                GradesSearchAndFilterBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedLevel = state.selectedLevel,
                    onLevelChange = { vm.onLevelChange(it) },
                    selectedGrade = state.selectedGrade,
                    onGradeChange = { vm.onGradeChange(it) },
                    gradeOptions = state.gradesOptions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                val filteredGrades = state.grades.filter { g ->
                    val byGrade = state.selectedGrade == "Todos los grados" || g.nombre == state.selectedGrade
                    val byLevel = state.selectedLevel == "Todos los niveles" || (g.nivel ?: "") == state.selectedLevel
                    val q = searchQuery.trim()
                    val byQuery = q.isBlank() || g.nombre.contains(q, true) || (g.descripcion ?: "").contains(q, true)
                    byGrade && byLevel && byQuery
                }

                // LISTA DE GRADOS - Actualizado con nuevos parámetros
                GradesList(
                    grades = filteredGrades,
                    onSectionClick = onSectionClick
                )

                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
