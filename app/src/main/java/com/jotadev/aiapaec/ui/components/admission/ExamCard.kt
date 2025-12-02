package com.jotadev.aiapaec.ui.components.admission

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults.iconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Exam(
    val id: String,
    val name: String,
    val className: String,
    val bimester: String,
    val type: String,
    val date: String,
    val isApplied: Boolean = false,
    val numQuestions: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamCard(
    exam: Exam,
    onEditClick: (Exam) -> Unit,
    onDeleteClick: (Exam) -> Unit,
    onClick: (Exam) -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick(exam) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = exam.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onToggleExpand() },
                            modifier = Modifier.size(36.dp),
                            colors = iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Más acciones",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = exam.numQuestions?.let { "$it preguntas" } ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // INFORMACION DEL EXAMEN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ExamInfoChip(label = "Clase", value = exam.className, icon = Icons.Default.School, modifier = Modifier.weight(1f))
                ExamInfoChip(label = "Bimestre", value = exam.bimester, icon = Icons.Default.Leaderboard, modifier = Modifier.weight(1f))
                ExamInfoChip(label = "Fecha", value = exam.date, icon = Icons.Default.Event, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExamInfoChip(
                    label = "Tipo",
                    value = exam.numQuestions?.let { "$it preguntas" } ?: "Sin asignar",
                    icon = Icons.Default.Assignment,
                    modifier = Modifier.weight(1f),
                )

                // ESTADO DEL EXAMEN
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = if (exam.isApplied) "Aplicado" else "Pendiente",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (exam.isApplied) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = if (exam.isApplied) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!exam.isApplied) {
                FilledTonalButton(
                    onClick = { onEditClick(exam) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.Blue.copy(alpha = 0.1f),
                        contentColor = Color.Blue
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar examen")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Editar")
                }
            }
            FilledTonalButton(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color.Red.copy(alpha = 0.1f),
                    contentColor = Color.Red
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar examen")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Eliminar")
            }
        }
    }

    // DIALOGO DE CONFIRMACION PARA ELIMINAR
    if (showDeleteDialog) {
        AlertDialog(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("¿Estás seguro de que deseas eliminar el examen \"${exam.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(exam)
                        showDeleteDialog = false
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
}

@Composable
fun ExamInfoChip(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = value, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}
