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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    modifier: Modifier = Modifier
) {
    var isFilterExpanded by remember { mutableStateOf(false) }
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
                    .height(if (isSmallScreen) 48.dp else 56.dp),
                placeholder = { 
                    Text(
                        "Buscar por nombre o ID...",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isSmallScreen) 10.sp else 16.sp)
                    ) 
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = if (isSmallScreen) 11.sp else 16.sp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp)
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
                    .size(if (isSmallScreen) 48.dp else 56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondary)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtros",
                    tint = if (isFilterExpanded) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // FILTROS EXPANDIBLES
        AnimatedVisibility(visible = isFilterExpanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // FILTRO POR GRADO
                FilterDropdown(
                    label = "Grado",
                    selectedValue = selectedGrade ?: "Todos",
                    options = listOf("Todos") + gradeOptions,
                    onValueChange = { v -> onGradeChange(if (v == "Todos") null else v) },
                    placeholder = if (isMetaLoading) "Cargando grados..." else "Selecciona grado",
                    modifier = Modifier.weight(1f)
                )

                // FILTRO POR SECCIÓN
                FilterDropdown(
                    label = "Sección",
                    selectedValue = selectedSection ?: "Todas",
                    options = listOf("Todas") + sectionOptions,
                    onValueChange = { v -> onSectionChange(if (v == "Todas") null else v) },
                    placeholder = if (isMetaLoading) "Cargando secciones..." else "Selecciona sección",
                    enabled = sectionOptions.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
