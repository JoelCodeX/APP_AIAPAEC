package com.jotadev.aiapaec.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jotadev.aiapaec.ui.screens.format.FormatItem

@Composable
fun EditFormatDialog(
    isOpen: Boolean,
    item: FormatItem,
    onDismiss: () -> Unit,
    onUpdate: (id: String, grade: String, section: String, numQuestions: Int, formatType: String, scoreFormat: String) -> Unit,
    isMetaLoading: Boolean = false,
    gradeOptions: List<String> = emptyList(),
    sectionOptions: List<String> = emptyList()
) {
    if (!isOpen) return

    val grade = remember { mutableStateOf(item.grade) }
    val section = remember { mutableStateOf(item.section) }
    val numQuestions = remember { mutableStateOf(item.numQuestions.toString()) }
    val formatType = remember { mutableStateOf(item.formatType) }
    val scoreText = when (item.scoreFormat) {
        "5.0" -> "Formato 20 Preguntas Correcta:5 Nula:1 Incorrecta :- 1"
        "4.0" -> "Formato 25 Preguntas Correcta:4 Nula:1 Incorrecta :- 1"
        else -> "Formato 30 Preguntas Correcta:3.33 Nula:1 Incorrecta :- 1"
    }
    val scoreFormat = remember { mutableStateOf(scoreText) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.90f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Editar formato", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterDropdown(
                        label = "Grado",
                        selectedValue = grade.value,
                        options = gradeOptions,
                        onValueChange = { grade.value = it },
                        placeholder = if (isMetaLoading) "Cargando…" else if (gradeOptions.isEmpty()) "Sin datos" else "Selecciona grado",
                        enabled = !isMetaLoading && gradeOptions.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    )
                    FilterDropdown(
                        label = "Sección",
                        selectedValue = section.value,
                        options = sectionOptions,
                        onValueChange = { section.value = it },
                        placeholder = if (isMetaLoading) "Cargando…" else if (sectionOptions.isEmpty()) "Sin datos" else "Selecciona sección",
                        enabled = !isMetaLoading && sectionOptions.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterDropdown(
                        label = "Formato",
                        selectedValue = formatType.value,
                        options = listOf("Estructura 3ero Primaria", "Estructura 4to Primaria", "Estructura 5to Primaria"),
                        onValueChange = { formatType.value = it },
                        placeholder = "Selecciona formato",
                        modifier = Modifier.weight(1f)
                    )
                    FilterDropdown(
                        label = "N° de preguntas",
                        selectedValue = numQuestions.value,
                        options = listOf("10", "20", "25", "30", "50", "100"),
                        onValueChange = { numQuestions.value = it },
                        placeholder = "Selecciona número",
                        modifier = Modifier.weight(1f)
                    )
                }

                FilterDropdown(
                    label = "Puntaje",
                    selectedValue = scoreFormat.value,
                    options = listOf(
                        "Formato 20 Preguntas Correcta:5 Nula:1 Incorrecta :- 1",
                        "Formato 25 Preguntas Correcta:4 Nula:1 Incorrecta :- 1",
                        "Formato 30 Preguntas Correcta:3.33 Nula:1 Incorrecta :- 1"
                    ),
                    onValueChange = { scoreFormat.value = it },
                    placeholder = "Selecciona puntaje",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val canSave =
                        grade.value.isNotBlank() &&
                        section.value.isNotBlank() &&
                        formatType.value.isNotBlank() &&
                        scoreFormat.value.isNotBlank() &&
                        numQuestions.value.toIntOrNull() != null
                    androidx.compose.material3.OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text("Cancelar")
                    }
                    androidx.compose.material3.Button(
                        onClick = {
                            val g = grade.value
                            val s = section.value
                            val f = formatType.value
                            val sf = scoreFormat.value
                            val n = numQuestions.value.toIntOrNull() ?: return@Button
                            onUpdate(item.id, g, s, n, f, sf)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = canSave
                    ) { Text("Guardar cambios") }
                }
            }
        }
    }
}