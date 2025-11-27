package com.jotadev.aiapaec.ui.screens.students

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.jotadev.aiapaec.navigation.NavigationRoutes
import com.jotadev.aiapaec.ui.components.students.StudentsList
import com.jotadev.aiapaec.ui.components.students.StudentsSearchAndFilterBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun StudentsScreen(navController: NavController) {
    val vm: StudentsViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    val pullState = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh = { vm.refresh() }
    )
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        // topBar unificado en MainScreen
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                // BARRA DE BUSQUEDA Y FILTROS
                StudentsSearchAndFilterBar(
                    searchQuery = state.query,
                    onSearchQueryChange = {
                        vm.onQueryChange(it)
                        vm.fetchStudents(page = 1)
                    },
                    selectedClass = "Todas las clases",
                    onClassChange = { /* TODO: conectar filtro por clase cuando API lo soporte */ }
                )
                
                // LISTA DE ESTUDIANTES
                StudentsList(
                    students = state.students,
                    modifier = Modifier.weight(1f),
                    onStudentClick = { student ->
                        navController.navigate(NavigationRoutes.detailsStudent(student.id))
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
            PullRefreshIndicator(
                refreshing = state.isLoading,
                state = pullState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}
