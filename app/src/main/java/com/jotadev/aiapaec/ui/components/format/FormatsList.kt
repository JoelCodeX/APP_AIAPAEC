package com.jotadev.aiapaec.ui.components.format

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jotadev.aiapaec.ui.screens.format.FormatItem

@Composable
fun FormatsList(
    items: List<FormatItem>,
    onEdit: (FormatItem) -> Unit,
    onDelete: (FormatItem) -> Unit,
    onClick: (FormatItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val expandedFormatId = remember { mutableStateOf<String?>(null) }
    val sortedItems = items.sortedBy { it.id.toIntOrNull() ?: Int.MAX_VALUE }
    if (items.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Leaderboard, contentDescription = "Sin Formatos", tint = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "No hay formatos creados", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
                Text(text = "Presiona el botón + para crear un formato", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedItems) { item ->
                FormatCard(
                    item = item,
                    onEditClick = onEdit,
                    onDeleteClick = onDelete,
                    onClick = onClick,
                    isExpanded = expandedFormatId.value == item.id,
                    onToggleExpand = {
                        expandedFormatId.value =
                            if (expandedFormatId.value == item.id) null else item.id
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
