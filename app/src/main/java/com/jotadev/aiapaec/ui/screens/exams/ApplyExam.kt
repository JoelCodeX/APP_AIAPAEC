package com.jotadev.aiapaec.ui.screens.exams

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jotadev.aiapaec.domain.models.Student
import com.jotadev.aiapaec.navigation.NavigationRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyExam(navController: NavController, examId: String) {
    val vm: ApplyExamViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(examId) {
        vm.load(examId)
    }

    // Lanzador para seleccionar PDF del solucionario
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        selectedPdfUri = uri
        if (uri != null) {
            // Conservar permiso de lectura para futuros usos
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { /* Ignorar si no aplica */ }
            // Subir automáticamente el PDF seleccionado
            if (state.quiz != null) {
                vm.uploadAnswerKeyFromUri(state.quiz!!.id, uri, context.contentResolver)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.quiz?.title ?: "Aplicar evaluación",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // INFO DE LA CARTILLA
            item {
                ExamInfoCard(
                    bimester = state.quiz?.bimesterName ?: "—",
                    className = state.quiz?.className ?: "—",
                    date = formatDate(state.quiz?.createdAt),
                    studentCount = state.students.size,
                    hasKey = state.quiz?.answerKeyFile != null,
                    numQuestions = state.quiz?.numQuestions
                )
            }

            // BOTONES DE ACCIÓN
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElevatedButton(
                        onClick = {
                            // Abrir el explorador de archivos para seleccionar un PDF
                            if (state.quiz == null) return@ElevatedButton
                            if (selectedPdfUri == null) {
                                pdfPickerLauncher.launch(arrayOf("application/pdf"))
                            } else {
                                // Intentar subir el PDF ya seleccionado
                                vm.uploadAnswerKeyFromUri(state.quiz!!.id, selectedPdfUri!!, context.contentResolver)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Subir solucionario", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { navController.navigate(NavigationRoutes.quizAnswers(examId)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver respuestas", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // GRAFICO DE BARRAS DE RENDIMIENTO
            item {
                PerformanceBarChart(
                    title = "Rendimiento de estudiantes",
                    bars = state.performanceBars
                )
            }
            // LISTADO DE ESTUDIANTES Y ESTADO DE CORRECCIÓN
            item {
                Text(
                    text = "Estudiantes (${state.students.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            items(state.students) { student ->
                StudentStatusRow(
                    student = student,
                    status = state.studentStatuses[student.id] ?: "Por corregir",
    onScanClick = { navController.navigate(NavigationRoutes.SCAN_UPLOAD) }
                )
            }

            if (state.isLoading) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }
            if (state.errorMessage != null) {
                item {
                    Text(
                        text = state.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamInfoCard(
    bimester: String,
    className: String,
    date: String,
    studentCount: Int,
    hasKey: Boolean,
    numQuestions: Int?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onPrimary)
            .padding(8.dp)
    ) {
        Text(
            text = "Detalles de la evaluación",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            InfoChip(label = "Bimestre", value = bimester)
            InfoChip(label = "Clase", value = className)
            InfoChip(label = "Fecha", value = date)

        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusPill(
                label = if (hasKey) "Con solucionario" else "Sin solucionario",
                color = if (hasKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            StatusPill(
                label = numQuestions?.let { "$it preguntas" } ?: "Sin asignar",
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.onPrimary)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Light)
    }
}

@Composable
private fun StatusPill(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StudentStatusRow(student: Student, status: String, onScanClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${student.firstName} ${student.lastName}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            val cls = student.className ?: "—"
            Text(text = cls, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        val isCorrected = status.equals("Corregido", ignoreCase = true)
        val icon = if (isCorrected) Icons.Default.CheckCircle else Icons.Default.Cancel
        val tint = if (isCorrected) Color(0xFF2E7D32) else Color(0xFFC62828)
        // Zona derecha: estado + botón de escaneo si no está corregido
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = if (isCorrected) "Corregido" else "Por corregir",
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            if (!isCorrected) {
                OutlinedButton(
                    onClick = onScanClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Escanear",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Escanear")
                }
            }
        }
    }
}

private fun formatDate(value: String?): String {
    if (value.isNullOrBlank()) return "—"
    // Tomar solo la parte de fecha si viene con hora
    val datePart = value.substringBefore('T').substringBefore(' ')
    // Si ya viene en formato dd/MM/yyyy, devolver tal cual
    if (datePart.contains('/')) return datePart
    // Intentar reordenar YYYY-MM-DD a DD/MM/YYYY
    val parts = datePart.split('-')
    return if (parts.size >= 3) {
        val y = parts[0]
        val m = parts[1].padStart(2, '0')
        val d = parts[2].padStart(2, '0')
        "$d/$m/$y"
    } else datePart
}