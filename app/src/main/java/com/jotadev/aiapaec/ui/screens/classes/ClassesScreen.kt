package com.jotadev.aiapaec.ui.screens.classes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jotadev.aiapaec.ui.components.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.jotadev.aiapaec.navigation.NavigationRoutes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ClassesScreen(navController: NavController) {
    val vm: ClassesViewModel = viewModel()
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
            Column(modifier = Modifier.fillMaxSize()) {
                // BARRA DE BUSQUEDA Y FILTROS
                ClassesSearchAndFilterBar(
                    searchQuery = state.query,
                    onSearchQueryChange = {
                        vm.onQueryChange(it)
                        vm.fetchClasses(page = 1)
                    },
                    selectedLevel = state.selectedLevel,
                    onLevelChange = {
                        vm.onLevelChange(it)
                        vm.fetchClasses(page = 1)
                    }
                )
                
                // LISTA DE CLASES
                ClassesList(
                    classes = state.classes,
                    modifier = Modifier.weight(1f),
                    onClassClick = { clazz ->
                        navController.navigate(NavigationRoutes.detailsClass(clazz.id))
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
