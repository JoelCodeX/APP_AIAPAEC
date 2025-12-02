package com.jotadev.aiapaec.ui.components.students

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.jotadev.aiapaec.ui.components.FilterDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsSearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    // Filtros por grado y sección
    selectedGrade: String?,
    onGradeChange: (String?) -> Unit,
    gradeOptions: List<String>,
    selectedSection: String?,
    onSectionChange: (String?) -> Unit,
    sectionOptions: List<String>,
    isMetaLoading: Boolean,
    // Filtro por clase
    selectedClass: String,
    onClassChange: (String) -> Unit,
    classOptions: List<String>,
    modifier: Modifier = Modifier
) {
    var isFilterExpanded by remember { mutableStateOf(false) }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // BARRA DE BUSQUEDA Y FILTRO
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // CAMPO DE BUSQUEDA
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar por nombre o ID...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true
            )
            // BOTÓN DE FILTRO
            IconButton(
                onClick = { isFilterExpanded = !isFilterExpanded },
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondary)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtros",
                    tint = if (isFilterExpanded) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // FILTROS EXPANDIBLES
        AnimatedVisibility(visible = isFilterExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // FILTRO POR GRADO
                FilterDropdown(
                    label = "Grado",
                    selectedValue = selectedGrade ?: "Todos",
                    options = listOf("Todos") + gradeOptions,
                    onValueChange = { v -> onGradeChange(if (v == "Todos") null else v) },
                    placeholder = if (isMetaLoading) "Cargando grados..." else "Selecciona grado"
                )

                // FILTRO POR SECCIÓN
                FilterDropdown(
                    label = "Sección",
                    selectedValue = selectedSection ?: "Todas",
                    options = listOf("Todas") + sectionOptions,
                    onValueChange = { v -> onSectionChange(if (v == "Todas") null else v) },
                    placeholder = if (isMetaLoading) "Cargando secciones..." else "Selecciona sección",
                    enabled = sectionOptions.isNotEmpty()
                )

                // FILTRO POR CLASE
                FilterDropdown(
                    label = "Clase",
                    selectedValue = selectedClass,
                    options = listOf("Todas") + classOptions,
                    onValueChange = onClassChange
                )
            }
        }
    }
}
