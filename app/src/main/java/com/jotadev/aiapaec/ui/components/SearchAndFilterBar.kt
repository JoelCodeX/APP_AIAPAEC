package com.jotadev.aiapaec.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFilterBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    selectedBimester: String,
    onBimesterChange: (String) -> Unit,
    selectedClass: String,
    onClassChange: (String) -> Unit,
    bimesters: List<String> = listOf(
        "Todos",
        "I Bimestre",
        "II Bimestre",
        "III Bimestre",
        "IV Bimestre"
    ),
    classes: List<String> = listOf(
        "Todas",
        "Matemáticas",
        "Comunicación",
        "Ciencias",
        "Personal Social"
    )
) {
    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // BARRA DE BUSQUEDA Y FILTRO - DEBEN ESTAR EN EL MISMO ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // CAMPO DE BUSQUEDA
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier.weight(1f), // <- AQUÍ USAMOS weight
                placeholder = { Text("Buscar exámenes...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )

            IconButton(
                onClick = { showFilters = !showFilters },
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondary)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtros",
                    tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // FILTROS EXPANDIBLES
        AnimatedVisibility(visible = showFilters) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // FILTRO BIMESTRE
                    FilterDropdown(
                        modifier = Modifier.weight(1f),
                        label = "Bimestre",
                        selectedValue = selectedBimester,
                        options = bimesters,
                        onValueChange = onBimesterChange
                    )

                    // FILTRO CLASE
                    FilterDropdown(
                        modifier = Modifier.weight(1f),
                        label = "Clase",
                        selectedValue = selectedClass,
                        options = classes,
                        onValueChange = onClassChange
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    modifier: Modifier = Modifier,
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        if (enabled) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}