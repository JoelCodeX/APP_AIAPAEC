package com.jotadev.aiapaec.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jotadev.aiapaec.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jotadev.aiapaec.ui.screens.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onClose: () -> Unit = {}
) {
    val settingsViewModel: SettingsViewModel = viewModel()
    val uiState = settingsViewModel.uiState
    val state = uiState.value
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Encabezado con color terciario, avatar y botón cerrar
        Surface(
            modifier = Modifier
                .height(150.dp),
            color = MaterialTheme.colorScheme.tertiary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo AIAPAEC",
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = state.userProfile?.name ?: "Usuario",
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.userProfile?.institution ?: "AIAPAEC",
                            color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.9f)
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        }

        // Lista de opciones de configuración con buenas prácticas
        var darkMode by remember { mutableStateOf(false) }
        var notificationsEnabled by remember { mutableStateOf(true) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            item {
                SectionTitle("Preferencias")
                ListItem(
                    headlineContent = { Text("Tema oscuro") },
                    supportingContent = { Text("Activa el tema oscuro para reducir fatiga visual") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Switch(checked = darkMode, onCheckedChange = { darkMode = it })
                    }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Idioma") },
                    supportingContent = { Text("Español") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }

            item {
                SectionTitle("Notificaciones")
                ListItem(
                    headlineContent = { Text("Notificaciones") },
                    supportingContent = { Text("Recibir alertas y recordatorios") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                    }
                )
            }

            item {
                SectionTitle("Acerca de")
                ListItem(
                    headlineContent = { Text("Versión de la app") },
                    supportingContent = { Text("1.0.0") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .padding(top = 16.dp, bottom = 8.dp, start = 16.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}