package com.jotadev.aiapaec.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ClassesList(
    classes: List<ClassInfo>,
    modifier: Modifier = Modifier
) {
    if (classes.isEmpty()) {
        // ESTADO VACIO
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Sin clases",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No hay clases disponibles",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Las clases aparecerán aquí cuando estén disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    } else {
        // LISTA DE CLASES
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(classes) { classInfo ->
                ClassCard(classInfo = classInfo)
            }
        }
    }
}