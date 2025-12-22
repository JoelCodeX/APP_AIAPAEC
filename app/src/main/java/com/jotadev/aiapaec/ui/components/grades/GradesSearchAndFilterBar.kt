package com.jotadev.aiapaec.ui.components.grades

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jotadev.aiapaec.ui.components.FilterDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesSearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedLevel: String,
    onLevelChange: (String) -> Unit,
    selectedGrade: String,
    onGradeChange: (String) -> Unit,
    gradeOptions: List<String>,
    modifier: Modifier = Modifier
) {
    var showFilters by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

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
                modifier = Modifier
                    .weight(1f)
                    .height(if (isSmallScreen) 48.dp else 56.dp), // <- AQUÍ USAMOS weight
                placeholder = { 
                    Text(
                        "Buscar grados disponibles...",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isSmallScreen) 10.sp else 16.sp)
                    ) 
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = if (isSmallScreen) 11.sp else 16.sp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )

            // BOTON DE FILTRO - MOVER DENTRO DEL MISMO ROW
            IconButton(
                onClick = { showFilters = !showFilters },
                modifier = Modifier
                    .size(if (isSmallScreen) 48.dp else 56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondary)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtros",
                    tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp)
                )
            }
        }
        // FILTROS EXPANDIBLES
        AnimatedVisibility(visible = showFilters) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // FILTRO NIVEL EDUCATIVO
                LevelFilterDropdown(
                    selectedLevel = selectedLevel,
                    onLevelChange = onLevelChange,
                    modifier = Modifier.weight(1f)
                )

                // FILTRO GRADO
                FilterDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Grado",
                    selectedValue = selectedGrade,
                    options = gradeOptions,
                    onValueChange = onGradeChange
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LevelFilterDropdown(
    selectedLevel: String,
    onLevelChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val levelOptions = listOf("Todos los niveles", "Primaria", "Secundaria")
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedLevel,
            onValueChange = { },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            label = { 
                Text(
                    "Nivel educativo",
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isSmallScreen) 10.sp else 14.sp)
                ) 
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = if (isSmallScreen) 12.sp else 16.sp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSecondary
            ),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.onPrimary)
        ) {
            levelOptions.forEach { level ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            level, 
                            color = MaterialTheme.colorScheme.onSecondary,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = if (isSmallScreen) 12.sp else 16.sp),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        ) 
                    },
                    onClick = {
                        onLevelChange(level)
                        expanded = false
                    }
                )
            }
        }
    }
}
