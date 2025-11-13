package com.jotadev.aiapaec.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jotadev.aiapaec.navigation.NavigationRoutes
import com.jotadev.aiapaec.ui.components.WelcomeTopAppBar
import com.jotadev.aiapaec.ui.screens.classes.ClassesViewModel
import com.jotadev.aiapaec.ui.screens.settings.SettingsScreen
import com.jotadev.aiapaec.ui.screens.settings.SettingsViewModel
import com.jotadev.aiapaec.ui.screens.students.StudentsViewModel
import com.jotadev.aiapaec.ui.screens.results.ResultsViewModel
import com.jotadev.aiapaec.ui.screens.results.ExamResult
import com.jotadev.aiapaec.presentation.BimestersViewModel
import kotlinx.coroutines.delay

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
    val institution = settingsUiState.userProfile?.institution ?: "AIAPAEC"
    val studentsVm: StudentsViewModel = viewModel()
    val studentsState by studentsVm.uiState.collectAsState()
    val classesVm: ClassesViewModel = viewModel()
    val classesState by classesVm.uiState.collectAsState()
    val bimestersVm: BimestersViewModel = viewModel()
    val bimestersState by bimestersVm.uiState.collectAsState()
    val resultsVm: ResultsViewModel = viewModel()
    val resultsState by resultsVm.uiState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
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
                Text(
                    text = "Hola, $userName",
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                Text(
                    text = "¿Que deseas hacer hoy?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetricCard(
                        label = "Examenes",
                        value = bimestersState.total.toString(),
                        icon = Icons.Rounded.Assessment,
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Clases",
                        value = classesState.total.toString(),
                        icon = Icons.Rounded.School,
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        accent = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Alumnos",
                        value = studentsState.total.toString(),
                        icon = Icons.Rounded.Groups,
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        accent = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                ExamsPerformanceSection(results = resultsState.results)
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
                                    detectTapGestures(onTap = { showSettings = false })
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
            icon = Icons.AutoMirrored.Rounded.Assignment,
            color = MaterialTheme.colorScheme.primary,
            route = NavigationRoutes.EXAMS
        ),
        CategoryItem(
            title = "Clases",
            subtitle = "Organiza contenidos",
            icon = Icons.Rounded.Groups,
            color = MaterialTheme.colorScheme.secondary,
            route = NavigationRoutes.CLASSES
        ),
        CategoryItem(
            title = "Alumnos",
            subtitle = "Lista y seguimiento",
            icon = Icons.Rounded.School,
            color = MaterialTheme.colorScheme.tertiary,
            route = NavigationRoutes.STUDENTS
        ),
        CategoryItem(
            title = "Resultados",
            subtitle = "Ver reportes",
            icon = Icons.Rounded.Leaderboard,
            color = MaterialTheme.colorScheme.error,
            route = NavigationRoutes.RESULTS
        ),
        CategoryItem(
            title = "Escanear",
            subtitle = "Tarjeta QR",
            icon = Icons.Rounded.QrCodeScanner,
            color = MaterialTheme.colorScheme.primary,
        route = NavigationRoutes.SCAN_UPLOAD
        ),
        CategoryItem(
            title = "Ajustes",
            subtitle = "Preferencias",
            icon = Icons.Rounded.Settings,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            route = NavigationRoutes.SETTINGS,
            isSettingsAction = true
        )
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp, start = 4.dp, end = 4.dp),
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
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(item.color.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(24.dp),
                    tint = item.color
                )
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.labelSmall,
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

@Composable
private fun MetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    accent: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.height(72.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(containerColor)
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(accent.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

@Composable
private fun ExamsPerformanceSection(results: List<ExamResult>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onPrimary, shape = MaterialTheme.shapes.medium)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Rendimiento de Exámenes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary
            )
            AssistChip(onClick = {}, label = { Text("Últimos 6 meses") })
        }
        Spacer(modifier = Modifier.height(8.dp))
        val (labels, values) = remember(results) { buildMonthlyAverage(results, 6) }
        PerformanceLineChart(
            labels = labels,
            values = values,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(8.dp)
        )
    }
}

@Composable
private fun PerformanceLineChart(
    labels: List<String>,
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val areaGradient = Brush.verticalGradient(
        colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent)
    )
    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val padding = 16f
                val points = values.mapIndexed { i, v ->
                    val x =
                        padding + (w - 2 * padding) * (i.toFloat() / (values.size - 1).coerceAtLeast(
                            1
                        ))
                    val y = h - padding - (h - 2 * padding) * (v / 100f)
                    Offset(x, y)
                }
                if (points.isNotEmpty()) {
                    val path = Path().apply {
                        moveTo(points.first().x, h - padding)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, h - padding)
                        close()
                    }
                    drawPath(path = path, brush = areaGradient)
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = lineColor,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 4f
                        )
                    }
                    points.forEach { drawCircle(color = lineColor, radius = 5f, center = it) }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun buildMonthlyAverage(
    results: List<ExamResult>,
    months: Int
): Pair<List<String>, List<Float>> {
    val monthNames =
        listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    val byMonth = results.groupBy { it.date.substring(5, 7).toInt() }
    val monthsSorted = byMonth.keys.sorted().takeLast(months)
    val labels = monthsSorted.map { monthNames[(it - 1).coerceIn(0, 11)] }
    val values = monthsSorted.map { m ->
        val scores = byMonth[m]?.map { it.score } ?: emptyList()
        if (scores.isEmpty()) 0f else scores.average().toFloat()
    }
    return labels to values
}
