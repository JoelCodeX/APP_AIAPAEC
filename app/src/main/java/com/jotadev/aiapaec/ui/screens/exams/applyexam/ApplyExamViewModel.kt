package com.jotadev.aiapaec.ui.screens.exams.applyexam

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.data.repository.QuizzesRepositoryImpl
import com.jotadev.aiapaec.data.repository.StudentRepositoryImpl
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.models.QuizAnswer
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Student
import com.jotadev.aiapaec.domain.models.StudentStatus
import com.jotadev.aiapaec.domain.usecases.GetStudentsUseCase
import com.jotadev.aiapaec.domain.usecases.UploadAnswerKeyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerformanceBar(val label: String, val value: Float)

data class ApplyExamUiState(
    val quiz: Quiz? = null,
    val students: List<Student> = emptyList(),
    val performanceBars: List<PerformanceBar> = emptyList(),
    val studentStatuses: Map<Int, StudentStatus> = emptyMap(),
    val answers: List<QuizAnswer> = emptyList(),
    val hasKey: Boolean = false,
    val justUploaded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showDistribution: Boolean = false,
    val expectedNumQuestions: Int? = null
)

class ApplyExamViewModel(
    private val quizzesRepository: QuizzesRepositoryImpl = QuizzesRepositoryImpl(),
    private val getStudents: GetStudentsUseCase = GetStudentsUseCase(StudentRepositoryImpl()),
    private val uploadAnswerKey: UploadAnswerKeyUseCase = UploadAnswerKeyUseCase(quizzesRepository)
) : ViewModel() {
    private val _uiState = MutableStateFlow(ApplyExamUiState())
    val uiState: StateFlow<ApplyExamUiState> = _uiState

    fun load(examId: String, gradeId: Int? = null, sectionId: Int? = null) {
        val id = examId.toIntOrNull()
        if (id == null) {
            _uiState.update { it.copy(errorMessage = "ID de evaluación inválido") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = quizzesRepository.getQuiz(id)) {
                is Result.Success -> {
                    val quiz = result.data
                    _uiState.update { it.copy(quiz = quiz, expectedNumQuestions = quiz.numQuestions) }
                    // Cargar respuestas del quiz si existen
                    loadAnswers(quiz.id)
                    when (val keys = quizzesRepository.listAnswerKeys(quiz.id, page = 1, pageSize = 10)) {
                        is Result.Success -> _uiState.update { it.copy(hasKey = keys.data.items.isNotEmpty()) }
                        else -> { }
                    }
                    val effectiveGradeId = gradeId ?: quiz.gradoId
                    // Si gradeId viene informado (contexto asignación), respetamos sectionId (aunque sea null) para evitar fallback erróneo al quiz
                    val effectiveSectionId = if (gradeId != null) sectionId else (sectionId ?: quiz.seccionId)
                    
                    // Si se especifica sección, ignoramos el grado para evitar filtros redundantes o conflictos
                    val queryGradeId = if (effectiveSectionId != null) null else effectiveGradeId
                    
                    val pageResult = getStudents(page = 1, perPage = 100, query = null, gradeId = queryGradeId, sectionId = effectiveSectionId)
                    when (pageResult) {
                        is Result.Success -> {
                            val items = pageResult.data.items
                            val filtered = items
                            
                            _uiState.update {
                                it.copy(
                                    students = filtered,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                            // Cargar estados después de tener estudiantes
                            refreshStudentStatuses(quiz.id)
                        }
                        is Result.Error -> {
                            _uiState.update { it.copy(isLoading = false, errorMessage = pageResult.message) }
                        }
                        is Result.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun refreshStudentStatuses(quizId: Int? = null) {
        val qId = quizId ?: _uiState.value.quiz?.id ?: return
        viewModelScope.launch {
            // Solo activar loading si no es una actualización silenciosa (opcional, pero para PullRefresh sirve)
            // Podemos usar una bandera separada isRefreshing si no queremos bloquear toda la UI,
            // pero isLoading está bien si PullRefreshIndicator lo usa.
            _uiState.update { it.copy(isLoading = true) }
            
            println("refreshStudentStatuses: Fetching status for quiz $qId")
            val statusResult = quizzesRepository.getQuizStatus(qId)
            
            if (statusResult is Result.Success) {
                val newStatuses = statusResult.data.mapKeys { it.key.toIntOrNull() ?: -1 }
                println("refreshStudentStatuses: Success, received ${newStatuses.size} statuses")
                val bars = buildPerformanceBars(newStatuses)
                _uiState.update { it.copy(studentStatuses = newStatuses, performanceBars = bars, isLoading = false) }
            } else {
                val msg = (statusResult as? Result.Error)?.message ?: "Unknown error"
                println("refreshStudentStatuses error: $msg")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun buildPerformanceBars(statuses: Map<Int, StudentStatus>): List<PerformanceBar> {
        if (statuses.isEmpty()) return emptyList()

        // Validar contra la lista actual de estudiantes para no contar eliminados
        val validStudentIds = _uiState.value.students.map { it.id }.toSet()

        // Asumimos que el puntaje máximo es 100, ya que el sistema maneja puntajes en esa escala
        // independientemente del número de preguntas.
        val maxScore = 100.0

        // Usamos 10 rangos de 10% cada uno (0-10%, 11-20%, ..., 91-100%)
        val totalBins = 10
        val binCounts = IntArray(totalBins)
        // Etiquetas: 10, 20, 30... 100
        val binLabels = (1..totalBins).map { "${it * 10}%" }

        statuses.forEach { (studentId, status) ->
            // Solo procesamos si el estudiante está en la lista actual
            if (studentId !in validStudentIds) return@forEach

            // Solo incluimos estudiantes que tengan un puntaje (score != null)
            // Si el score es null (no ha rendido o se eliminó), no entra en la gráfica.
            val score = status.score ?: return@forEach
            
            val percentage = (score / maxScore * 100).coerceIn(0.0, 100.0)
            
            // Calculamos el índice del bin (0 a 9)
            // 0-9.99 -> 0
            // 10-19.99 -> 1
            // ...
            // 100 -> 9
            val binIndex = (percentage / 10).toInt().coerceAtMost(totalBins - 1)
            binCounts[binIndex]++
        }

        return binLabels.mapIndexed { idx, label ->
            PerformanceBar(label = label, value = binCounts[idx].toFloat())
        }
    }

    fun uploadAnswerKeyFromUri(quizId: Int, uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                when (val chk = quizzesRepository.getQuiz(quizId)) {
                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = chk.message) }
                        return@launch
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Result.Success -> {
                        _uiState.update { it.copy(quiz = chk.data) }
                    }
                }
                val name = "solucionario.pdf"
                val mime = "application/pdf"
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
                when (val result = uploadAnswerKey(quizId, name, mime, bytes)) {
                    is Result.Success -> {
                        var extractedCount: Int? = null
                        when (val answersRes = quizzesRepository.getQuizAnswers(quizId)) {
                            is Result.Success -> {
                                extractedCount = answersRes.data.items.size
                            }
                            else -> {}
                        }
                        if ((extractedCount ?: 0) == 0) {
                            when (val keysRes = quizzesRepository.listAnswerKeys(quizId, page = 1, pageSize = 10)) {
                                is Result.Success -> {
                                    val latest = keysRes.data.items.maxByOrNull { it.version }
                                    extractedCount = latest?.parsedKeys?.size
                                }
                                else -> {}
                            }
                        }
                        val expected = _uiState.value.expectedNumQuestions ?: _uiState.value.quiz?.numQuestions
                        val matches = (extractedCount != null && expected != null && extractedCount == expected)
                        if (matches) {
                            when (val q = quizzesRepository.getQuiz(quizId)) {
                                is Result.Success -> _uiState.update { it.copy(quiz = q.data, isLoading = false, hasKey = true, justUploaded = true) }
                                is Result.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = q.message) }
                                is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                            }
                            loadAnswers(quizId)
                        } else {
                            when (quizzesRepository.deleteLatestAnswerKey(quizId)) {
                                is Result.Success -> {
                                    _uiState.update { it.copy(isLoading = false, hasKey = false, errorMessage = "Solucionario no compatible") }
                                }
                                is Result.Error -> {
                                    _uiState.update { it.copy(isLoading = false, hasKey = false, errorMessage = "Solucionario no compatible") }
                                }
                                is Result.Loading -> {
                                    _uiState.update { it.copy(isLoading = true) }
                                }
                            }
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun loadAnswers(quizId: Int) {
        viewModelScope.launch {
            when (val result = quizzesRepository.getQuizAnswers(quizId)) {
                is Result.Success -> {
                    val items = result.data.items
                    _uiState.update { it.copy(answers = items, hasKey = it.hasKey || items.isNotEmpty()) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Result.Loading -> {
                    // no-op
                }
            }
        }
    }

    fun ackJustUploaded() {
        _uiState.update { it.copy(justUploaded = false) }
    }

    // Toggle para mostrar/ocultar la sección de distribución
    fun toggleDistribution() {
        _uiState.update { it.copy(showDistribution = !it.showDistribution) }
    }
}

@Composable
fun PerformanceBarChart(title: String, bars: List<PerformanceBar>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onPrimary)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (bars.isEmpty()) {
            Text(text = "Sin datos aún", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            // Calcular escala del eje Y dinámicamente
            val maxCount = bars.maxOf { it.value }.toInt()
            val steps = 5
            val stepSize = if (maxCount <= 5) 1 else ((maxCount + steps - 1) / steps)
            val yTicks = (1..steps).map { it * stepSize }
            val maxYTick = yTicks.last().toFloat()

            val axisColor = MaterialTheme.colorScheme.outline
            
            // Layout principal: Eje Y a la izquierda, Gráfico + Eje X a la derecha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp), // Aumentamos altura para acomodar etiquetas
                verticalAlignment = Alignment.Top
            ) {
                // SECCIÓN IZQUIERDA: EJE Y (Texto "Estudiantes" + Números)
                Row(
                    modifier = Modifier
                        .width(36.dp) // Espacio fijo para el eje Y
                        .fillMaxHeight()
                        .padding(bottom = 24.dp), // Espacio para alinear con el gráfico (descontando altura de etiquetas X)
                    horizontalArrangement = Arrangement.End
                ) {
                    // Texto vertical "Estudiantes"
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Estudiantes",
                            modifier = Modifier.rotate(-90f),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            softWrap = false,
                            maxLines = 1,
                            overflow = TextOverflow.Visible
                        )
                    }
                    
                    // Números del eje Y
                    Column(
                        modifier = Modifier
                            .width(22.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        yTicks.reversed().forEach { t ->
                            Text(
                                text = t.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        // Espacio para el 0 (alineado con la base)
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // SECCIÓN DERECHA: GRÁFICO + EJE X
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Área del Gráfico (Barras + Grid)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // 1. Grid y Ejes dibujados con Canvas
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val dash = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                            val gridColor = axisColor.copy(alpha = 0.3f)
                            
                            // Líneas Horizontales (Grid Y)
                            // Dibujamos steps + 1 líneas (incluyendo el 0)
                            val yStep = size.height / steps
                            for (i in 0..steps) {
                                val y = i * yStep
                                drawLine(
                                    color = if (i == steps) axisColor else gridColor, // La última es el eje X (base)
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = if (i == steps) 2f else 1f,
                                    pathEffect = if (i == steps) null else dash
                                )
                            }

                            // Líneas Verticales (Grid X)
                            // 10 rangos -> 11 líneas (0, 10, ..., 100)
                            val xSteps = 10
                            val xStep = size.width / xSteps
                            for (i in 0..xSteps) {
                                val x = i * xStep
                                drawLine(
                                    color = if (i == 0) axisColor else gridColor, // La primera es el eje Y
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = if (i == 0) 2f else 1f,
                                    pathEffect = if (i == 0) null else dash
                                )
                            }
                        }

                        // 2. Barras
                        Row(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            bars.forEach { bar ->
                                val fraction = (bar.value / maxYTick).coerceIn(0f, 1f)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    // Barra coloreada
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f) // Un poco de espacio entre barras
                                            .fillMaxHeight(fraction)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(MaterialTheme.colorScheme.secondary)
                                    )
                                }
                            }
                        }
                    }

                    // Etiquetas Eje X
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Generamos etiquetas 0, 10, 20... 100
                        for (i in 0..10) {
                            Text(
                                text = "${i * 10}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(20.dp) // Ancho fijo para centrar mejor
                            )
                        }
                    }
                    
                    // Título del Eje X
                    Text(
                        text = "Porcentaje de puntuación",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
