package com.jotadev.aiapaec.ui.components.admission

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
import com.jotadev.aiapaec.ui.components.FilterDropdown

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
    title: String = "Crear Nuevo Examen",
    examNames: List<String> = generateWeeklyExamNames(8)
) {
    if (isVisible) {
        var examName by remember { mutableStateOf(initialName) }
        var selectedClass by remember { mutableStateOf(initialClass) }
        var selectedBimester by remember { mutableStateOf(initialBimester) }

        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
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
                    FilterDropdown(
                        label = "Nombre del examen",
                        selectedValue = examName,
                        options = examNames,
                        onValueChange = { examName = it },
                        placeholder = "Selecciona un examen"
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
                                onSaveExam(examName, selectedClass, selectedBimester)
                                onDismiss()
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

private fun generateWeeklyExamNames(count: Int): List<String> =
    (1..count).map { "Examen Semanal N°" + it.toString().padStart(2, '0') }