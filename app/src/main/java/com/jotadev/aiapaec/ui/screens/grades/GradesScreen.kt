package com.jotadev.aiapaec.ui.screens.grades

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.ExperimentalMaterialApi
import com.jotadev.aiapaec.ui.components.grades.GradesList
import com.jotadev.aiapaec.ui.components.grades.GradesSearchAndFilterBar
import com.jotadev.aiapaec.ui.screens.grades.GradesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun GradesScreen(navController: NavController) {
    val vm: GradesViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("Todos los niveles") }

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
                    selectedLevel = selectedLevel,
                    onLevelChange = { selectedLevel = it },
                    selectedGrade = state.selectedGrade,
                    onGradeChange = { vm.onGradeChange(it) },
                    gradeOptions = state.gradesOptions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                val filteredGrades = state.grades.filter { g ->
                    val byGrade = state.selectedGrade == "Todos los grados" || g.nombre == state.selectedGrade
                    val byLevel = selectedLevel == "Todos los niveles" || (g.nivel ?: "") == selectedLevel
                    val q = searchQuery.trim()
                    val byQuery = q.isBlank() || g.nombre.contains(q, true) || (g.descripcion ?: "").contains(q, true)
                    byGrade && byLevel && byQuery
                }
                // LISTA DE GRADOS
                GradesList(
                    grades = filteredGrades,
                    onGradeClick = { grade ->
                        vm.onGradeChange(grade.nombre)
                    }
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
