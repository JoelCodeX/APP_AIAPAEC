package com.jotadev.aiapaec.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButtonDefaults.iconButtonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onClick: (Exam) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick(exam) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ENCABEZADO CON NOMBRE Y ACCIONES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exam.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // BOTON EDITAR (SOLO SI NO FUE APLICADO)
                    if (!exam.isApplied) {
                        IconButton(
                            onClick = { onEditClick(exam) },
                            modifier = Modifier.size(36.dp),
                            colors = iconButtonColors(
                                contentColor = Color.Blue,
                                containerColor = Color.Blue.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar examen",
                                tint = Color.Blue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    // BOTON ELIMINAR
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(36.dp),
                        colors = iconButtonColors(
                            contentColor = Color.Red,
                            containerColor = Color.Red.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar examen",
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                modifier = Modifier.height(8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )
            Spacer(modifier = Modifier.height(8.dp))

            // INFORMACION DEL EXAMEN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ExamInfoChip(
                    label = "Clase",
                    value = exam.className,
                    modifier = Modifier.weight(1f)
                )
                ExamInfoChip(
                    label = "Bimestre",
                    value = exam.bimester,
                    modifier = Modifier.weight(1f)
                )
                ExamInfoChip(
                    label = "Fecha",
                    value = exam.date,
                    modifier = Modifier.weight(1f)
                )
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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}