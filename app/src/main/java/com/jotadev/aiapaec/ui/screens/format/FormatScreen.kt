package com.jotadev.aiapaec.ui.screens.format

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jotadev.aiapaec.ui.components.CreateFormatDialog
import com.jotadev.aiapaec.ui.components.FormatsList
import com.jotadev.aiapaec.ui.components.FormatSearchAndFilterBar
import com.jotadev.aiapaec.ui.components.FormatOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatScreen(navController: NavController) {
    val vm: FormatViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf<String?>(null) }
    var selectedSection by remember { mutableStateOf<String?>(null) }
    var selectedFormatType by remember { mutableStateOf<String?>(null) }
    var selectedScoreFormat by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<com.jotadev.aiapaec.ui.screens.format.FormatItem?>(null) }

    // CARGAR OPCIONES AL INGRESAR A LA PANTALLA
    LaunchedEffect(Unit) {
        vm.reloadMetaOptions()
    }

    // RECARGAR OPCIONES AL ABRIR EL DIÁLOGO DE CREACIÓN
    LaunchedEffect(showCreateDialog) {
        if (showCreateDialog) vm.reloadMetaOptions()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Asignar formato")
            }
        }
    ) { paddingValues ->
        val swipeState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
        SwipeRefresh(
            state = swipeState,
            onRefresh = {
                vm.refreshFormats()
                isRefreshing = state.isLoading
            },
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
            Column(modifier = Modifier.fillMaxSize()) {
                FormatSearchAndFilterBar(
                    searchText = searchText,
                    onSearchTextChange = {
                        searchText = it
                        vm.loadFormats(
                            query = it,
                            grade = selectedGrade,
                            section = selectedSection,
                            formatType = selectedFormatType,
                            scoreFormat = selectedScoreFormat
                        )
                    },
                    selectedGrade = selectedGrade,
                    onGradeChange = {
                        selectedGrade = it
                        vm.loadFormats(
                            query = searchText,
                            grade = it,
                            section = selectedSection,
                            formatType = selectedFormatType,
                            scoreFormat = selectedScoreFormat
                        )
                    },
                    selectedSection = selectedSection,
                    onSectionChange = {
                        selectedSection = it
                        vm.loadFormats(
                            query = searchText,
                            grade = selectedGrade,
                            section = it,
                            formatType = selectedFormatType,
                            scoreFormat = selectedScoreFormat
                        )
                    },
                    selectedFormatType = selectedFormatType,
                    onFormatTypeChange = {
                        selectedFormatType = it
                        vm.loadFormats(
                            query = searchText,
                            grade = selectedGrade,
                            section = selectedSection,
                            formatType = it,
                            scoreFormat = selectedScoreFormat
                        )
                    },
                    selectedScoreFormat = selectedScoreFormat,
                    onScoreFormatChange = {
                        selectedScoreFormat = it
                        vm.loadFormats(
                            query = searchText,
                            grade = selectedGrade,
                            section = selectedSection,
                            formatType = selectedFormatType,
                            scoreFormat = it
                        )
                    },
                    isMetaLoading = state.isMetaLoading,
                    gradeOptions = state.gradesOptions,
                    sectionOptions = state.sectionsOptions,
                    formatTypeOptions = FormatOptions.formatTypes,
                    scoreFormatOptions = FormatOptions.scoreFormats
                )
                FormatsList(
                    items = state.formats,
                    onEdit = { itemToEdit = it; showEditDialog = true },
                    onDelete = { vm.deleteFormat(it.id) },
                    onClick = { /* navegar si se requiere */ },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    CreateFormatDialog(
        isOpen = showCreateDialog,
        onDismiss = { showCreateDialog = false },
        onConfirm = { grade, section, numQuestions, formatType, scoreFormat ->
            val name = "Formato $formatType $numQuestions preguntas"
            vm.createFormat(name, grade, section, numQuestions, formatType, scoreFormat)
        },
        isMetaLoading = state.isMetaLoading,
        gradeOptions = state.gradesOptions,
        sectionOptions = state.sectionsOptions,
        title = "Nuevo formato",
        confirmButtonText = "Guardar"
    )

    

    itemToEdit?.let { current ->
        CreateFormatDialog(
            isOpen = showEditDialog,
            onDismiss = { showEditDialog = false; itemToEdit = null },
            onConfirm = { grade, section, numQuestions, formatType, scoreFormat ->
                vm.updateFormat(current.id, grade, section, numQuestions, formatType, scoreFormat)
            },
            isMetaLoading = state.isMetaLoading,
            gradeOptions = state.gradesOptions,
            sectionOptions = state.sectionsOptions,
            title = "Editar formato",
            initialGrade = current.grade,
            initialSection = current.section,
            initialNumQuestions = current.numQuestions,
            initialFormatType = current.formatType,
            initialScoreFormat = null,
            confirmButtonText = "Guardar"
        )
    }
}