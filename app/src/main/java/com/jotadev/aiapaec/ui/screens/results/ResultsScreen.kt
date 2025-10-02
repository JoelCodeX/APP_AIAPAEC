package com.jotadev.aiapaec.ui.screens.results

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jotadev.aiapaec.ui.components.FilterDropdown
import com.jotadev.aiapaec.ui.components.ScreenTopAppBar
import java.io.File
import java.io.FileOutputStream
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(navController: NavController) {
    val context = LocalContext.current

    // Datos ficticios
    val campuses = remember { listOf("Sede Norte", "Sede Sur") }
    val classesByCampus = remember {
        mapOf(
            "Sede Norte" to listOf("Clase A", "Clase B"),
            "Sede Sur" to listOf("Clase C")
        )
    }
    val studentsByClass = remember {
        mapOf(
            "Clase A" to listOf("Ana", "Luis", "Carla"),
            "Clase B" to listOf("Miguel", "Sara"),
            "Clase C" to listOf("Pedro", "Lucía")
        )
    }

    val periods = remember { listOf("Última semana", "Último mes", "Trimestre", "Año") }

    var selectedCampus by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("") }
    var selectedStudent by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(periods[1]) }

    // Resultados y datos de rendimiento ficticios
    val sampleResults = remember { mutableStateListOf<ResultItem>() }
    val performanceSeries by remember(selectedCampus, selectedClass, selectedStudent, selectedPeriod) {
        mutableStateOf(generatePerformanceSeries(selectedCampus, selectedClass, selectedStudent, selectedPeriod))
    }

    // Poblar resultados según selección (datos ficticios)
    LaunchedEffect(selectedCampus, selectedClass, selectedStudent, selectedPeriod) {
        sampleResults.clear()
        val scopeName = when {
            selectedStudent.isNotEmpty() -> selectedStudent
            selectedClass.isNotEmpty() -> selectedClass
            selectedCampus.isNotEmpty() -> selectedCampus
            else -> "General"
        }
        sampleResults.addAll(fakeResults(scopeName))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.onPrimary,
        topBar = {
            ScreenTopAppBar(
                screenTitle = "Resultados",
                subtitle = "Consulta tus resultados y estadísticas"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Filtros avanzados
            Text(text = "Filtros", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                FilterDropdown(
                    label = "Sede",
                    selectedValue = selectedCampus,
                    options = campuses,
                    onValueChange = {
                        selectedCampus = it
                        selectedClass = ""
                        selectedStudent = ""
                    },
                    modifier = Modifier.weight(1f)
                )
                FilterDropdown(
                    label = "Clase",
                    selectedValue = selectedClass,
                    options = classesByCampus[selectedCampus] ?: emptyList(),
                    onValueChange = {
                        selectedClass = it
                        selectedStudent = ""
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                FilterDropdown(
                    label = "Alumno",
                    selectedValue = selectedStudent,
                    options = studentsByClass[selectedClass] ?: emptyList(),
                    onValueChange = { selectedStudent = it },
                    modifier = Modifier.weight(1f)
                )
                FilterDropdown(
                    label = "Período",
                    selectedValue = selectedPeriod,
                    options = periods,
                    onValueChange = { selectedPeriod = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Acciones
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Gráfico de rendimiento", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = {
                    exportResultsToPdf(
                        context = context,
                        scopeTitle = when {
                            selectedStudent.isNotEmpty() -> "Alumno: $selectedStudent"
                            selectedClass.isNotEmpty() -> "Clase: $selectedClass"
                            selectedCampus.isNotEmpty() -> "Sede: $selectedCampus"
                            else -> "General"
                        },
                        period = selectedPeriod,
                        results = sampleResults.toList(),
                        performanceSeries = performanceSeries
                    )
                }) { Text("Exportar PDF") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gráfico de rendimiento
            PerformanceChart(
                data = performanceSeries,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Resumen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Procesados",
                    value = sampleResults.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Promedio",
                    value = sampleResults.map { it.score }.average().let { if (it.isNaN()) "-" else it.toInt().toString() },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Listas dinámicas según filtro
            when {
                selectedStudent.isNotEmpty() -> {
                    Text(text = "Resultados de $selectedStudent", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(sampleResults) { item -> ResultListItem(item) }
                    }
                }
                selectedClass.isNotEmpty() -> {
                    Text(text = "Alumnos de ${selectedClass}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(studentsByClass[selectedClass] ?: emptyList()) { student ->
                            ElevatedCard {
                                // Si la versión de Material3 no soporta onClick en ElevatedCard, usamos Modifier.clickable
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = student, style = MaterialTheme.typography.titleMedium)
                                    TextButton(onClick = { selectedStudent = student }) { Text("Ver resultados") }
                                }
                            }
                        }
                    }
                }
                selectedCampus.isNotEmpty() -> {
                    Text(text = "Clases de ${selectedCampus}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(classesByCampus[selectedCampus] ?: emptyList()) { clazz ->
                            ElevatedCard {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = clazz, style = MaterialTheme.typography.titleMedium)
                                    TextButton(onClick = { selectedClass = clazz }) { Text("Ver alumnos") }
                                }
                            }
                        }
                    }
                }
                else -> {
                    Text(text = "Listado general", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(sampleResults) { item -> ResultListItem(item) }
                    }
                }
            }
        }
    }
}

