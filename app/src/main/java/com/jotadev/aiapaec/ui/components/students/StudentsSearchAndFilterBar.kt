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
    selectedClass: String,
    onClassChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFilterExpanded by remember { mutableStateOf(false) }

    val classOptions = listOf(
        "Todas las clases",
        "1° Primaria",
        "2° Primaria",
        "3° Primaria",
        "4° Primaria",
        "5° Primaria",
        "6° Primaria",
        "1° Secundaria",
        "2° Secundaria",
        "3° Secundaria",
        "4° Secundaria",
        "5° Secundaria"
    )

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
                // FILTRO POR CLASE
            FilterDropdown(
                label = "Clase",
                selectedValue = selectedClass,
                options = classOptions,
                onValueChange = onClassChange
            )
            
        }
    }
}