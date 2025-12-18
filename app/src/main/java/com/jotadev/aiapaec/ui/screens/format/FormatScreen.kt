package com.jotadev.aiapaec.ui.screens.format

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jotadev.aiapaec.navigation.NavigationRoutes
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.jotadev.aiapaec.ui.components.format.CreateFormatDialog
import com.jotadev.aiapaec.ui.components.format.FormatOptions
import com.jotadev.aiapaec.ui.components.format.FormatSearchAndFilterBar
import com.jotadev.aiapaec.ui.components.format.FormatsList

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun FormatScreen(navController: NavController, openDialogInitially: Boolean = false) {
    val vm: FormatViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(openDialogInitially) }
    val ctx = LocalContext.current

    val handle = navController.currentBackStackEntry?.savedStateHandle
    val formatsCreateFlow = handle?.getStateFlow("formats_create_request", false) ?: MutableStateFlow(false)
    val formatsCreateRequest by formatsCreateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(formatsCreateRequest) {
        if (formatsCreateRequest) {
            showCreateDialog = true
            handle?.set("formats_create_request", false)
        }
    }
    var searchText by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf<String?>(null) }
    var selectedSection by remember { mutableStateOf<String?>(null) }
    var selectedFormatType by remember { mutableStateOf<String?>(null) }
    var selectedScoreFormat by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<com.jotadev.aiapaec.ui.screens.format.FormatItem?>(null) }
    var itemToDelete by remember { mutableStateOf<com.jotadev.aiapaec.ui.screens.format.FormatItem?>(null) }

    var showEditWarningDialog by remember { mutableStateOf(false) }
    var pendingEditAction by remember { mutableStateOf<(() -> Unit)?>(null) }

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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val refreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = {
                vm.refreshFormats()
                isRefreshing = state.isLoading
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(refreshState)
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
                        vm.loadSectionsForGrade(it)
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
                    sectionOptions = selectedGrade?.let { g -> state.sectionsByGrade[g] ?: emptyList() } ?: emptyList(),
                    formatTypeOptions = FormatOptions.formatTypes,
                    scoreFormatOptions = FormatOptions.scoreFormats
                )
                FormatsList(
                    items = state.formats,
                    onEdit = { itemToEdit = it; showEditDialog = true },
                    onDelete = {
                        itemToDelete = it
                        vm.checkFormatUsage(it.id)
                    },
                    onClick = { item ->
                        val handle = navController.currentBackStackEntry?.savedStateHandle
                        handle?.set("weekly_assignment_id", item.id.toInt())
                        handle?.set("weekly_grade_name", item.grade)
                        handle?.set("weekly_grade_id", item.gradeId)
                        handle?.set("weekly_section_name", item.section)
                        handle?.set("weekly_section_id", item.sectionId)
                        handle?.set("weekly_num_questions", item.numQuestions)
                        navController.navigate(NavigationRoutes.weekly(item.formatType, item.id.toInt()))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = refreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }

    CreateFormatDialog(
        isOpen = showCreateDialog,
        onDismiss = { 
            showCreateDialog = false
            selectedGrade = null
            selectedSection = null
            selectedFormatType = null
            selectedScoreFormat = null
            searchText = ""
            vm.loadFormats(query = searchText)
        },
        onConfirm = { grade, section, numQuestions, formatType, scoreFormat ->
            val name = "Formato $formatType $numQuestions preguntas"
            vm.createFormat(name, grade, section, numQuestions, formatType, scoreFormat)
            Toast.makeText(ctx, "Formato creado", Toast.LENGTH_SHORT).show()
        },
        onGradeChange = { vm.loadSectionsForGrade(it) },
        isMetaLoading = state.isMetaLoading,
        gradeOptions = state.gradesOptions,
        sectionOptions = state.sectionsOptions,
        sectionsByGrade = state.sectionsByGrade,
        title = "Asignar formato",
        confirmButtonText = "Guardar"
    )

    

    itemToEdit?.let { current ->
        CreateFormatDialog(
            isOpen = showEditDialog,
            onDismiss = { showEditDialog = false; itemToEdit = null },
            onConfirm = { grade, section, numQuestions, formatType, scoreFormat ->
            val action = {
                vm.updateFormat(current.id, grade, section, numQuestions, formatType, scoreFormat)
                Toast.makeText(ctx, "Formato actualizado", Toast.LENGTH_SHORT).show()
                showEditDialog = false
                itemToEdit = null
            }

            vm.checkFormatUsage(current.id)
            pendingEditAction = action
            showEditWarningDialog = true
        },
        onGradeChange = { vm.loadSectionsForGrade(it) },
            isMetaLoading = state.isMetaLoading,
            gradeOptions = state.gradesOptions,
            sectionOptions = state.sectionsOptions,
            sectionsByGrade = state.sectionsByGrade,
            title = "Editar formato",
            initialGrade = current.grade,
            initialSection = current.section,
            initialNumQuestions = current.numQuestions,
            initialFormatType = current.formatType,
            initialScoreFormat = null,
            confirmButtonText = "Guardar"
        )
    }

    if (itemToDelete != null) {
        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(
                    "Confirmar eliminación",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                     if (state.isUsageLoading || state.formatUsageCount == null) {
                         Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                             CircularProgressIndicator()
                         }
                     } else {
                         val count = state.formatUsageCount ?: 0
                         if (count > 0) {
                             Column {
                                 Text("¡ADVERTENCIA CRÍTICA!", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                 Spacer(modifier = Modifier.height(8.dp))
                                 Text("Este formato tiene $count semanales asociados. Si lo eliminas, SE PERDERÁN TODOS LOS SEMANALES y sus notas.")
                                 Spacer(modifier = Modifier.height(8.dp))
                                 Text("¿Estás absolutamente seguro de continuar?")
                             }
                         } else {
                             Text("¿Eliminar formato ${itemToDelete?.name}?")
                         }
                     }
                },
                confirmButton = {
                    if (!state.isUsageLoading && state.formatUsageCount != null) {
                        TextButton(onClick = {
                             itemToDelete?.let { 
                                 vm.deleteFormat(it.id) 
                                 Toast.makeText(ctx, "Formato eliminado", Toast.LENGTH_SHORT).show()
                             }
                             itemToDelete = null
                        }) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showEditWarningDialog) {
        LaunchedEffect(state.isUsageLoading, state.formatUsageCount) {
             if (!state.isUsageLoading && state.formatUsageCount == 0) {
                 pendingEditAction?.invoke()
                 showEditWarningDialog = false
                 pendingEditAction = null
             }
        }
        
        if (state.isUsageLoading || state.formatUsageCount == null || state.formatUsageCount!! > 0) {
             AlertDialog(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                onDismissRequest = { showEditWarningDialog = false },
                title = {
                    Text(
                        if (state.isUsageLoading || state.formatUsageCount == null) "Verificando..." else "Advertencia de modificación",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    if (state.isUsageLoading || state.formatUsageCount == null) {
                         Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                             CircularProgressIndicator()
                         }
                    } else {
                         Column {
                             Text("¡ATENCIÓN!", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                             Spacer(modifier = Modifier.height(8.dp))
                             Text("Este formato tiene ${state.formatUsageCount} semanales asociados.")
                             Text("Modificar la configuración (Grado, Sección, etc.) podría causar inconsistencias en los semanales existentes.")
                             Spacer(modifier = Modifier.height(8.dp))
                             Text("¿Deseas guardar los cambios de todos modos?")
                         }
                    }
                },
                confirmButton = {
                    if (!state.isUsageLoading && state.formatUsageCount != null) {
                        TextButton(onClick = {
                            pendingEditAction?.invoke()
                            showEditWarningDialog = false
                            pendingEditAction = null
                        }) {
                            Text("Guardar y Arriesgarse", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showEditWarningDialog = false 
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
