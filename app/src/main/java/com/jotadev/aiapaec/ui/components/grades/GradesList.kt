package com.jotadev.aiapaec.ui.components.grades

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jotadev.aiapaec.data.api.SectionDto
import com.jotadev.aiapaec.domain.models.Grade

@Composable
fun GradesList(
    grades: List<Grade>,
    onSectionClick: (Grade, SectionDto) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado para controlar qué tarjeta está expandida
    var expandedGradeId by remember { mutableStateOf<Int?>(null) }

    if (grades.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Sin grados",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No hay grados disponibles",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Los grados aparecerán aquí cuando estén disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(grades) { grade ->
            GradeCard(
                grade = grade,
                isExpanded = expandedGradeId == grade.id,
                onCardClick = {
                    // Si ya está expandida, la contraemos
                    // Si no está expandida, expandimos esta y cerramos cualquier otra
                    expandedGradeId = if (expandedGradeId == grade.id) {
                        null
                    } else {
                        grade.id
                    }
                },
                onSectionClick = { section -> onSectionClick(grade, section) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
