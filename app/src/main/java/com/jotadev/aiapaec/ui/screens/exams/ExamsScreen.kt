package com.jotadev.aiapaec.ui.screens.exams

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jotadev.aiapaec.navigation.NavigationRoutes
import com.jotadev.aiapaec.ui.components.*
import java.util.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jotadev.aiapaec.presentation.BimestersViewModel
import com.jotadev.aiapaec.ui.screens.classes.ClassesViewModel
import com.jotadev.aiapaec.ui.components.Exam as UiExam

@OptIn(ExperimentalMaterial3Api::class)
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ScreenTopAppBar(
                screenTitle = "Exámenes",
                subtitle = "Gestión de exámenes y evaluaciones"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingExam = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear examen"
                )
            }
        }
    ) { paddingValues ->
        val swipeState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
        SwipeRefresh(
            state = swipeState,
            onRefresh = {
                examsVm.refreshExams()
                isRefreshing = examsState.isLoading
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // BARRA DE BUSQUEDA Y FILTROS
                SearchAndFilterBar(
                    searchText = searchText,
                    onSearchTextChange = {
                        searchText = it
                        val classId = classesState.classes.firstOrNull { c -> c.name == selectedClass }?.id
                        val bimesterId = bimestersState.bimesters.firstOrNull { b -> b.name == selectedBimester }?.id
                        examsVm.loadExams(query = searchText.ifBlank { null }, classId = classId, bimesterId = bimesterId)
                    },
                    selectedBimester = selectedBimester,
                    onBimesterChange = {
                        selectedBimester = it
                        val classId = classesState.classes.firstOrNull { c -> c.name == selectedClass }?.id
                        val bimesterId = bimestersState.bimesters.firstOrNull { b -> b.name == selectedBimester }?.id
                        examsVm.loadExams(query = if (searchText.isBlank()) null else searchText, classId = classId, bimesterId = bimesterId)
                    },
                    selectedClass = selectedClass,
                    onClassChange = {
                        selectedClass = it
                        val classId = classesState.classes.firstOrNull { c -> c.name == selectedClass }?.id
                        val bimesterId = bimestersState.bimesters.firstOrNull { b -> b.name == selectedBimester }?.id
                        examsVm.loadExams(query = if (searchText.isBlank()) null else searchText, classId = classId, bimesterId = bimesterId)
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
                        examsVm.createExam(name, classId, bimesterId)
                    } else if (editingIdInt != null) {
                        examsVm.updateExam(id = editingIdInt, title = name, classId = classId, bimesterId = bimesterId)
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