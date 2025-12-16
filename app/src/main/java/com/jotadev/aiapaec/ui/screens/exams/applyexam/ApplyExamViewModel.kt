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
                    val effectiveSectionId = sectionId ?: quiz.seccionId
                    val gradeParam = if (effectiveSectionId != null) null else effectiveGradeId
                    val pageResult = getStudents(page = 1, perPage = 100, query = null, gradeId = gradeParam, sectionId = effectiveSectionId)
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
            println("refreshStudentStatuses: Fetching status for quiz $qId")
            val statusResult = quizzesRepository.getQuizStatus(qId)
            if (statusResult is Result.Success) {
                val newStatuses = statusResult.data.mapKeys { it.key.toIntOrNull() ?: -1 }
                println("refreshStudentStatuses: Success, received ${newStatuses.size} statuses")
                _uiState.update { it.copy(studentStatuses = newStatuses) }
            } else {
                val msg = (statusResult as? Result.Error)?.message ?: "Unknown error"
                println("refreshStudentStatuses error: $msg")
            }
        }
    }

    private fun buildPerformanceBars(students: List<Student>): List<PerformanceBar> {
        // Datos de ejemplo mientras no exista distribución real
        val bins = (10..100 step 10).map { it }
        val sampleCounts = intArrayOf(0, 1, 0, 1, 4, 3, 2, 0, 0, 0)
        return bins.mapIndexed { idx, p -> PerformanceBar(label = "$p%", value = sampleCounts[idx].toFloat()) }
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
            val maxValue = bars.maxOf { it.value }.coerceAtLeast(1f)
            val axisColor = MaterialTheme.colorScheme.outline
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Texto vertical del eje Y
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart
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
                    Spacer(modifier = Modifier.width(0.dp))
                    // Números del eje Y (1–5) fuera del área, espaciado uniforme
                    val yTicks = (1..5).toList()
                    Column(
                        modifier = Modifier
                            .width(16.dp)
                            .fillMaxHeight()
                            .padding(top = 8.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        yTicks.reversed().forEach { t ->
                            Text(
                                text = t.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Contenedor del gráfico y ejes
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        // Rejilla punteada (horizontal por ticks Y, vertical por centro de cada barra)
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 8.dp, bottom = 8.dp)
                        ) {
                            val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            val gridColor = axisColor.copy(alpha = 0.35f)
                            val yCount = yTicks.size
                            val xCount = bars.size
                            val yStep = size.height / yCount
                            // Líneas horizontales (una por tick Y)
                            for (i in 1..yCount) {
                                val y = size.height - i * yStep
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1f,
                                    pathEffect = dash
                                )
                            }
                            // Líneas verticales centradas por barra
                            val xStep = size.width / xCount
                            for (j in 0 until xCount) {
                                val x = j * xStep + xStep / 2f
                                drawLine(
                                    color = gridColor,
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = 1f,
                                    pathEffect = dash
                                )
                            }
                        }
                        // Eje Y
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(axisColor)
                                .align(Alignment.BottomStart)
                        )
                        // Ticks del eje Y dentro del área, alineados a la línea
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .fillMaxHeight()
                                .padding(top = 8.dp, bottom = 8.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            yTicks.forEach { _ ->
                                Box(
                                    modifier = Modifier
                                        .width(8.dp)
                                        .height(1.dp)
                                        .background(axisColor)
                                )
                            }
                        }
                        // Eje X
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(axisColor)
                                .align(Alignment.BottomStart)
                        )
                        // Ticks sobre el eje X centrados con cada barra
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .height(8.dp)
                                .padding(start = 0.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            bars.forEach { _ ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .fillMaxHeight()
                                            .background(axisColor)
                                    )
                                }
                            }
                        }
                        // Barras
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 0.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            bars.forEach { bar ->
                                val fraction = (bar.value / maxValue).coerceIn(0f, 1f)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(horizontal = 2.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(fraction)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.secondary)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Etiquetas eje X: 0 al inicio y cada porcentaje centrado bajo su barra
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 30.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    bars.forEach { bar ->
                        Box(
                            modifier = Modifier
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bar.label.replace("%", ""),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                // Texto bajo el eje X
                Text(
                    text = "Porcentaje de puntuación",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )
                // Etiquetas 1–5 movidas al eje Y
            }
        }
    }
}
