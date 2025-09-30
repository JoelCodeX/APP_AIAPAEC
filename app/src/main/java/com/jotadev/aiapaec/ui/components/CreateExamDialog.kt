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
    onCreateExam: (String, String, String, String) -> Unit,
    classes: List<String> = listOf("Matemáticas", "Comunicación", "Ciencias", "Personal Social"),
    bimesters: List<String> = listOf("I Bimestre", "II Bimestre", "III Bimestre", "IV Bimestre"),
    examTypes: List<String> = listOf("20 preguntas", "50 preguntas")
) {
    if (isVisible) {
        var examName by remember { mutableStateOf("") }
        var selectedClass by remember { mutableStateOf(classes.first()) }
        var selectedBimester by remember { mutableStateOf(bimesters.first()) }
        var selectedType by remember { mutableStateOf(examTypes.first()) }
        var nameError by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
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
                        text = "Crear Nuevo Examen",
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
                        placeholder = { Text("Ej: Examen de Matemáticas - Álgebra") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError,
                        supportingText = if (nameError) {
                            { Text("El nombre es obligatorio", color = MaterialTheme.colorScheme.error) }
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
                        onValueChange = { selectedClass = it }
                    )

                    // SELECTOR DE BIMESTRE
                    FilterDropdown(
                        label = "Bimestre",
                        selectedValue = selectedBimester,
                        options = bimesters,
                        onValueChange = { selectedBimester = it }
                    )

                    // SELECTOR DE TIPO DE CARTILLA
                    FilterDropdown(
                        label = "Tipo de cartilla",
                        selectedValue = selectedType,
                        options = examTypes,
                        onValueChange = { selectedType = it }
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
                                    onCreateExam(examName, selectedClass, selectedBimester, selectedType)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Crear Examen")
                        }
                    }
                }
            }
        }
    }
}