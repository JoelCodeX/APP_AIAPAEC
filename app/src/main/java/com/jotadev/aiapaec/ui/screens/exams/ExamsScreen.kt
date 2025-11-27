package com.jotadev.aiapaec.ui.screens.exams

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.jotadev.aiapaec.navigation.NavigationRoutes
import com.jotadev.aiapaec.ui.components.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jotadev.aiapaec.presentation.BimestersViewModel
import com.jotadev.aiapaec.ui.components.exam.CreateExamDialog
import com.jotadev.aiapaec.ui.components.exam.ExamsList
import com.jotadev.aiapaec.ui.screens.classes.ClassesViewModel
import com.jotadev.aiapaec.ui.components.exam.Exam as UiExam

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ExamsScreen(navController: NavController) {
    // ViewModel for dynamic bimesters
    val bimestersVm: BimestersViewModel = viewModel()
    val bimestersState by bimestersVm.uiState.collectAsStateWithLifecycle()
    // ViewModel for dynamic classes
    val classesVm: ClassesViewModel = viewModel()
    val classesState by classesVm.uiState.collectAsStateWithLifecycle()
    // ViewModel for quizzes-backed exams
    val examsVm: ExamsViewModel = viewModel()
    val examsState by examsVm.uiState.collectAsStateWithLifecycle()

    // ESTADOS PARA FILTROS Y BUSQUEDA
    var searchText by remember { mutableStateOf("") }
    var selectedBimester by remember { mutableStateOf("Todos") }
    var selectedClass by remember { mutableStateOf("Todas") }
    var showDialog by remember { mutableStateOf(false) }
    var editingExam by remember { mutableStateOf<UiExam?>(null) }

    val handle = navController.currentBackStackEntry?.savedStateHandle
    val examsCreateFlow = handle?.getStateFlow("exams_create_request", false) ?: MutableStateFlow(false)
    val examsCreateRequest by examsCreateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(examsCreateRequest) {
        if (examsCreateRequest) {
            editingExam = null
            showDialog = true
            handle?.set("exams_create_request", false)
        }
    }
    var isRefreshing by remember { mutableStateOf(false) }
    val bimesterOptions = remember(bimestersState.bimesters) {
        listOf("Todos") + bimestersState.bimesters.map { it.name }
    }
    val classOptions = remember(classesState.classes) {
        listOf("Todas") + classesState.classes.map { it.name }
    }
    // LISTA DE EXÁMENES DESDE EL VIEWMODEL (filtrado en backend)
    val filteredExams = examsState.exams

    // Auto-refresco cada 30s (simulado)
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(30_000)
            examsVm.refreshExams()
            isRefreshing = examsState.isLoading
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val pullState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = {
                examsVm.refreshExams()
                isRefreshing = examsState.isLoading
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // BARRA DE BUSQUEDA Y FILTROS
                SearchAndFilterBar(
                    searchText = searchText,
                    onSearchTextChange = {
                        searchText = it
                        val bimesterId = bimestersState.bimesters.firstOrNull { b -> b.name == selectedBimester }?.id
                        examsVm.loadExams(query = searchText.ifBlank { null }, gradoId = null, seccionId = null, bimesterId = bimesterId)
                    },
                    selectedBimester = selectedBimester,
                    onBimesterChange = {
                        selectedBimester = it
                        val bimesterId = bimestersState.bimesters.firstOrNull { b -> b.name == selectedBimester }?.id
                        examsVm.loadExams(query = searchText.ifBlank { null }, gradoId = null, seccionId = null, bimesterId = bimesterId)
                    },
                    selectedClass = selectedClass,
                    onClassChange = {
                        selectedClass = it
                        val bimesterId = bimestersState.bimesters.firstOrNull { b -> b.name == selectedBimester }?.id
                        examsVm.loadExams(query = if (searchText.isBlank()) null else searchText, gradoId = null, seccionId = null, bimesterId = bimesterId)
                    },
                    bimesters = bimesterOptions,
                    classes = classOptions
                )

                // LISTA DE EXAMENES
                ExamsList(
                    exams = filteredExams,
                    onEditExam = { exam ->
                        editingExam = exam
                        showDialog = true
                    },
                    onDeleteExam = { exam ->
                        examsVm.deleteExam(exam.id)
                    },
                    onExamClick = { exam ->
                        navController.navigate(NavigationRoutes.applyExam(exam.id))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }

        // DIALOGO PARA CREAR / EDITAR EXAMEN
        CreateExamDialog(
            isVisible = showDialog,
            onDismiss = { showDialog = false },
            onSaveExam = { name, className, bimester ->
                val classId = classesState.classes.firstOrNull { it.name == className }?.id
                val bimesterId = bimestersState.bimesters.firstOrNull { it.name == bimester }?.id
                if (classId != null && bimesterId != null) {
                    val editingIdInt = editingExam?.id?.toIntOrNull()
                    if (editingExam == null) {
                        examsVm.createExam(classId, bimesterId, name)
                    } else if (editingIdInt != null) {
                        examsVm.updateExam(
                            id = editingIdInt,
                            classId = classId,
                            bimesterId = bimesterId,
                            detalle = name
                        )
                    }
                }
            },
            bimesters = bimestersState.bimesters.map { it.name },
            classes = classesState.classes.map { it.name },
            initialName = editingExam?.name ?: "",
            initialClass = editingExam?.className ?: "",
            initialBimester = editingExam?.bimester ?: "",
            title = if (editingExam == null) "Crear Nuevo Examen" else "Editar Examen"
        )
    }
}
