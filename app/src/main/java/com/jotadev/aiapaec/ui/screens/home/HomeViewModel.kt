package com.jotadev.aiapaec.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.data.api.RetrofitClient
import com.jotadev.aiapaec.data.repository.GradesRepositoryImpl
import com.jotadev.aiapaec.data.repository.QuizzesRepositoryImpl
import com.jotadev.aiapaec.data.repository.StudentRepositoryImpl
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.ui.screens.exams.answers.ExamResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class TimeRange {
    LAST_WEEK,
    LAST_BIMESTER,
    LAST_6_MONTHS
}

data class HomeUiState(
    val formatsCount: Int = 0,
    val weekliesCount: Int = 0,
    val gradesCount: Int = 0,
    val studentsCount: Int = 0,
    val performanceData: List<ExamResult> = emptyList(),
    val selectedTimeRange: TimeRange = TimeRange.LAST_6_MONTHS,
    val isLoadingMetrics: Boolean = false,
    val isLoadingChart: Boolean = false,
    val error: String? = null,
    val isSessionExpired: Boolean = false
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val quizzesRepo = QuizzesRepositoryImpl()
    private val gradesRepo = GradesRepositoryImpl()
    private val studentsRepo = StudentRepositoryImpl()
    private val apiService = RetrofitClient.apiService

    init {
        loadMetrics()
        loadPerformanceData(TimeRange.LAST_6_MONTHS)
    }

    fun refreshData() {
        loadMetrics()
        loadPerformanceData(_uiState.value.selectedTimeRange)
    }

    fun loadMetrics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMetrics = true, error = null) }
            try {
                // Execute in parallel
                val formatsDeferred = async { apiService.getWeeklyAssignments(page = 1, perPage = 1) }
                val weekliesDeferred = async { quizzesRepo.getQuizzes(page = 1, perPage = 1, query = null, gradoId = null, seccionId = null, bimesterId = null) }
                val gradesDeferred = async { gradesRepo.getGradesByBranch(page = 1, perPage = 200) } // Need all to count distinctive if needed, or just total items
                val studentsDeferred = async { studentsRepo.getStudents(page = 1, perPage = 1, query = null, gradeId = null, sectionId = null) }

                val formatsResp = formatsDeferred.await()
                val weekliesResult = weekliesDeferred.await()
                val gradesResult = gradesDeferred.await()
                val studentsResult = studentsDeferred.await()

                // Check for 401 Unauthorized
                if (formatsResp.code() == 401) {
                    _uiState.update { it.copy(isLoadingMetrics = false, isSessionExpired = true) }
                    return@launch
                }

                var formatsCount = 0
                if (formatsResp.isSuccessful) {
                    formatsCount = formatsResp.body()?.data?.total ?: 0
                }

                var weekliesCount = 0
                if (weekliesResult is Result.Success) {
                    weekliesCount = weekliesResult.data.total
                } else if (weekliesResult is Result.Error && weekliesResult.message.contains("401")) {
                     _uiState.update { it.copy(isLoadingMetrics = false, isSessionExpired = true) }
                     return@launch
                }

                var gradesCount = 0
                if (gradesResult is Result.Success) {
                    // Count unique grade names or total items. 
                    // HomeScreen used gradesState.grades.size. 
                    // GradesViewModel loads all grades by branch (perPage 200).
                    // So we take the items count.
                    gradesCount = gradesResult.data.items.size
                }

                var studentsCount = 0
                if (studentsResult is Result.Success) {
                    studentsCount = studentsResult.data.total
                }

                _uiState.update { 
                    it.copy(
                        isLoadingMetrics = false,
                        formatsCount = formatsCount,
                        weekliesCount = weekliesCount,
                        gradesCount = gradesCount,
                        studentsCount = studentsCount
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingMetrics = false, error = e.message) }
            }
        }
    }

    fun onTimeRangeSelected(range: TimeRange) {
        _uiState.update { it.copy(selectedTimeRange = range) }
        loadPerformanceData(range)
    }

    private fun loadPerformanceData(range: TimeRange) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingChart = true) }
            try {
                // 1. Get recent quizzes (fetch enough to filter)
                val quizzesResult = quizzesRepo.getQuizzes(page = 1, perPage = 50, query = null, gradoId = null, seccionId = null, bimesterId = null)
                if (quizzesResult is Result.Success) {
                    val allQuizzes = quizzesResult.data.items
                    
                    // 2. Filter by date based on range
                    val today = LocalDate.now()
                    val filteredQuizzes = allQuizzes.filter { quiz ->
                        try {
                            val quizDate = LocalDate.parse(quiz.fecha)
                            when (range) {
                                TimeRange.LAST_WEEK -> quizDate.isAfter(today.minusWeeks(1)) || quizDate.isEqual(today.minusWeeks(1))
                                TimeRange.LAST_BIMESTER -> quizDate.isAfter(today.minusMonths(2)) || quizDate.isEqual(today.minusMonths(2))
                                TimeRange.LAST_6_MONTHS -> quizDate.isAfter(today.minusMonths(6)) || quizDate.isEqual(today.minusMonths(6))
                            }
                        } catch (e: Exception) {
                            false // Skip if date parsing fails
                        }
                    }.sortedBy { it.fecha } // Oldest to newest for chart

                    // 3. For each quiz, get status to calculate average score
                    // Limit to top 20 to avoid too many requests
                    val quizzesToProcess = filteredQuizzes.takeLast(20)

                    val performanceData = quizzesToProcess.map { quiz ->
                        async {
                            try {
                                val statusResp = apiService.getQuizStatus(quiz.id)
                                if (statusResp.isSuccessful) {
                                    val statusMap = statusResp.body()
                                    val scores = statusMap?.values?.mapNotNull { it.score } ?: emptyList()
                                    val average = if (scores.isNotEmpty()) scores.average().toFloat() else 0f
                                    ExamResult(quiz.id.toString(), quiz.fecha, average)
                                } else {
                                    ExamResult(quiz.id.toString(), quiz.fecha, 0f)
                                }
                            } catch (e: Exception) {
                                ExamResult(quiz.id.toString(), quiz.fecha, 0f)
                            }
                        }
                    }.awaitAll()

                    _uiState.update { 
                        it.copy(
                            isLoadingChart = false,
                            performanceData = performanceData
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoadingChart = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingChart = false) }
            }
        }
    }
}
