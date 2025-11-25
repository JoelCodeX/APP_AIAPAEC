package com.jotadev.aiapaec.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun CreateFormatDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (grade: String, section: String, numQuestions: Int, formatType: String, scoreFormat: String) -> Unit,
    onGradeChange: ((String) -> Unit)? = null,
    isMetaLoading: Boolean,
    gradeOptions: List<String>,
    sectionOptions: List<String>,
    sectionsByGrade: Map<String, List<String>> = emptyMap(),
    title: String,
    initialGrade: String? = null,
    initialSection: String? = null,
    initialNumQuestions: Int? = null,
    initialFormatType: String? = null,
    initialScoreFormat: String? = null,
    confirmButtonText: String = "Guardar",
    formatTypeOptions: List<String> = FormatOptions.formatTypes,
    scoreFormatOptions: List<String> = FormatOptions.scoreFormats
 ) {
    val grade = remember(isOpen, initialGrade) { mutableStateOf<String?>(initialGrade) }
    val section = remember(isOpen, initialSection) { mutableStateOf<String?>(initialSection) }
    val numQuestions = remember(isOpen, initialNumQuestions) { mutableStateOf<String>(initialNumQuestions?.toString() ?: "") }
    val formatType = remember(isOpen, initialFormatType) { mutableStateOf<String?>(initialFormatType) }
    val scoreFormat = remember(isOpen, initialScoreFormat) { mutableStateOf<String?>(initialScoreFormat) }

    if (!isOpen) return

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)){
        LaunchedEffect(grade.value) {
            val g = grade.value
            if (!g.isNullOrBlank()) onGradeChange?.invoke(g)
        }
        LaunchedEffect(isOpen) {
            if (isOpen) {
                grade.value = initialGrade
                section.value = initialSection
                numQuestions.value = initialNumQuestions?.toString() ?: ""
                formatType.value = initialFormatType
                scoreFormat.value = initialScoreFormat
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth(0.90f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onPrimary
            )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterDropdown(
                            label = "Grado",
                            selectedValue = grade.value ?: "",
                            options = gradeOptions,
                            onValueChange = { 
                                grade.value = it
                                section.value = null
                                onGradeChange?.invoke(it)
                            },
                            placeholder = if (isMetaLoading) "Cargando…" else if (gradeOptions.isEmpty()) "Sin datos" else "Selecciona grado",
                            enabled = !isMetaLoading && gradeOptions.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        )
                        val currentSections = sectionsByGrade[grade.value ?: ""] ?: sectionOptions
                        FilterDropdown(
                            label = "Sección",
                            selectedValue = section.value ?: "",
                            options = currentSections,
                            onValueChange = { section.value = it },
                            placeholder = if (isMetaLoading) "Cargando…" else if (currentSections.isEmpty()) "Sin datos" else if ((grade.value ?: "").isBlank()) "Selecciona grado primero" else "Selecciona sección",
                            enabled = !isMetaLoading && currentSections.isNotEmpty() && !(grade.value ?: "").isBlank(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                    FilterDropdown(
                        label = "Formato",
                        selectedValue = formatType.value ?: "",
                        options = formatTypeOptions,
                        onValueChange = { formatType.value = it },
                        placeholder = "Selecciona formato",
                        modifier = Modifier.weight(1f)
                    )
                    FilterDropdown(
                        label = "N° de preguntas",
                        selectedValue = numQuestions.value,
                        options = FormatOptions.questionCounts,
                        onValueChange = { numQuestions.value = it },
                        placeholder = "Selecciona número",
                        modifier = Modifier.weight(1f)
                    )
                }

                    FilterDropdown(
                        label = "Puntaje",
                        selectedValue = scoreFormat.value ?: "",
                        options = scoreFormatOptions,
                        onValueChange = { scoreFormat.value = it },
                        placeholder = "Selecciona puntaje",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val canSave =
                            grade.value != null &&
                            section.value != null &&
                            formatType.value != null &&
                            scoreFormat.value != null &&
                            numQuestions.value.toIntOrNull() != null
                        androidx.compose.material3.OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Cancelar") }
                        androidx.compose.material3.Button(
                            onClick = {
                                val g = grade.value ?: return@Button
                                val s = section.value ?: return@Button
                                val f = formatType.value ?: return@Button
                                val sf = scoreFormat.value ?: return@Button
                                val n = numQuestions.value.toIntOrNull() ?: return@Button
                                onConfirm(g, s, n, f, sf)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = canSave
                        ) { Text(confirmButtonText) }
                    }
                }
            }
    }

}
