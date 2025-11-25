package com.jotadev.aiapaec.ui.screens.format.weekly

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberExpandedState
import androidx.compose.material3.ExperimentalLayoutApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun WeeklyScreen(navController: NavController) {
    val vm: WeeklyViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    val prevHandle = navController.previousBackStackEntry?.savedStateHandle
    val assignmentId = prevHandle?.get<Int>("weekly_assignment_id")
    val gradeName = prevHandle?.get<String>("weekly_grade_name") ?: ""
    val sectionName = prevHandle?.get<String>("weekly_section_name") ?: ""
    val numQuestions = prevHandle?.get<Int>("weekly_num_questions") ?: 0

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedBimesterLabel by remember { mutableStateOf<String?>(null) }
    var selectedUnidadLabel by remember { mutableStateOf<String?>(null) }
    var selectedSemana by remember { mutableStateOf<Int?>(null) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var detalle by remember { mutableStateOf("") }

    val bimesters = listOf("I BIMESTRE", "II BIMESTRE", "III BIMESTRE", "IV BIMESTRE")
    val unidades = listOf("I UNIDAD", "II UNIDAD", "III UNIDAD", "IV UNIDAD")
    val semanas = (1..10).toList()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text(text = "Semanal") }, actions = {
                TextButton(onClick = { showCreateDialog = true }) { Text("Nuevo") }
            })
        }
    ) { paddingValues ->
        val refreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = { isRefreshing = false }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(refreshState)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                WeeklySummaryCard(
                    id = assignmentId?.toString() ?: "-",
                    bimester = selectedBimesterLabel ?: "-",
                    unidad = selectedUnidadLabel ?: "-",
                    semana = selectedSemana?.toString() ?: "-",
                    sede = "-",
                    grado = gradeName,
                    seccion = sectionName,
                    fecha = selectedDate ?: "-",
                    numPreguntas = numQuestions
                )

                Spacer(modifier = Modifier.size(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showCreateDialog = true }) { Text("Crear Quiz") }
                    Button(onClick = { /* SUBIR DATA */ }) { Text("Subir data") }
                    Button(onClick = { /* VER REPORTE */ }) { Text("Ver Reporte") }
                }
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

    if (showCreateDialog) {
        CreateWeeklyQuizDialog(
            bimesters = bimesters,
            unidades = unidades,
            semanas = semanas,
            selectedBimester = selectedBimesterLabel,
            selectedUnidad = selectedUnidadLabel,
            selectedSemana = selectedSemana,
            selectedDate = selectedDate,
            detalle = detalle,
            onDismiss = { showCreateDialog = false },
            onConfirm = { bimLabel, uniLabel, semanaSel, fechaSel, det ->
                val bimId = bimLabel?.let { bimesters.indexOf(it) + 1 }
                val uniId = uniLabel?.let { unidades.indexOf(it) + 1 }
                val title = listOfNotNull(uniLabel, semanaSel?.let { "Semana $it" }).joinToString(" - ")
                vm.createQuiz(
                    title = title.ifBlank { "QUIZ SEMANAL" },
                    bimesterId = bimId,
                    unidadId = uniId,
                    fecha = fechaSel ?: java.time.LocalDate.now().toString(),
                    numQuestions = numQuestions,
                    detalle = det,
                    asignacionId = assignmentId
                )
                selectedBimesterLabel = bimLabel
                selectedUnidadLabel = uniLabel
                selectedSemana = semanaSel
                selectedDate = fechaSel
                showCreateDialog = false
            },
            onChange = { b, u, s, f, d ->
                selectedBimesterLabel = b
                selectedUnidadLabel = u
                selectedSemana = s
                selectedDate = f
                detalle = d
            }
        )
    }
}

