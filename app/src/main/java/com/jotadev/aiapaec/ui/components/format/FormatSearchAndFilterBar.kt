package com.jotadev.aiapaec.ui.components.format

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jotadev.aiapaec.ui.components.FilterDropdown

@Composable
fun FormatSearchAndFilterBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    selectedGrade: String?,
    onGradeChange: (String?) -> Unit,
    selectedSection: String?,
    onSectionChange: (String?) -> Unit,
    selectedFormatType: String?,
    onFormatTypeChange: (String?) -> Unit,
    selectedScoreFormat: String?,
    onScoreFormatChange: (String?) -> Unit,
    isMetaLoading: Boolean = false,
    gradeOptions: List<String>,
    sectionOptions: List<String>,
    formatTypeOptions: List<String>,
    scoreFormatOptions: List<String>
) {
    var showFilters by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier
                    .weight(1f)
                    .size(width = 0.dp, height = if (isSmallScreen) 48.dp else 56.dp),
                singleLine = true,
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp)
                    ) 
                },
                placeholder = { 
                    Text(
                        "Buscar formatos...",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isSmallScreen) 10.sp else 16.sp)
                    ) 
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = if (isSmallScreen) 11.sp else 16.sp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

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
                    tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp)
                )
            }
        }

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
                    FilterDropdown(
                        modifier = Modifier.weight(1f),
                        label = "Grado",
                        selectedValue = selectedGrade ?: "Todos",
                        options = listOf("Todos") + gradeOptions,
                        onValueChange = { onGradeChange(if (it == "Todos") null else it) },
                        placeholder = if (isMetaLoading) "Cargando…" else if (gradeOptions.isEmpty()) "Sin datos" else null,
                        enabled = !isMetaLoading && gradeOptions.isNotEmpty()
                    )

                    FilterDropdown(
                        modifier = Modifier.weight(1f),
                        label = "Sección",
                        selectedValue = selectedSection ?: "Todas",
                        options = listOf("Todas") + sectionOptions,
                        onValueChange = { onSectionChange(if (it == "Todas") null else it) },
                        placeholder = if (isMetaLoading) "Cargando…" else if ((selectedGrade
                                ?: "").isBlank()
                        ) "Selecciona grado primero" else if (sectionOptions.isEmpty()) "Sin datos" else null,
                        enabled = !isMetaLoading && sectionOptions.isNotEmpty() && !(selectedGrade
                            ?: "").isBlank()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterDropdown(
                        modifier = Modifier.weight(1f),
                        label = "Formato",
                        selectedValue = selectedFormatType ?: "Todos",
                        options = listOf("Todos") + formatTypeOptions,
                        onValueChange = { onFormatTypeChange(if (it == "Todos") null else it) }
                    )

                    FilterDropdown(
                        modifier = Modifier.weight(1f),
                        label = "Puntaje",
                        selectedValue = selectedScoreFormat ?: "Todos",
                        options = listOf("Todos") + scoreFormatOptions,
                        onValueChange = { onScoreFormatChange(if (it == "Todos") null else it) }
                    )
                }
            }
        }
    }
}
