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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jotadev.aiapaec.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.clickable

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onClose: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val settingsViewModel: SettingsViewModel = viewModel()
    val uiState = settingsViewModel.uiState
    val state = uiState.value
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro que deseas salir de la aplicación?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        settingsViewModel.performAction("logout", onLogoutSuccess = onLogout)
                    }
                ) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Encabezado con color terciario, avatar y botón cerrar
        Surface(
            modifier = Modifier
                .height(if (isSmallScreen) 110.dp else 150.dp),
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
                            .size(if (isSmallScreen) 40.dp else 52.dp)
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
                    Spacer(modifier = Modifier.width(if (isSmallScreen) 8.dp else 12.dp))
                    Column {
                        Text(
                            text = state.userProfile?.name ?: "Usuario",
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = if (isSmallScreen) 16.sp else 20.sp)
                        )
                        Text(
                            text = state.userProfile?.institution ?: "AIAPAEC",
                            color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isSmallScreen) 12.sp else 14.sp)
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
        
        val headlineStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = if (isSmallScreen) 14.sp else 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        val supportingStyle = MaterialTheme.typography.bodyMedium.copy(
            fontSize = if (isSmallScreen) 10.sp else 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            item {
                SectionTitle("Preferencias")
                ListItem(
                    headlineContent = { Text("Tema oscuro", style = headlineStyle) },
                    supportingContent = { 
                        Text(
                            "Activa el tema oscuro para reducir fatiga visual", 
                            style = supportingStyle
                        ) 
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp)
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = darkMode, 
                            onCheckedChange = { darkMode = it },
                            modifier = Modifier.scale(if (isSmallScreen) 0.8f else 1f)
                        )
                    }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Idioma", style = headlineStyle) },
                    supportingContent = { Text("Español", style = supportingStyle) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp)
                        )
                    }
                )
            }

            item {
                SectionTitle("Notificaciones")
                ListItem(
                    headlineContent = { Text("Notificaciones", style = headlineStyle) },
                    supportingContent = { 
                        Text(
                            "Recibir alertas y recordatorios", 
                            style = supportingStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (isSmallScreen) 18.dp else 24.dp)
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = notificationsEnabled, 
                            onCheckedChange = { notificationsEnabled = it },
                            modifier = Modifier.scale(if (isSmallScreen) 0.7f else 1f)
                        )
                    }
                )
            }

            item {
                SectionTitle("Acerca de")
                ListItem(
                    headlineContent = { Text("Versión de la app", style = headlineStyle) },
                    supportingContent = { Text("1.0.0", style = supportingStyle) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (isSmallScreen) 18.dp else 24.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SectionTitle("Cuenta")
                ListItem(
                    headlineContent = { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error, style = headlineStyle) },
                    supportingContent = { Text("Salir de la aplicación", style = supportingStyle) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(if (isSmallScreen) 18.dp else 24.dp)
                        )
                    },
                    modifier = Modifier.clickable { showLogoutDialog = true }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360
    
    Text(
        text = title,
        modifier = Modifier
            .padding(top = if (isSmallScreen) 11.dp else 16.dp, bottom = 8.dp, start = 16.dp),
        style = MaterialTheme.typography.titleSmall.copy(fontSize = if (isSmallScreen) 12.sp else 14.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}