@Composable
private fun WeeklySummaryCard(
    id: String,
    bimester: String,
    unidad: String,
    semana: String,
    sede: String,
    grado: String,
    seccion: String,
    fecha: String,
    numPreguntas: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(text = "Id: $id", style = MaterialTheme.typography.titleMedium)
        Text(text = "Bimestre: $bimester")
        Text(text = "Unidad: $unidad")
        Text(text = "Semana: $semana")
        Text(text = "Sede: $sede")
        Text(text = "Grado: $grado")
        Text(text = "Sección: $seccion")
        Text(text = "Fecha: $fecha")
        Text(text = "Número Preguntas: $numPreguntas")
    }
}

@Composable
private fun CreateWeeklyQuizDialog(
    bimesters: List<String>,
    unidades: List<String>,
    semanas: List<Int>,
    selectedBimester: String?,
    selectedUnidad: String?,
    selectedSemana: Int?,
    selectedDate: String?,
    detalle: String,
    onDismiss: () -> Unit,
    onConfirm: (String?, String?, Int?, String?, String?) -> Unit,
    onChange: (String?, String?, Int?, String?, String) -> Unit
) {
    val ctx = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedBimester, selectedUnidad, selectedSemana, selectedDate, detalle) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Crear Quiz Semanal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectorField(label = "Bimestre", options = bimesters, selected = selectedBimester) { onChange(it, selectedUnidad, selectedSemana, selectedDate, detalle) }
                SelectorField(label = "Unidad", options = unidades, selected = selectedUnidad) { onChange(selectedBimester, it, selectedSemana, selectedDate, detalle) }
                SelectorField(label = "Semana", options = semanas.map { it.toString() }, selected = selectedSemana?.toString()) { sel -> onChange(selectedBimester, selectedUnidad, sel?.toIntOrNull(), selectedDate, detalle) }
                DateSelectorField(label = "Fecha", value = selectedDate) { onChange(selectedBimester, selectedUnidad, selectedSemana, it, detalle) }
                OutlinedTextField(value = detalle, onValueChange = { onChange(selectedBimester, selectedUnidad, selectedSemana, selectedDate, it) }, label = { Text("Detalle") })
            }
        }
    )
}

@Composable
private fun SelectorField(label: String, options: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(text = label)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selected ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { opt -> DropdownMenuItem(text = { Text(opt) }, onClick = { onSelect(opt); expanded = false }) }
            }
        }
    }
}

@Composable
private fun DateSelectorField(label: String, value: String?, onChange: (String) -> Unit) {
    val ctx = LocalContext.current
    Column {
        Text(text = label)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = value ?: "", onValueChange = {}, readOnly = true, label = { Text(label) }, modifier = Modifier.weight(1f))
            Button(onClick = {
                val cal = Calendar.getInstance()
                DatePickerDialog(ctx, { _, y, m, d ->
                    val month = (m + 1).toString().padStart(2, '0')
                    val day = d.toString().padStart(2, '0')
                    onChange("$y-$month-$day")
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            }) { Text("Seleccionar") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun WeeklyScreen(navController: NavController) {
    val vm: FormatViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
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
    var itemToEdit by remember { mutableStateOf<FormatItem?>(null) }

    LaunchedEffect(Unit) { vm.reloadMetaOptions() }
    LaunchedEffect(showCreateDialog) { if (showCreateDialog) vm.reloadMetaOptions() }

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
                    onDelete = { vm.deleteFormat(it.id); Toast.makeText(ctx, "Formato eliminado", Toast.LENGTH_SHORT).show() },
                    onClick = { /* aquí podríamos abrir detalle semanal si aplica */ },
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
        title = "Nuevo formato",
        confirmButtonText = "Guardar"
    )

    itemToEdit?.let { current ->
        CreateFormatDialog(
            isOpen = showEditDialog,
            onDismiss = { showEditDialog = false; itemToEdit = null },
            onConfirm = { grade, section, numQuestions, formatType, scoreFormat ->
                vm.updateFormat(current.id, grade, section, numQuestions, formatType, scoreFormat)
                Toast.makeText(ctx, "Formato actualizado", Toast.LENGTH_SHORT).show()
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
}
