package com.jotadev.aiapaec.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExamDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSaveExam: (String, String, String) -> Unit,
    classes: List<String> = listOf("Matemáticas", "Comunicación", "Ciencias", "Personal Social"),
    bimesters: List<String> = emptyList(),
    initialName: String = "",
    initialClass: String = "",
    initialBimester: String = "",
    title: String = "Crear Nuevo Examen"
) {
    if (isVisible) {
        var examName by remember { mutableStateOf(initialName) }
        var selectedClass by remember { mutableStateOf(initialClass) }
        var selectedBimester by remember { mutableStateOf(initialBimester) }
        var nameError by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // TITULO
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // NOMBRE DEL EXAMEN
                    OutlinedTextField(
                        value = examName,
                        onValueChange = {
                            examName = it
                            nameError = false
                        },
                        label = { Text("Nombre del examen") },
                        placeholder = { Text("Ej: Evaluación semanal N° 01") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError,
                        supportingText = if (nameError) {
                            {
                                Text(
                                    "El nombre es obligatorio",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else null,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // SELECTOR DE CLASE
                    FilterDropdown(
                        label = "Clase",
                        selectedValue = selectedClass,
                        options = classes,
                        onValueChange = { selectedClass = it },
                        placeholder = "Selecciona una clase"
                    )

                    // SELECTOR DE BIMESTRE
                    FilterDropdown(
                        label = "Bimestre",
                        selectedValue = selectedBimester,
                        options = bimesters,
                        onValueChange = { selectedBimester = it },
                        placeholder = "Selecciona un bimestre"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // BOTONES
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                if (examName.isBlank()) {
                                    nameError = true
                                } else {
                                    onSaveExam(examName, selectedClass, selectedBimester)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = examName.isNotBlank() && selectedClass.isNotBlank() && selectedBimester.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}