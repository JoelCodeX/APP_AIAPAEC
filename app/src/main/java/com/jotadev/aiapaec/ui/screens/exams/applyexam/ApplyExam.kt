package com.jotadev.aiapaec.ui.screens.exams.applyexam

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jotadev.aiapaec.data.api.NetworkConfig
import com.jotadev.aiapaec.domain.models.Student
import com.jotadev.aiapaec.domain.models.StudentStatus
import com.jotadev.aiapaec.navigation.NavigationRoutes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ApplyExam(navController: NavController, examId: String) {
    val vm: ApplyExamViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

    val prevHandle = navController.previousBackStackEntry?.savedStateHandle
    val applyGradeId = prevHandle?.get<Int>("apply_grade_id")
    val applySectionId = prevHandle?.get<Int>("apply_section_id")
    val applyGradeName = prevHandle?.get<String>("apply_grade_name")
    val applySectionName = prevHandle?.get<String>("apply_section_name")
    val context = LocalContext.current

    // Estado para indicador de carga local al escanear
    var scanningStudentId by remember { mutableStateOf<Int?>(null) }

    val pullState = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh = { vm.refreshStudentStatuses() }
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.refreshStudentStatuses()
                scanningStudentId = null
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(examId, applyGradeId, applySectionId) {
        vm.load(examId, applyGradeId, applySectionId)
    }

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage
        if (!msg.isNullOrBlank()) {
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // REDIRIGE SOLO TRAS SUBIDA EXITOSA DEL SOLUCIONARIO
    LaunchedEffect(state.justUploaded, state.answers.size) {
        if (state.justUploaded && state.answers.isNotEmpty()) {
            vm.ackJustUploaded()
            navController.navigate(NavigationRoutes.quizAnswers(examId))
        }
    }

    // Lanzador para seleccionar PDF del solucionario
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        selectedPdfUri = uri
        if (uri != null) {
            // Conservar permiso de lectura para futuros usos
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { /* Ignorar si no aplica */
            }
            if (state.quiz != null) {
                vm.uploadAnswerKeyFromUri(state.quiz!!.id, uri, context.contentResolver)
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage
        if (!msg.isNullOrBlank() && msg.contains("Solucionario no compatible", ignoreCase = true)) {
            selectedPdfUri = null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        var studentSearch by remember { mutableStateOf("") }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isSmallScreen) 10.dp else 16.dp)
                    .imePadding()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 12.dp else 16.dp)
            ) {
                // INFO DE LA CARTILLA
            item {
                val classLabelOverride =
                    listOfNotNull(applyGradeName, applySectionName).joinToString(" ")
                        .ifBlank { null }
                ExamInfoCard(
                    bimester = state.quiz?.bimesterName ?: "—",
                    className = classLabelOverride ?: listOfNotNull(
                        state.quiz?.gradoNombre,
                        state.quiz?.seccionNombre
                    ).joinToString(" ").ifBlank { "—" },
                    date = formatDate(state.quiz?.fecha),
                    studentCount = state.students.size,
                    hasKey = state.hasKey,
                    numQuestions = state.expectedNumQuestions,
                    onActionClick = {
                        state.quiz?.let { quiz ->
                            if (!state.hasKey) {
                                if (selectedPdfUri == null) {
                                    pdfPickerLauncher.launch(arrayOf("application/pdf"))
                                } else {
                                    vm.uploadAnswerKeyFromUri(
                                        quiz.id,
                                        selectedPdfUri!!,
                                        context.contentResolver
                                    )
                                }
                            } else {
                                navController.navigate(NavigationRoutes.quizAnswers(examId))
                            }
                        }
                    },
                    showDistribution = state.showDistribution,
                    onToggleDistributionClick = { vm.toggleDistribution() }
                )
            }

            // AVISO DE SOLUCIONARIO FALTANTE
            if (!state.hasKey) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Para comenzar a escanear, primero debes subir el solucionario.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // GRAFICO DE BARRAS DE RENDIMIENTO
            item {
                if (state.showDistribution) {
                    PerformanceBarChart(
                        title = "Distribución de Puntajes",
                        bars = state.performanceBars
                    )
                }
            }
            // BARRA DE BÚSQUEDA (ESTILO COPIADO) Y LISTADO DE ESTUDIANTES
            val filteredStudents =
                if (studentSearch.isBlank()) state.students else state.students.filter {
                    val t = (it.firstName + " " + it.lastName).lowercase()
                    t.contains(studentSearch.lowercase())
                }
            item {
                Text(
                    text = "Estudiantes (${filteredStudents.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            item {
                OutlinedTextField(
                    value = studentSearch,
                    onValueChange = { studentSearch = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isSmallScreen) 48.dp else 56.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp)
                        )
                    },
                    placeholder = { 
                        Text(
                            "Buscar estudiantes...",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isSmallScreen) 12.sp else 16.sp)
                        ) 
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = if (isSmallScreen) 12.sp else 16.sp),
                    singleLine = true
                )
            }
            items(filteredStudents) { student ->
                val statusObj = state.studentStatuses[student.id]
                StudentStatusRow(
                    student = student,
                    status = statusObj,
                    isScanning = scanningStudentId == student.id,
                    onScanClick = { 
                        if (!state.hasKey) {
                            Toast.makeText(context, "Debes subir el solucionario antes de escanear.", Toast.LENGTH_SHORT).show()
                        } else {
                            scanningStudentId = student.id
                            navController.navigate(NavigationRoutes.scanUpload(examId, student.id, state.expectedNumQuestions ?: 0)) 
                        }
                    },
                    onViewResultClick = { runId ->
                        // Corregido: Agregar /api al path para coincidir con el backend
                        val overlayUrl = "${NetworkConfig.baseRoot}/api/scan/results/overlay/$runId"
                        val encOverlay = Uri.encode(overlayUrl)
                        navController.navigate(
                            NavigationRoutes.scanResult(
                                runId, 
                                encOverlay, 
                                state.expectedNumQuestions ?: 20,
                                examId.toIntOrNull() ?: 0,
                                student.id,
                                readOnly = true
                            )
                        )
                    }
                )
            }

            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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
        
        PullRefreshIndicator(
            refreshing = state.isLoading,
            state = pullState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
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
    numQuestions: Int?,
    onActionClick: () -> Unit,
    showDistribution: Boolean,
    onToggleDistributionClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(color = 0xA4901012),
                        Color(color = 0xE1790F0E)
                    )
                )
            )
            .padding(if (isSmallScreen) 6.dp else 8.dp)
    ) {
        Text(
            text = "Detalles de la evaluación",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = if (isSmallScreen) 14.sp else 16.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(if (isSmallScreen) 4.dp else 6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoChip(label = "Bimestre", value = bimester, icon = Icons.AutoMirrored.Filled.Assignment, isSmallScreen = isSmallScreen)
            InfoChip(label = "Clase", value = className, icon = Icons.Filled.School, isSmallScreen = isSmallScreen)
            InfoChip(label = "Fecha", value = date, icon = Icons.Filled.CalendarToday, isSmallScreen = isSmallScreen)

        }
        Spacer(modifier = Modifier.height(if (isSmallScreen) 4.dp else 6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 4.dp else 8.dp)
        ) {
            StatusPill(
                label = if (hasKey) "Con solucionario" else "Sin solucionario",
                color = MaterialTheme.colorScheme.secondary,
                icon = if (hasKey) Icons.Filled.Key else Icons.Filled.Cancel,
                isSmallScreen = isSmallScreen
            )
            StatusPill(
                label = numQuestions?.let { "$it preguntas" } ?: "Sin asignar",
                color = MaterialTheme.colorScheme.secondary,
                icon = Icons.Filled.QuestionMark,
                isSmallScreen = isSmallScreen
            )
        }
        Spacer(modifier = Modifier.height(if (isSmallScreen) 6.dp else 10.dp))
        val actionText = if (hasKey) "Ver respuestas" else "Subir solucionario"
        val actionIcon = if (hasKey) Icons.AutoMirrored.Filled.Assignment else Icons.Filled.Key
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 4.dp else 8.dp)
        ) {
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                contentPadding = if (isSmallScreen) PaddingValues(horizontal = 8.dp, vertical = 4.dp) else ButtonDefaults.ContentPadding,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    modifier = Modifier.size(if (isSmallScreen) 16.dp else 18.dp)
                )
                Spacer(modifier = Modifier.width(if (isSmallScreen) 4.dp else 8.dp))
                Text(text = actionText, fontWeight = FontWeight.SemiBold, fontSize = if (isSmallScreen) 11.sp else 14.sp)
            }
            Button(
                onClick = onToggleDistributionClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD3CECE),
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                contentPadding = if (isSmallScreen) PaddingValues(horizontal = 8.dp, vertical = 4.dp) else ButtonDefaults.ContentPadding,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.PieChart,
                    contentDescription = null,
                    modifier = Modifier.size(if (isSmallScreen) 16.dp else 18.dp)
                )
                Spacer(modifier = Modifier.width(if (isSmallScreen) 4.dp else 8.dp))
                Text(
                    text =  " % de puntajes",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isSmallScreen) 11.sp else 14.sp
                )
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String, icon: ImageVector, isSmallScreen: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = if (isSmallScreen) 0.08f else 0.12f))
            .padding(horizontal = if (isSmallScreen) 6.dp else 12.dp, vertical = if (isSmallScreen) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(if (isSmallScreen) 14.dp else 16.dp)
        )
        Spacer(modifier = Modifier.width(if (isSmallScreen) 4.dp else 6.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isSmallScreen) 10.sp else 11.sp)
        )
    }
}

