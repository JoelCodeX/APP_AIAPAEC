package com.jotadev.aiapaec.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.widthIn
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jotadev.aiapaec.navigation.NavigationRoutes
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.PostAdd
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jotadev.aiapaec.ui.screens.exams.answers.ExamResult
import com.jotadev.aiapaec.ui.screens.settings.SettingsViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.jotadev.aiapaec.data.storage.TokenStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onOpenSettings: () -> Unit,
    onSessionExpired: () -> Unit
) {
    val settingsViewModel: SettingsViewModel = viewModel()
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val userName = settingsUiState.userProfile?.name ?: "Usuario"
    val branchName = settingsUiState.userProfile?.branchName ?: "Sede"
    
    val homeViewModel: HomeViewModel = viewModel()
    val homeState by homeViewModel.uiState.collectAsState()

    // --- SESSION & RESUME HANDLING ---
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Handle Session Expiry (Redirect to Login)
    LaunchedEffect(homeState.isSessionExpired) {
        if (homeState.isSessionExpired) {
            TokenStorage.clear()
            // UserStorage.clear() // Optional but recommended
            onSessionExpired()
        }
    }

    // 2. Refresh Data on Resume (Fix for "0 0 0" when returning to app)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                homeViewModel.refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // ---------------------------------

    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

    val categories = listOf(
        CategoryItem(
            title = "Admisión",
            subtitle = "Gestiona ingresos",
            icon = Icons.AutoMirrored.Rounded.Assignment,
            color = MaterialTheme.colorScheme.primary,
            route = NavigationRoutes.EXAMS_FULL
        ),
        CategoryItem(
            title = "Grados",
            subtitle = "Seguimiento académico",
            icon = Icons.Rounded.Groups,
            color = MaterialTheme.colorScheme.secondary,
            route = NavigationRoutes.GRADES_FULL
        ),
        CategoryItem(
            title = "Alumnos",
            subtitle = "Lista y seguimiento",
            icon = Icons.Rounded.School,
            color = MaterialTheme.colorScheme.tertiary,
            route = NavigationRoutes.STUDENTS_FULL
        ),
        CategoryItem(
            title = "Formatos",
            subtitle = "Asignaciones",
            icon = Icons.Rounded.Leaderboard,
            color = MaterialTheme.colorScheme.error,
            route = NavigationRoutes.FORMATS_FULL
        ),
        CategoryItem(
            title = "Nuevo Formato",
            subtitle = "Crear asignación",
            icon = Icons.Rounded.PostAdd,
            color = MaterialTheme.colorScheme.secondary,
            route = "${NavigationRoutes.FORMATS_FULL}?openDialog=true"
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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
        ) { paddingValues ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 100.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Greeting (Span Full)
            item(span = { GridItemSpan(maxLineSpan) }) {
                GreetingCard(userName = userName, branchName = branchName)
            }

            // 2. Metrics (Span Full)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetricCard(
                        label = "Formatos",
                        value = if (homeState.isLoadingMetrics) "..." else homeState.formatsCount.toString(),
                        icon = Icons.Rounded.Assessment,
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Semanales",
                        value = if (homeState.isLoadingMetrics) "..." else homeState.weekliesCount.toString(),
                        icon = Icons.Rounded.Leaderboard,
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        accent = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Grados",
                        value = if (homeState.isLoadingMetrics) "..." else homeState.gradesCount.toString(),
                        icon = Icons.Rounded.School,
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        accent = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Alumnos",
                        value = if (homeState.isLoadingMetrics) "..." else homeState.studentsCount.toString(),
                        icon = Icons.Rounded.Groups,
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        accent = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Chart (Span Full)
            item(span = { GridItemSpan(maxLineSpan) }) {
                ExamsPerformanceSection(
                    results = homeState.performanceData,
                    selectedRange = homeState.selectedTimeRange,
                    onRangeSelected = homeViewModel::onTimeRangeSelected
                )
            }

            // 4. Title (Span Full)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = if (isSmallScreen) 14.sp else 16.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }

            // 5. Category Items (Span 1)
            items(categories) { item ->
                CategoryCard(
                    item = item,
                    onNavigate = { route -> navController.navigate(route) },
                    onOpenSettings = onOpenSettings
                )
            }
        }
    }
}
}

@Composable
private fun GreetingCard(userName: String, branchName: String) {
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFDC400),
                            Color(0xFFE3B719),
                            Color(0xFFCC9E18)
                        )
                    )
                )
                .padding(if (isSmallScreen) 12.dp else 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "Hola, $userName",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = if (isSmallScreen) 14.sp else 16.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Text(
                        text = "¿Qué deseas hacer hoy?",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isSmallScreen) 12.sp else 14.sp),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    BranchChip(branchName = branchName)
                }
            }
        }
    }
}

