package com.jotadev.aiapaec.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ExamsList(
    exams: List<Exam>,
    onEditExam: (Exam) -> Unit,
    onDeleteExam: (Exam) -> Unit,
    onExamClick: (Exam) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedExamId by remember { mutableStateOf<String?>(null) }
    if (exams.isEmpty()) {
        // ESTADO VACIO
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = "Sin Exámenes",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No hay exámenes creados",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Presiona el botón + para crear tu primer examen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // LISTA DE EXAMENES
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(exams) { exam ->
                ExamCard(
                    exam = exam,
                    onEditClick = onEditExam,
                    onDeleteClick = onDeleteExam,
                    onClick = onExamClick,
                    isExpanded = expandedExamId == exam.id,
                    onToggleExpand = {
                        expandedExamId = if (expandedExamId == exam.id) null else exam.id
                    }
                )
            }
            
            // ESPACIO ADICIONAL AL FINAL PARA EL FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
