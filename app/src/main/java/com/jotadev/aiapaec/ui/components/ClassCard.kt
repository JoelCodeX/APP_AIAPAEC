package com.jotadev.aiapaec.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ClassInfo(
    val id: String,
    val name: String,
    val level: String, // "Primaria" o "Secundaria"
    val studentCount: Int
)

@Composable
fun ClassCard(
    classInfo: ClassInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ENCABEZADO CON ID Y NIVEL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${classInfo.id}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (classInfo.level == "Primaria") 
                        MaterialTheme.colorScheme.primaryContainer 
                    else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = classInfo.level,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (classInfo.level == "Primaria") 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // NOMBRE DE LA CLASE
            Text(
                text = classInfo.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
            
            // CANTIDAD DE ESTUDIANTES
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Estudiantes",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "${classInfo.studentCount} estudiantes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}