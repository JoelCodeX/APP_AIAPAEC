package com.jotadev.aiapaec.ui.screens.exams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.models.Student
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.data.repository.QuizzesRepositoryImpl
import com.jotadev.aiapaec.data.repository.StudentRepositoryImpl
import com.jotadev.aiapaec.domain.usecases.GetStudentsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerformanceBar(val label: String, val value: Float)

data class ApplyExamUiState(
    val quiz: Quiz? = null,
    val students: List<Student> = emptyList(),
    val performanceBars: List<PerformanceBar> = emptyList(),
    val studentStatuses: Map<Int, String> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ApplyExamViewModel(
    private val quizzesRepository: QuizzesRepositoryImpl = QuizzesRepositoryImpl(),
    private val getStudents: GetStudentsUseCase = GetStudentsUseCase(StudentRepositoryImpl())
) : ViewModel() {
    private val _uiState = MutableStateFlow(ApplyExamUiState())
    val uiState: StateFlow<ApplyExamUiState> = _uiState

    fun load(examId: String) {
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
                    _uiState.update { it.copy(quiz = quiz) }
                    // Cargar estudiantes (filtrar por classId si existe)
                    val pageResult = getStudents(page = 1, perPage = 100, query = null)
                    when (pageResult) {
                        is Result.Success -> {
                            val items = pageResult.data.items
                            val filtered = quiz.classId?.let { classId ->
                                items.filter { it.classId == classId }
                            } ?: items
                            val statuses = filtered.associate { it.id to "Por corregir" }
                            val bars = buildPerformanceBars(filtered)
                            _uiState.update {
                                it.copy(
                                    students = filtered,
                                    studentStatuses = statuses,
                                    performanceBars = bars,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
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

    private fun buildPerformanceBars(students: List<Student>): List<PerformanceBar> {
        if (students.isEmpty()) return emptyList()
        val total = students.size.toFloat()
        val corrected = 0f
        val pending = total
        return listOf(
            PerformanceBar(label = "Corregidos", value = corrected),
            PerformanceBar(label = "Por corregir", value = pending)
        )
    }
}

@Composable
fun PerformanceBarChart(title: String, bars: List<PerformanceBar>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (bars.isEmpty()) {
            Text(
                text = "Sin datos aún",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val maxValue = bars.maxOf { it.value }.coerceAtLeast(1f)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                bars.forEach { bar ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = bar.label,
                            modifier = Modifier.width(100.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .height(18.dp)
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            val barColor = if (bar.label == "Corregidos") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(bar.value / maxValue)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(barColor)
                            )
                        }
                        Text(
                            text = "${bar.value.toInt()}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}