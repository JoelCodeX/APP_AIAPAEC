package com.jotadev.aiapaec.ui.screens.exams.answers

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jotadev.aiapaec.domain.models.QuizAnswer
import kotlinx.coroutines.delay

// import eliminado: NavigationRoutes no se utiliza

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswersScreen(navController: NavController, examId: String) {
    val vm: AnswersViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var backAfterSave by remember { mutableStateOf(false) }
    val handle = navController.currentBackStackEntry?.savedStateHandle

    LaunchedEffect(examId) {
        vm.load(examId)
    }

    // Escucha acciones del TopBar global
    LaunchedEffect(navController) {
        while (true) {
            val toggle = handle?.remove<Boolean>("answers_edit_toggle")
            if (toggle == true) {
                editMode = !editMode
                handle?.set("answers_is_editing", editMode)
            }
            val reqDelete = handle?.remove<Boolean>("answers_delete_request")
            if (reqDelete == true) {
                showDeleteDialog = true
            }
            val reqSave = handle?.remove<Boolean>("answers_save_request")
            if (reqSave == true) {
                if (editMode && state.hasUnsavedChanges) {
                    vm.saveAnswers(examId)
                }
            }
            val reqBack = handle?.remove<Boolean>("answers_back_request")
            if (reqBack == true) {
                if (editMode) {
                    if (state.hasUnsavedChanges) {
                        showUnsavedDialog = true
                    } else {
                        editMode = false
                        handle?.set("answers_is_editing", false)
                    }
                } else {
                    navController.popBackStack()
                }
            }
            delay(100)
        }
    }

    LaunchedEffect(editMode) {
        handle?.set("answers_is_editing", editMode)
    }

    LaunchedEffect(state.hasUnsavedChanges) {
        handle?.set("answers_has_changes", state.hasUnsavedChanges)
    }

    LaunchedEffect(state.answers) {
        handle?.set("answers_has_any", state.answers.isNotEmpty())
    }

    BackHandler(enabled = editMode) {
        if (state.hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            editMode = false
            handle?.set("answers_is_editing", false)
        }
    }

    LaunchedEffect(state.lastSaveSucceeded) {
        if (state.lastSaveSucceeded) {
            snackbarHostState.showSnackbar("Cambios realizados exitosamente")
            if (backAfterSave) {
                backAfterSave = false
                editMode = false
                handle?.set("answers_is_editing", false)
            }
            vm.ackSaveSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val quiz = state.quiz
            if (quiz != null && !editMode) {
                ExamSummaryCard(
                    title = (quiz.detalle ?: "QUIZ SEMANAL"),
                    className = listOfNotNull(quiz.gradoNombre, quiz.seccionNombre).joinToString(" ").ifBlank { "—" },
                    numQuestions = quiz.numQuestions ?: 0,
                    pointsPerQuestion = state.pointsPerQuestion
                )
            }

            if (state.answers.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.answers) { answer ->
                        AnswerItem(
                            answer = answer,
                            pointsPerQuestion = state.pointsPerQuestion,
                            isEditable = editMode,
                            onOptionChange = { q, opt -> if (editMode) vm.updateAnswerOption(q, opt) }
                        )
                    }
                }

                if (editMode && state.hasUnsavedChanges) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { vm.saveAnswers(examId) }) {
                        Text("Guardar cambios")
                    }
                }
            }
        }
    }

    // DIALOGO DE CONFIRMACION DE ELIMINACION
    if (showDeleteDialog) {
        AlertDialog(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Confirmar eliminación",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = { Text("¿Estás seguro de que deseas eliminar el solucionario?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteAnswerKey(examId)
                        showDeleteDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // DIALOGO DE CAMBIOS NO GUARDADOS
    if (showUnsavedDialog) {
        AlertDialog(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showUnsavedDialog = false },
            title = {
                Text(
                    text = "Cambios sin guardar",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = { Text("Tienes cambios sin guardar. ¿Deseas guardar antes de salir?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedDialog = false
                        backAfterSave = true
                        vm.saveAnswers(examId)
                    }
                ) { Text("Guardar y salir") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showUnsavedDialog = false
                    vm.load(examId)
                    editMode = false
                    handle?.set("answers_is_editing", false)
                }) { Text("Salir sin guardar") }
            }
        )
    }
}

@Composable
private fun ExamSummaryCard(
    title: String,
    className: String,
    numQuestions: Int,
    pointsPerQuestion: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.School, contentDescription = "Clase", tint = MaterialTheme.colorScheme.primary)
                Text(text = "Clase: $className", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "Preguntas", tint = MaterialTheme.colorScheme.primary)
                Text(text = "Preguntas: $numQuestions", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(6.dp))
            val ptsText = if (pointsPerQuestion % 1.0 == 0.0) pointsPerQuestion.toInt().toString() else "%.2f".format(pointsPerQuestion)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Puntos", tint = MaterialTheme.colorScheme.primary)
                Text(text = "Puntos por pregunta:", color = MaterialTheme.colorScheme.onSecondary)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100))
                        .background(MaterialTheme.colorScheme.secondary.copy(0.6f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$ptsText pts",
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sin respuestas cargadas aún",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AnswerItem(
    answer: QuizAnswer,
    pointsPerQuestion: Double,
    isEditable: Boolean,
    onOptionChange: (Int, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Resolución ${answer.questionNumber.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf("A", "B", "C", "D", "E").forEach { option ->
                        val selected = option == answer.correctOption.uppercase()
                        val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        val base = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(bg)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        Box(
                            modifier = if (isEditable) {
                                base.clickable { onOptionChange(answer.questionNumber, option) }
                            } else base,
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                color = fg,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
            val pointsText = if (pointsPerQuestion % 1.0 == 0.0) pointsPerQuestion.toInt().toString() else "%.2f".format(pointsPerQuestion)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100))
                    .background(MaterialTheme.colorScheme.secondary.copy(0.6f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$pointsText pts",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}
