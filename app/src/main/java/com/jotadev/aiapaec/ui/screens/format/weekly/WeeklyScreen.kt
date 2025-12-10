package com.jotadev.aiapaec.ui.screens.format.weekly

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jotadev.aiapaec.navigation.NavigationRoutes
import com.jotadev.aiapaec.ui.components.FilterDropdown
import com.jotadev.aiapaec.ui.components.format.InfoChip
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun WeeklyScreen(navController: NavController, assignmentId: Int?) {
    val vm: WeeklyViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    val handle = navController.currentBackStackEntry?.savedStateHandle
    val weeklyCreateFlow = handle?.getStateFlow("weekly_create_request", false) ?: MutableStateFlow(false)
    val weeklyCreateRequest by weeklyCreateFlow.collectAsStateWithLifecycle()

    val prevHandle = navController.previousBackStackEntry?.savedStateHandle
    val gradeName = prevHandle?.get<String>("weekly_grade_name") ?: ""
    val sectionName = prevHandle?.get<String>("weekly_section_name") ?: ""
    val numQuestions = prevHandle?.get<Int>("weekly_num_questions") ?: 0
    val gradeId = prevHandle?.get<Int>("weekly_grade_id")
    val sectionId = prevHandle?.get<Int>("weekly_section_id")

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedBimesterLabel by remember { mutableStateOf<String?>(null) }
    var selectedUnidadLabel by remember { mutableStateOf<String?>(null) }
    var selectedSemana by remember { mutableStateOf<Int?>(null) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var detalle by remember { mutableStateOf("") }

    var searchText by remember { mutableStateOf("") }

    // ESTADOS PARA EDICIÓN
    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<com.jotadev.aiapaec.domain.models.Quiz?>(null) }

    // Dynamic bimesters via ViewModel
    val bimestersVm: com.jotadev.aiapaec.presentation.BimestersViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val bimestersState by bimestersVm.uiState.collectAsStateWithLifecycle()
    val bimesterOptions = listOf("Todos") + bimestersState.bimesters.map { it.name }
    val bimestersForDialog = bimestersState.bimesters.map { it.name }
    val unidades = state.unitOptions
    val semanas = state.weekOptions

    LaunchedEffect(weeklyCreateRequest) {
        if (weeklyCreateRequest) {
            showCreateDialog = true
            handle?.set("weekly_create_request", false)
        }
    }

    // TOAST DE CREACIÓN SE MANEJA CON state.message PARA EVITAR DUPLICADOS

    LaunchedEffect(state.message) {
        val msg = state.message ?: return@LaunchedEffect
        android.widget.Toast.makeText(ctx, msg, android.widget.Toast.LENGTH_SHORT).show()
        vm.clearMessage()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
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
            Column(modifier = Modifier.fillMaxSize()) {
                WeeklySearchAndFilterBar(
                    searchText = searchText,
                    onSearchTextChange = {
                        searchText = it
                        vm.loadQuizzes(
                            query = it.ifBlank { null },
                            bimesterLabel = selectedBimesterLabel,
                            unidadLabel = selectedUnidadLabel,
                            assignmentId = assignmentId
                        )
                    },
                    bimesterOptions = bimesterOptions,
                    selectedBimester = selectedBimesterLabel,
                    onBimesterChange = {
                        selectedBimesterLabel = it
                        val bimesterId = bimestersState.bimesters.firstOrNull { b -> b.name == it }?.id
                        vm.loadUnitsForBimesterById(bimesterId)
                        vm.loadQuizzes(
                            query = searchText.ifBlank { null },
                            bimesterLabel = it,
                            unidadLabel = selectedUnidadLabel,
                            assignmentId = assignmentId
                        )
                    },
                    selectedUnidad = selectedUnidadLabel,
                    onUnidadChange = {
                        selectedUnidadLabel = it
                        vm.loadQuizzes(
                            query = searchText.ifBlank { null },
                            bimesterLabel = selectedBimesterLabel,
                            unidadLabel = it,
                            assignmentId = assignmentId
                        )
                    },
                    unidadOptions = unidades,
                    isUnitsLoading = state.isUnitsLoading
                )

                WeeklyList(
                    items = state.quizzes,
                    onClick = { quiz ->
                        val handle = navController.currentBackStackEntry?.savedStateHandle
                        handle?.set("apply_grade_id", gradeId)
                        handle?.set("apply_section_id", sectionId)
                        handle?.set("apply_grade_name", gradeName)
                        handle?.set("apply_section_name", sectionName)
                        navController.navigate(NavigationRoutes.applyExam(quiz.id.toString()))
                    },
                    modifier = Modifier.fillMaxSize(),
                    getWeekNumberForItem = vm::getStoredWeekNumberForItem,
                    onDelete = { vm.deleteWeekly(it.id) },
                    onEdit = {
                        editingItem = it
                        selectedBimesterLabel = it.bimesterId?.let { id -> bimesterLabel(id) }
                        val bimId = it.bimesterId
                        vm.loadUnitsForBimesterById(bimId)
                        selectedUnidadLabel = it.unidadId?.let { uid -> unidadLabel(uid) }
                        selectedUnidadLabel?.let { label -> vm.loadWeeksForUnit(label) }
                        selectedSemana = vm.getStoredWeekNumberForItem(it)
                        selectedDate = it.fecha
                        detalle = it.detalle ?: ""
                        showEditDialog = true
                    }
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

    LaunchedEffect(assignmentId) {
        vm.loadQuizzes(assignmentId = assignmentId)
    }

    if (showCreateDialog) {
        CreateWeeklyQuizDialog(
            title = "Crear Semanal",
            bimesters = bimestersForDialog,
            unidades = unidades,
            semanas = semanas,
            selectedBimester = selectedBimesterLabel,
            selectedUnidad = selectedUnidadLabel,
            selectedSemana = selectedSemana,
            selectedDate = selectedDate,
            detalle = detalle,
            onDismiss = {
                showCreateDialog = false
                selectedBimesterLabel = null
                selectedUnidadLabel = null
                selectedSemana = null
                selectedDate = null
                detalle = ""
            },
            onConfirm = { bimLabel, uniLabel, semanaSel, fechaSel, det ->
                val bimId = vm.getBimesterIdFromLabel(bimLabel)
                val uniId = vm.getUnitIdFromLabel(uniLabel)
                vm.createQuiz(
                    bimesterId = bimId,
                    unidadId = uniId,
                    fecha = fechaSel ?: vm.getWeekStartDate(semanaSel) ?: java.time.LocalDate.now().toString(),
                    numQuestions = numQuestions,
                    detalle = det,
                    asignacionId = assignmentId,
                    gradoId = gradeId,
                    seccionId = sectionId,
                    weekNumber = semanaSel
                )
                selectedBimesterLabel = null
                selectedUnidadLabel = null
                selectedSemana = null
                selectedDate = null
                detalle = ""
                showCreateDialog = false
            },
            onChange = { b, u, s, f, d ->
                if (selectedBimesterLabel != b) {
                    val bimesterId = bimestersState.bimesters.firstOrNull { bb -> bb.name == b }?.id
                    vm.loadUnitsForBimesterById(bimesterId)
                    selectedUnidadLabel = null
                    selectedSemana = null
                    selectedDate = null
                }
                if (selectedUnidadLabel != u) {
                    vm.loadWeeksForUnit(u)
                    selectedSemana = null
                    selectedDate = null
                }
                if (selectedSemana != s) {
                    selectedDate = vm.getWeekStartDate(s)
                }
                selectedBimesterLabel = b
                selectedUnidadLabel = u
                selectedSemana = s
                selectedDate = f
                detalle = d
            },
            isUnitsLoading = state.isUnitsLoading,
            isWeeksLoading = state.isWeeksLoading
        )
    }

    if (showEditDialog && editingItem != null) {
        CreateWeeklyQuizDialog(
            title = "Editar Semanal",
            bimesters = bimestersForDialog,
            unidades = state.unitOptions,
            semanas = state.weekOptions,
            selectedBimester = selectedBimesterLabel,
            selectedUnidad = selectedUnidadLabel,
            selectedSemana = selectedSemana,
            selectedDate = selectedDate,
            detalle = detalle,
            onDismiss = {
                showEditDialog = false
                editingItem = null
                selectedBimesterLabel = null
                selectedUnidadLabel = null
                selectedSemana = null
                selectedDate = null
                detalle = ""
            },
            onConfirm = { bimLabel, uniLabel, semanaSel, fechaSel, det ->
                val bimId = vm.getBimesterIdFromLabel(bimLabel)
                val uniId = vm.getUnitIdFromLabel(uniLabel)
                val id = editingItem!!.id
                vm.updateWeekly(
                    id = id,
                    detalle = det,
                    bimesterId = bimId,
                    unidadId = uniId,
                    gradoId = gradeId,
                    seccionId = sectionId,
                    fecha = fechaSel ?: vm.getWeekStartDate(semanaSel),
                    numQuestions = numQuestions,
                    asignacionId = assignmentId,
                    weekNumber = semanaSel
                )
                showEditDialog = false
                editingItem = null
                selectedBimesterLabel = null
                selectedUnidadLabel = null
                selectedSemana = null
                selectedDate = null
                detalle = ""
            },
            onChange = { b, u, s, f, d ->
                if (selectedBimesterLabel != b) {
                    val bimesterId = bimestersState.bimesters.firstOrNull { bb -> bb.name == b }?.id
                    vm.loadUnitsForBimesterById(bimesterId)
                    selectedUnidadLabel = null
                    selectedSemana = null
                    selectedDate = null
                }
                if (selectedUnidadLabel != u) {
                    vm.loadWeeksForUnit(u)
                    selectedSemana = null
                    selectedDate = null
                }
                if (selectedSemana != s) {
                    selectedDate = vm.getWeekStartDate(s)
                }
                selectedBimesterLabel = b
                selectedUnidadLabel = u
                selectedSemana = s
                selectedDate = f
                detalle = d
            },
            isUnitsLoading = state.isUnitsLoading,
            isWeeksLoading = state.isWeeksLoading
        )
    }
}

@Composable
private fun WeeklySearchAndFilterBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    bimesterOptions: List<String>,
    selectedBimester: String?,
    onBimesterChange: (String?) -> Unit,
    selectedUnidad: String?,
    onUnidadChange: (String?) -> Unit,
    unidadOptions: List<String>,
    isUnitsLoading: Boolean
) {
    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                placeholder = { Text("Buscar semanales...") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            IconButton(onClick = { showFilters = !showFilters },
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondary)) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtros",
                    tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = showFilters,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterDropdown(
                        modifier = Modifier.weight(1f),
                        label = "Bimestre",
                        selectedValue = selectedBimester ?: "Todos",
                        options = bimesterOptions,
                        onValueChange = { onBimesterChange(if (it == "Todos") null else it) }
                    )
                    FilterDropdown(
                        modifier = Modifier.weight(1f),
                        label = "Unidad",
                        selectedValue = selectedUnidad ?: "Todas",
                        options = listOf("Todas") + unidadOptions,
                        onValueChange = { onUnidadChange(if (it == "Todas") null else it) },
                        placeholder = when {
                            (selectedBimester ?: "").isBlank() -> "Selecciona bimestre primero"
                            isUnitsLoading -> "Cargando…"
                            unidadOptions.isEmpty() -> "Sin datos"
                            else -> null
                        },
                        enabled = !(selectedBimester ?: "").isBlank() && !isUnitsLoading && unidadOptions.isNotEmpty()
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyList(
    items: List<com.jotadev.aiapaec.domain.models.Quiz>,
    onClick: (com.jotadev.aiapaec.domain.models.Quiz) -> Unit,
    modifier: Modifier = Modifier,
    getWeekNumberForItem: (com.jotadev.aiapaec.domain.models.Quiz) -> Int?,
    onDelete: (com.jotadev.aiapaec.domain.models.Quiz) -> Unit,
    onEdit: (com.jotadev.aiapaec.domain.models.Quiz) -> Unit
) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Assessment, contentDescription = "Sin Semanales", tint = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "No hay Semanales creados", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
                Text(text = "Presiona el botón + para crear un semanal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    } else {
        var expandedWeeklyId by remember { mutableStateOf<Int?>(null) }
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items.sortedBy { it.id }) { item ->
                WeeklyCard(
                    item = item,
                    onClick = onClick,
                    weekNumberProvider = getWeekNumberForItem,
                    isExpanded = expandedWeeklyId == item.id,
                    onToggleExpand = { expandedWeeklyId = if (expandedWeeklyId == item.id) null else item.id },
                    onDelete = { onDelete(item) },
                    onEdit = { onEdit(item) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeeklyCard(
    item: com.jotadev.aiapaec.domain.models.Quiz,
    onClick: (com.jotadev.aiapaec.domain.models.Quiz) -> Unit,
    weekNumberProvider: (com.jotadev.aiapaec.domain.models.Quiz) -> Int?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick(item) },
        border = BorderStroke(
            width = 0.5.dp,
            color = Color.Gray
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                        )
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val weekNumber = weekNumberProvider(item)
                            Text(
                                text = "Semanal N° ${weekNumber ?: "-"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                        IconButton(
                            onClick = onToggleExpand,
                            modifier = Modifier.size(36.dp, 30.dp).padding(vertical = 4.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Más acciones",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoChip(label = "Bimestre", value = bimesterLabel(item.bimesterId), icon = Icons.Default.AcUnit, modifier = Modifier.weight(1f), centered = false)
                        InfoChip(label = "Unidad", value = unidadLabel(item.unidadId), icon = Icons.Default.HourglassTop, modifier = Modifier.weight(1f), centered = true)
                        InfoChip(label = "Fecha", value = item.fecha ?: "-", icon = Icons.Default.DateRange, modifier = Modifier.weight(1f), centered = true)
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoChip(label = "Detalle", value = item.detalle ?: "-", icon = Icons.Default.Info, modifier = Modifier.weight(1f), centered = false)
                    }
                }
            }
            androidx.compose.material3.Surface(
                shape = RoundedCornerShape(topStart = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 0.dp,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text(
                    text = "# ${item.id}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
    androidx.compose.animation.AnimatedVisibility(
        visible = isExpanded,
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
    ) {
        var showDeleteDialog by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.FilledTonalButton(
                onClick = { onEdit() },
                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                    containerColor = androidx.compose.ui.graphics.Color.Blue.copy(alpha = 0.1f),
                    contentColor = androidx.compose.ui.graphics.Color.Blue
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Editar")
            }
            androidx.compose.material3.FilledTonalButton(
                onClick = { showDeleteDialog = true },
                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                    containerColor = androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.1f),
                    contentColor = androidx.compose.ui.graphics.Color.Red
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Eliminar")
            }
        }
        if (showDeleteDialog) {
            androidx.compose.material3.AlertDialog(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        "Confirmar eliminación",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = { Text("¿Eliminar semanal #${item.id}?") },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
    Spacer(modifier = Modifier.size(4.dp))
}

private fun bimesterLabel(id: Int?): String {
    return when (id) {
        1 -> "I BIMESTRE"
        2 -> "II BIMESTRE"
        3 -> "III BIMESTRE"
        4 -> "IV BIMESTRE"
        else -> "-"
    }
}

private fun unidadLabel(id: Int?): String {
    return when (id) {
        1 -> "I UNIDAD"
        2 -> "II UNIDAD"
        3 -> "III UNIDAD"
        4 -> "IV UNIDAD"
        else -> "-"
    }
}

@Composable
private fun CreateWeeklyQuizDialog(
    title: String,
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
    onChange: (String?, String?, Int?, String?, String) -> Unit,
    isUnitsLoading: Boolean,
    isWeeksLoading: Boolean
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.90f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterDropdown(
                        label = "Bimestre",
                        selectedValue = selectedBimester ?: "",
                        options = bimesters,
                        onValueChange = { onChange(it, selectedUnidad, selectedSemana, selectedDate, detalle) },
                        placeholder = "Selecciona bimestre",
                        modifier = Modifier.fillMaxWidth()
                    )
                    FilterDropdown(
                        label = "Unidad",
                        selectedValue = selectedUnidad ?: "",
                        options = unidades,
                        onValueChange = { onChange(selectedBimester, it, selectedSemana, selectedDate, detalle) },
                        placeholder = when {
                            (selectedBimester ?: "").isBlank() -> "Selecciona bimestre primero"
                            isUnitsLoading -> "Cargando…"
                            unidades.isEmpty() -> "Sin datos"
                            else -> "Selecciona unidad"
                        },
                        enabled = !(selectedBimester ?: "").isBlank() && !isUnitsLoading && unidades.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    FilterDropdown(
                        label = "Semana",
                        selectedValue = selectedSemana?.toString() ?: "",
                        options = semanas.map { it.toString() },
                        onValueChange = { sel -> onChange(selectedBimester, selectedUnidad, sel.toIntOrNull(), selectedDate, detalle) },
                        placeholder = when {
                            (selectedUnidad ?: "").isBlank() -> "Selecciona unidad primero"
                            isWeeksLoading -> "Cargando…"
                            semanas.isEmpty() -> "Sin datos"
                            else -> "Selecciona semana"
                        },
                        enabled = !(selectedUnidad ?: "").isBlank() && !isWeeksLoading && semanas.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    DateSelectorField(label = "Fecha", value = selectedDate) { onChange(selectedBimester, selectedUnidad, selectedSemana, it, detalle) }
                    OutlinedTextField(
                        value = detalle,
                        onValueChange = { onChange(selectedBimester, selectedUnidad, selectedSemana, selectedDate, it) },
                        label = { Text("Detalle") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                val canSave = !(
                    (selectedBimester ?: "").isBlank() ||
                    (selectedUnidad ?: "").isBlank() ||
                    selectedSemana == null ||
                    (selectedDate ?: "").isBlank()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Cancelar") }
                    Button(
                        onClick = { onConfirm(selectedBimester, selectedUnidad, selectedSemana, selectedDate, detalle); onDismiss() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = canSave
                    ) { Text("Guardar") }
                }
            }
        }
    }
}

@Composable
private fun DateSelectorField(label: String, value: String?, onChange: (String) -> Unit) {
    val ctx = LocalContext.current
    Column {
        OutlinedTextField(
            value = value ?: "",
            onValueChange = { input ->
                val digits = input.filter { it.isDigit() }.take(8)
                val y = digits.take(4)
                val m = if (digits.length > 4) digits.drop(4).take(2) else ""
                val d = if (digits.length > 6) digits.drop(6).take(2) else ""
                val formatted = when {
                    digits.length <= 4 -> y
                    digits.length <= 6 -> "$y-$m"
                    else -> "$y-$m-$d"
                }
                onChange(formatted)
            },
            readOnly = false,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            trailingIcon = {
                IconButton(onClick = {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(ctx, { _, y, m, d ->
                        val month = (m + 1).toString().padStart(2, '0')
                        val day = d.toString().padStart(2, '0')
                        onChange("$y-$month-$day")
                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                }) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Seleccionar fecha"
                    )
                }
            }
        )
    }
}