@Composable
private fun BranchChip(branchName: String) {
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f))
            .padding(horizontal = if (isSmallScreen) 8.dp else 10.dp, vertical = if (isSmallScreen) 4.dp else 6.dp)
            .widthIn(min = if (isSmallScreen) 80.dp else 96.dp, max = if (isSmallScreen) 120.dp else 140.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Apartment,
            contentDescription = "Sede",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(if (isSmallScreen) 14.dp else 18.dp)
        )
        Spacer(modifier = Modifier.width(if (isSmallScreen) 4.dp else 6.dp))
        Text(
            text = branchName,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = if (isSmallScreen) 10.sp else 12.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CategoryCard(
    item: CategoryItem,
    onNavigate: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            if (item.isSettingsAction) onOpenSettings() else onNavigate(item.route)
        },
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (isSmallScreen) 8.dp else 10.dp),
            verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 2.dp else 4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(if (isSmallScreen) 28.dp else 36.dp)
                    .background(item.color.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(if (isSmallScreen) 18.dp else 24.dp),
                    tint = item.color
                )
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = if (isSmallScreen) 10.sp else 12.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isSmallScreen) 8.sp else 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
private fun MetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

    ElevatedCard(
        modifier = modifier.height(if (isSmallScreen) 60.dp else 72.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(containerColor)
                .padding(if (isSmallScreen) 6.dp else 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(if (isSmallScreen) 18.dp else 22.dp)
                        .background(accent.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = accent,
                        modifier = Modifier.size(if (isSmallScreen) 14.dp else 16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(if (isSmallScreen) 4.dp else 6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isSmallScreen) 10.sp else 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = if (isSmallScreen) 14.sp else 16.sp),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

@Composable
private fun ExamsPerformanceSection(
    results: List<ExamResult>,
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onPrimary, shape = MaterialTheme.shapes.medium)
            .padding(if (isSmallScreen) 8.dp else 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Rendimiento de Exámenes",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = if (isSmallScreen) 14.sp else 16.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary
            )
            Box {
                AssistChip(
                    onClick = { expanded = true },
                    label = { 
                        Text(
                            text = when(selectedRange) {
                                TimeRange.LAST_WEEK -> "Última semana"
                                TimeRange.LAST_BIMESTER -> "Último bimestre"
                                TimeRange.LAST_6_MONTHS -> "Últimos 6 meses"
                            },
                            fontSize = if (isSmallScreen) 10.sp else 12.sp
                        ) 
                    },
                    trailingIcon = {
                        Icon(Icons.Rounded.DateRange, contentDescription = null, modifier = Modifier.size(if (isSmallScreen) 14.dp else 16.dp))
                    },
                    modifier = Modifier.height(if (isSmallScreen) 24.dp else 32.dp)
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Última semana") },
                        onClick = { 
                            onRangeSelected(TimeRange.LAST_WEEK)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Último bimestre") },
                        onClick = { 
                            onRangeSelected(TimeRange.LAST_BIMESTER)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Últimos 6 meses") },
                        onClick = { 
                            onRangeSelected(TimeRange.LAST_6_MONTHS)
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(if (isSmallScreen) 4.dp else 8.dp))
        val (labels, values) = remember(results, selectedRange) { buildChartData(results, selectedRange) }
        PerformanceLineChart(
            labels = labels,
            values = values,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isSmallScreen) 140.dp else 180.dp)
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
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

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
                
                if (values.isEmpty()) return@Canvas

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
        Spacer(modifier = Modifier.height(if (isSmallScreen) 4.dp else 6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Show only first, middle and last label to avoid overlap if too many
            if (labels.size > 5) {
                Text(text = labels.firstOrNull() ?: "", style = MaterialTheme.typography.bodySmall.copy(fontSize = if (isSmallScreen) 10.sp else 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = labels.getOrNull(labels.size / 2) ?: "", style = MaterialTheme.typography.bodySmall.copy(fontSize = if (isSmallScreen) 10.sp else 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = labels.lastOrNull() ?: "", style = MaterialTheme.typography.bodySmall.copy(fontSize = if (isSmallScreen) 10.sp else 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = if (isSmallScreen) 10.sp else 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun buildChartData(
    results: List<ExamResult>,
    range: TimeRange
): Pair<List<String>, List<Float>> {
    if (results.isEmpty()) return emptyList<String>() to emptyList<Float>()

    return when (range) {
        TimeRange.LAST_WEEK -> {
            // Group by Day Name (Mon, Tue...)
            val byDay = results.groupBy { 
                try {
                    LocalDate.parse(it.date).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "ES"))
                } catch (e: Exception) { "?" }
            }
            // Sort by original date order ideally, but here we just take the results order assuming they are sorted by date from VM
            // To ensure correct order, we should re-sort keys based on the first occurrence in the sorted results
            val sortedKeys = byDay.keys.sortedBy { key -> 
                results.indexOfFirst { 
                    try {
                        LocalDate.parse(it.date).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "ES")) == key
                    } catch (e: Exception) { false }
                } 
            }
            
            val labels = sortedKeys
            val values = sortedKeys.map { day ->
                val scores = byDay[day]?.map { it.score } ?: emptyList()
                if (scores.isEmpty()) 0f else scores.average().toFloat()
            }
            labels to values
        }
        TimeRange.LAST_BIMESTER -> {
            // Group by Week? Or just raw dates if few. Let's group by "Sem X"
            // For simplicity, let's group by Date (Day-Month)
             val byDate = results.groupBy { 
                try {
                    val d = LocalDate.parse(it.date)
                    "${d.dayOfMonth}/${d.monthValue}"
                } catch (e: Exception) { "?" }
            }
            val labels = byDate.keys.toList()
            val values = labels.map { d ->
                 val scores = byDate[d]?.map { it.score } ?: emptyList()
                 if (scores.isEmpty()) 0f else scores.average().toFloat()
            }
            labels to values
        }
        TimeRange.LAST_6_MONTHS -> {
            val monthNames =
                listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
            val byMonth = results.groupBy { it.date.substring(5, 7).toIntOrNull() ?: 1 }
            val monthsSorted = byMonth.keys.sorted()
            val labels = monthsSorted.map { monthNames[(it - 1).coerceIn(0, 11)] }
            val values = monthsSorted.map { m ->
                val scores = byMonth[m]?.map { it.score } ?: emptyList()
                if (scores.isEmpty()) 0f else scores.average().toFloat()
            }
            labels to values
        }
    }
}
