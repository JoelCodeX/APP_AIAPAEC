package com.jotadev.aiapaec.ui.screens.exams

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jotadev.aiapaec.ui.components.*
import java.util.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(navController: NavController) {
    // ESTADOS PARA FILTROS Y BUSQUEDA
    var searchText by remember { mutableStateOf("") }
    var selectedBimester by remember { mutableStateOf("Todos") }
    var selectedClass by remember { mutableStateOf("Todas") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    // LISTA DE EXAMENES (SIMULADA)
    var examsList by remember { 
        mutableStateOf(
            listOf(
                Exam(
                    id = "1",
                    name = "Examen de Álgebra Básica",
                    className = "Matemáticas",
                    bimester = "I Bimestre",
                    type = "20 preguntas",
                    date = "2024-09-15",
                    isApplied = false
                ),
                Exam(
                    id = "2",
                    name = "Evaluación de Comprensión Lectora",
                    className = "Comunicación",
                    bimester = "I Bimestre",
                    type = "50 preguntas",
                    date = "2024-09-15",
                    isApplied = true
                ),
                Exam(
                    id = "3",
                    name = "Examen de Ciencias Naturales",
                    className = "Ciencias",
                    bimester = "II Bimestre",
                    type = "20 preguntas",
                    date = "2024-09-15",
                    isApplied = false
                )
            )
        )
    }

    // FILTRAR EXAMENES SEGUN CRITERIOS
    val filteredExams = examsList.filter { exam ->
        val matchesSearch = exam.name.contains(searchText, ignoreCase = true) ||
                           exam.className.contains(searchText, ignoreCase = true)
        val matchesBimester = selectedBimester == "Todos" || exam.bimester == selectedBimester
        val matchesClass = selectedClass == "Todas" || exam.className == selectedClass
        
        matchesSearch && matchesBimester && matchesClass
    }

    // Auto-refresco cada 30s (simulado)
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(30_000)
            // Aquí podrías conectar a un ViewModel cuando haya API
            isRefreshing = true
            // Simular tarea de actualización rápida
            delay(500)
            isRefreshing = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.onPrimary,
        topBar = {
            ScreenTopAppBar(
                screenTitle = "Exámenes",
                subtitle = "Gestión de exámenes y evaluaciones"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
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
                // Aquí podrías conectar a un ViewModel cuando haya API
                isRefreshing = true
                // Simular tarea de actualización rápida
                isRefreshing = false
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
                    onSearchTextChange = { searchText = it },
                    selectedBimester = selectedBimester,
                    onBimesterChange = { selectedBimester = it },
                    selectedClass = selectedClass,
                    onClassChange = { selectedClass = it }
                )

                // LISTA DE EXAMENES
                ExamsList(
                    exams = filteredExams,
                    onEditExam = { exam ->
                        // LOGICA PARA EDITAR EXAMEN
                    },
                    onDeleteExam = { exam ->
                        examsList = examsList.filter { it.id != exam.id }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // DIALOGO PARA CREAR EXAMEN
        CreateExamDialog(
            isVisible = showCreateDialog,
            onDismiss = { showCreateDialog = false },
            onCreateExam = { name, className, bimester, type ->
                val newExam = Exam(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    className = className,
                    bimester = bimester,
                    type = type,
                    date = "2024-09-15",
                    isApplied = false
                )
                examsList = examsList + newExam
            }
        )
    }
}