@Composable
private fun StatusPill(label: String, color: Color, icon: ImageVector? = null, isSmallScreen: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.3f))
            .padding(horizontal = if (isSmallScreen) 8.dp else 12.dp, vertical = if (isSmallScreen) 4.dp else 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(if (isSmallScreen) 14.dp else 16.dp)
                )
                Spacer(modifier = Modifier.width(if (isSmallScreen) 4.dp else 6.dp))
            }
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isSmallScreen) 10.sp else 11.sp),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
@Composable
private fun StatusPillExam(label: String, color: Color, icon: ImageVector? = null, isSmallScreen: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.3f))
            .padding(horizontal = if (isSmallScreen) 8.dp else 10.dp, vertical = if (isSmallScreen) 4.dp else 6.dp)
            .widthIn(max = if (isSmallScreen) 100.dp else 120.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(if (isSmallScreen) 12.dp else 14.dp)
                )
                Spacer(modifier = Modifier.width(if (isSmallScreen) 4.dp else 6.dp))
            }
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isSmallScreen) 10.sp else 11.sp),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StudentStatusRow(
    student: Student, 
    status: StudentStatus?, 
    isScanning: Boolean = false,
    onScanClick: () -> Unit,
    onViewResultClick: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.onPrimary)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = status?.runId != null
            ) {
                status?.runId?.let { onViewResultClick(it) }
            }
            .padding(if (isSmallScreen) 8.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        // Avatar con iniciales
        val initials =
            (student.firstName.takeIf { it.isNotBlank() }?.firstOrNull()?.toString() ?: "") +
                    (student.lastName.takeIf { it.isNotBlank() }?.firstOrNull()?.toString() ?: "")
        Box(
            modifier = Modifier
                .size(if (isSmallScreen) 28.dp else 36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isSmallScreen) 12.sp else 14.sp),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.width(if (isSmallScreen) 8.dp else 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${student.firstName} ${student.lastName}",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = if (isSmallScreen) 12.sp else 14.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val clsRaw = student.className ?: "—"
            val cls = clsRaw.replace(" - ", " \u00A0- \u00A0")
            Text(
                text = cls,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = if (isSmallScreen) 10.sp else 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        val isCorrected = status?.status?.equals("Corregido", ignoreCase = true) == true
        val icon = if (isCorrected) Icons.Default.CheckCircle else Icons.Default.Cancel
        val tint = if (isCorrected) Color(0xFF2E7D32) else Color(0xFFC62828)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 4.dp else 6.dp)
        ) {
            StatusPillExam(
                label = if (isCorrected) "Corregido" else "Por corregir",
                color = tint,
                icon = icon,
                isSmallScreen = isSmallScreen
            )
            if (!isCorrected) {
                if (isScanning) {
                    Box(
                        modifier = Modifier
                            .widthIn(min = if (isSmallScreen) 60.dp else 80.dp)
                            .height(if (isSmallScreen) 30.dp else 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(if (isSmallScreen) 16.dp else 20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onScanClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        contentPadding = PaddingValues(horizontal = if (isSmallScreen) 6.dp else 8.dp, vertical = if (isSmallScreen) 4.dp else 6.dp),
                        modifier = Modifier
                            .widthIn(max = if (isSmallScreen) 100.dp else 120.dp)
                            .defaultMinSize(minWidth = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Escanear",
                            modifier = Modifier.size(if (isSmallScreen) 14.dp else 16.dp)
                        )
                        Spacer(modifier = Modifier.width(if (isSmallScreen) 4.dp else 6.dp))
                        Text(
                            "Escanear",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isSmallScreen) 10.sp else 11.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun buildStudentInlineLabel(student: Student): AnnotatedString {
    val name = "${student.firstName} ${student.lastName}".trim()
    val cls = (student.className ?: "—").trim()
    return AnnotatedString.Builder().apply {
        append(name)
        append(" ")
        pushStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        )
        append(cls)
        pop()
    }.toAnnotatedString()
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
