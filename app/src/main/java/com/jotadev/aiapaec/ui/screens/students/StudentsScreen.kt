package com.jotadev.aiapaec.ui.screens.students

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jotadev.aiapaec.ui.components.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jotadev.aiapaec.ui.screens.students.StudentsViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(navController: NavController) {
    val vm: StudentsViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    val swipeState = rememberSwipeRefreshState(isRefreshing = state.isLoading)
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.onPrimary,
        topBar = {
            ScreenTopAppBar(
                screenTitle = "Estudiantes",
                subtitle = "Listado de estudiantes registrados"
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = swipeState,
            onRefresh = { vm.refresh() },
            indicator = { s, trigger ->
                SwipeRefreshIndicator(
                    state = s,
                    refreshTriggerDistance = trigger,
                    scale = true,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    modifier = Modifier.weight(1f)
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