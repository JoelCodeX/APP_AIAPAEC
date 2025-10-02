package com.jotadev.aiapaec.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
// import androidx.compose.ui.draw.blur
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jotadev.aiapaec.ui.components.WelcomeTopAppBar
import com.jotadev.aiapaec.ui.screens.settings.SettingsScreen
import com.jotadev.aiapaec.ui.screens.settings.SettingsViewModel
import com.jotadev.aiapaec.navigation.NavigationRoutes
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val settingsViewModel: SettingsViewModel = viewModel()
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val userName = settingsUiState.userProfile?.name ?: "Usuario"
    var showSettings by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.onPrimary,
            topBar = {
                WelcomeTopAppBar(onNavigationClick = { showSettings = true })
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Cabecera con saludo y acciones rápidas
                Text(
                    text = "Hola, $userName",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                Text(
                    text = "¿Que deseas hacer hoy?",
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Estadísticas rápidas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Exámenes",
                        value = uiState.quickActions.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Actividades",
                        value = uiState.recentActivities.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Acciones Principales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                // Cuadrícula de categorías con nuestros items
                CategoryGridSection(
                    onNavigate = { route -> navController.navigate(route) },
                    onOpenSettings = { showSettings = true }
                )
            }
        }

        var renderPopup by remember { mutableStateOf(false) }
        LaunchedEffect(showSettings) {
            if (showSettings) {
                renderPopup = true
            } else {
                delay(200)
                renderPopup = false
            }
        }

        if (renderPopup) {
            Popup(
                alignment = Alignment.TopStart,
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = false,
                    clippingEnabled = false
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Overlay de fondo a pantalla completa (estable y con fade aparte)
                    AnimatedVisibility(
                        visible = renderPopup,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                                .pointerInput(Unit) {
                                    // Consumir toques para bloquear interacción sin cerrar
                                    detectTapGestures(onTap = { /* no-op */ })
                                }
                        )
                    }

                    // Panel animado sobre el overlay
                    AnimatedVisibility(
                        visible = showSettings,
                        enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                        modifier = Modifier.zIndex(1f)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.8f)
                        ) {
                            SettingsScreen(
                                navController = navController,
                                onClose = { showSettings = false })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryGridSection(
    onNavigate: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val categories = listOf(
        CategoryItem(
            title = "Exámenes",
            subtitle = "Crear o gestionar",
            icon = Icons.AutoMirrored.Filled.Assignment,
            color = MaterialTheme.colorScheme.primary,
            route = NavigationRoutes.EXAMS
        ),
        CategoryItem(
            title = "Clases",
            subtitle = "Organiza contenidos",
            icon = Icons.Filled.Groups,
            color = MaterialTheme.colorScheme.secondary,
            route = NavigationRoutes.CLASSES
        ),
        CategoryItem(
            title = "Alumnos",
            subtitle = "Lista y seguimiento",
            icon = Icons.Filled.School,
            color = MaterialTheme.colorScheme.tertiary,
            route = NavigationRoutes.STUDENTS
        ),
        CategoryItem(
            title = "Resultados",
            subtitle = "Ver reportes",
            icon = Icons.Filled.Leaderboard,
            color = MaterialTheme.colorScheme.error,
            route = NavigationRoutes.RESULTS
        ),
        CategoryItem(
            title = "Escanear",
            subtitle = "Tarjeta QR",
            icon = Icons.Filled.QrCodeScanner,
            color = MaterialTheme.colorScheme.primary,
            route = NavigationRoutes.SCAN_CARD
        ),
        CategoryItem(
            title = "Ajustes",
            subtitle = "Preferencias",
            icon = Icons.Filled.Settings,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            route = NavigationRoutes.SETTINGS,
            isSettingsAction = true
        )
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { item ->
            CategoryCard(item = item, onNavigate = onNavigate, onOpenSettings = onOpenSettings)
        }
    }
}

@Composable
private fun CategoryCard(
    item: CategoryItem,
    onNavigate: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            if (item.isSettingsAction) onOpenSettings() else onNavigate(item.route)
        },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(item.color.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ){
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(36.dp),
                    tint = item.color
                )
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class CategoryItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String,
    val isSettingsAction: Boolean = false
)

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier
            .height(88.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}