// --- Components ---

data class ResultItem(val title: String, val context: String, val score: Int)

// --- Chart ---
@Composable
private fun PerformanceChart(data: List<Float>, modifier: Modifier = Modifier) {
    // Capturar colores en contexto composable (no dentro de Canvas)
    val outlineColor = MaterialTheme.colorScheme.outline
    val primaryColor = MaterialTheme.colorScheme.primary

    androidx.compose.foundation.Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        val padding = 24f
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        val maxVal = (data.maxOrNull() ?: 100f).coerceAtLeast(1f)
        val minVal = (data.minOrNull() ?: 0f)
        val range = (maxVal - minVal).coerceAtLeast(1f)
        val stepX = chartWidth / (data.size - 1)

        // Axes
        drawLine(
            color = outlineColor,
            start = Offset(padding, size.height - padding),
            end = Offset(size.width - padding, size.height - padding),
            strokeWidth = 3f
        )
        drawLine(
            color = outlineColor,
            start = Offset(padding, size.height - padding),
            end = Offset(padding, padding),
            strokeWidth = 3f
        )

        // Path
        val path = Path()
        data.forEachIndexed { i, v ->
            val x = padding + stepX * i
            val y = padding + chartHeight * (1f - (v - minVal) / range)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // Points
        data.forEachIndexed { i, v ->
            val x = padding + stepX * i
            val y = padding + chartHeight * (1f - (v - minVal) / range)
            drawCircle(color = primaryColor, radius = 8f, center = Offset(x, y))
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
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
private fun ResultListItem(item: ResultItem) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.context,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            AssistChip(onClick = {}, label = { Text("Puntaje: ${item.score}") })
        }
    }
}

// --- Helpers & PDF ---
private fun generatePerformanceSeries(campus: String, clazz: String, student: String, period: String): List<Float> {
    val base = when {
        student.isNotEmpty() -> 75
        clazz.isNotEmpty() -> 70
        campus.isNotEmpty() -> 65
        else -> 60
    }
    return when (period) {
        "Última semana" -> listOf(base.toFloat(), (base + 2).toFloat(), (base + 4).toFloat(), (base + 6).toFloat())
        "Trimestre" -> listOf(62f, 68f, 71f, 75f, 78f, 80f)
        "Año" -> listOf(58f, 60f, 63f, 67f, 70f, 74f, 77f, 79f, 82f, 85f)
        else -> listOf(60f, 65f, 70f, 72f, 75f)
    }
}

private fun fakeResults(scopeName: String): List<ResultItem> = listOf(
    ResultItem("Examen Matemáticas", scopeName, 85),
    ResultItem("Examen Historia", scopeName, 72),
    ResultItem("Examen Ciencias", scopeName, 91)
)

private fun exportResultsToPdf(
    context: android.content.Context,
    scopeTitle: String,
    period: String,
    results: List<ResultItem>,
    performanceSeries: List<Float>
) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 aprox en puntos
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 18f
            color = AndroidColor.BLACK
        }
        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = 12f
            color = AndroidColor.DKGRAY
        }

        var y = 40f
        canvas.drawText("Resultados - $scopeTitle", 40f, y, titlePaint)
        y += 24f
        canvas.drawText("Período: $period", 40f, y, textPaint)
        y += 28f

        // Pequeño gráfico de barras en PDF
        val maxVal = (performanceSeries.maxOrNull() ?: 100f).coerceAtLeast(1f)
        val barWidth = 20f
        val gap = 12f
        var x = 40f
        performanceSeries.forEach { v ->
            val h = 100f * (v / maxVal)
            canvas.drawRect(x, y + 100f - h, x + barWidth, y + 100f, Paint().apply { color = AndroidColor.BLUE })
            x += barWidth + gap
        }
        y += 140f

        canvas.drawText("Listado", 40f, y, titlePaint)
        y += 22f
        results.forEach { r ->
            canvas.drawText("- ${r.title} | ${r.context} | Puntaje: ${r.score}", 40f, y, textPaint)
            y += 18f
        }

        pdfDocument.finishPage(page)

        val fileName = "resultados_${System.currentTimeMillis()}.pdf"
        val dest = File(context.getExternalFilesDir(null), fileName)
        pdfDocument.writeTo(FileOutputStream(dest))
        pdfDocument.close()

        Toast.makeText(context, "PDF guardado en: ${dest.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al exportar PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}