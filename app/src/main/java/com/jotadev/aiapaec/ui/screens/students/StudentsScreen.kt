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
import androidx.compose.runtime.remember
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
import androidx.compose.runtime.setValue
import com.jotadev.aiapaec.navigation.NavigationRoutes
import com.jotadev.aiapaec.ui.components.students.StudentsList
import com.jotadev.aiapaec.ui.components.students.StudentsSearchAndFilterBar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.platform.LocalConfiguration
import com.jotadev.aiapaec.ui.components.ListSkeleton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import com.jotadev.aiapaec.ui.components.students.StudentCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun StudentsScreen(navController: NavController) {
    val vm: StudentsViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360
    
    val pullState = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh = { vm.refresh() }
    )

    // Detectar fin de lista para paginación
    val listState = rememberLazyListState()
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount == 0) {
                false
            } else {
                val lastVisibleItem = visibleItemsInfo.last()
                val viewportHeight = layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset
                (lastVisibleItem.index + 1 == layoutInfo.totalItemsCount) &&
                        (lastVisibleItem.offset + lastVisibleItem.size <= viewportHeight)
            }
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            vm.loadNextPage()
        }
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
                .pullRefresh(pullState)
        ) {
            // Se elimina el filtrado local redundante; confiamos en state.students que viene del backend (filtrado o completo)
            val sectionOptions = state.selectedGrade?.let { g -> state.sectionsByGrade[g] ?: emptyList() } ?: emptyList()
            // NOTA: Si el backend ya filtra, usamos state.students directo. 
            // Si el filtrado local era "extra" sobre lo que traía el backend, al quitarlo confiamos 100% en la API.
            // Para mantener consistencia con "no modificar nada", si el backend no filtraba bien, esto podría cambiar comportamiento.
            // Pero el ViewModel envía los filtros al backend. Así que usar state.students es lo correcto para paginación real.
            
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
                    selectedGrade = state.selectedGrade,
                    onGradeChange = { vm.onGradeSelected(it) },
                    gradeOptions = state.gradesOptions,
                    selectedSection = state.selectedSection,
                    onSectionChange = { vm.onSectionSelected(it) },
                    sectionOptions = sectionOptions,
                    isMetaLoading = state.isMetaLoading
                )
                
                // LISTA DE ESTUDIANTES
                if (state.isLoading && state.students.isEmpty()) {
                    ListSkeleton(isSmallScreen = isSmallScreen)
                } else {
                    // Reemplazamos StudentsList por LazyColumn directa para tener control del scroll state
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = state.students, key = { it.id }) { student ->
                            StudentCard(
                                student = student,
                                onClick = { 
                                    navController.navigate(NavigationRoutes.detailsStudent(student.id))
                                }
                            )
                        }
                        
                        if (state.isAppending) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
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
