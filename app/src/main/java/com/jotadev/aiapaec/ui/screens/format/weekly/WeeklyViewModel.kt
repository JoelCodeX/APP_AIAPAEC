package com.jotadev.aiapaec.ui.screens.format.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import com.jotadev.aiapaec.data.api.RetrofitClient
import com.jotadev.aiapaec.data.repository.QuizzesRepositoryImpl
import com.jotadev.aiapaec.data.repository.UnitsRepositoryImpl
import com.jotadev.aiapaec.data.repository.WeeksRepositoryImpl
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.domain.models.Week
import com.jotadev.aiapaec.domain.usecases.GetUnitsUseCase
import com.jotadev.aiapaec.domain.usecases.GetWeeksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WeeklyUiState(
    val isLoading: Boolean = false,
    val createdQuiz: Quiz? = null,
    val message: String? = null,
    val isMetaLoading: Boolean = false,
    val quizzes: List<Quiz> = emptyList(),
    val allQuizzes: List<Quiz> = emptyList(),
    val gradesOptions: List<String> = emptyList(),
    val sectionsOptions: List<String> = emptyList(),
    val sectionsByGrade: Map<String, List<String>> = emptyMap(),
    val unitOptions: List<String> = emptyList(),
    val weekOptions: List<Int> = emptyList(),
    val isUnitsLoading: Boolean = false,
    val isWeeksLoading: Boolean = false
)

class WeeklyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WeeklyUiState())
    val uiState: StateFlow<WeeklyUiState> = _uiState.asStateFlow()

    private val repo = QuizzesRepositoryImpl()
    private var gradeNameToId: Map<String, Int> = emptyMap()
    private val getUnits = GetUnitsUseCase(UnitsRepositoryImpl(RetrofitClient.apiService))
    private val getWeeks = GetWeeksUseCase(WeeksRepositoryImpl(RetrofitClient.apiService))
    private var unitLabelToId: Map<String, Int> = emptyMap()
    private var weeksByNumber: Map<Int, Week> = emptyMap()
    private var weeksById: Map<Int, Week> = emptyMap()
    private val weekNumberByQuizId: MutableMap<Int, Int> = mutableMapOf()
    private var loadJob: Job? = null

    init {
        loadQuizzes()
        loadMetaOptions()
    }

    fun createQuiz(
        bimesterId: Int?,
        unidadId: Int?,
        fecha: String,
        numQuestions: Int?,
        detalle: String?,
        asignacionId: Int?,
        gradoId: Int? = null,
        seccionId: Int? = null,
        weekNumber: Int? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val weekId = weekNumber?.let { weeksByNumber[it]?.id }
            val res = repo.createQuiz(
                bimesterId = bimesterId,
                unidadId = unidadId,
                gradoId = gradoId,
                seccionId = seccionId,
                weekId = weekId,
                fecha = fecha,
                numQuestions = numQuestions,
                detalle = detalle,
                asignacionId = asignacionId
            )
            when (res) {
                is com.jotadev.aiapaec.domain.models.Result.Success -> {
                    val current = _uiState.value
                    val newList = listOf(res.data) + current.quizzes
                    weekNumber?.let { wn -> weekNumberByQuizId[res.data.id] = wn }
                    _uiState.value = current.copy(
                        isLoading = false,
                        createdQuiz = res.data,
                        quizzes = newList,
                        allQuizzes = newList,
                        message = "Semanal creado"
                    )
                }
                is com.jotadev.aiapaec.domain.models.Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, message = res.message)
                com.jotadev.aiapaec.domain.models.Result.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun reloadMetaOptions() {
        loadMetaOptions()
    }

    fun refreshQuizzes() {
        loadQuizzes()
    }

    private fun loadMetaOptions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMetaLoading = true)
            val gradesResp = RetrofitClient.apiService.getGradesByBranch(page = 1, perPage = 200)
            val gradeItems = gradesResp.body()?.data?.items ?: emptyList()
            gradeNameToId = gradeItems.associate { it.nombre to it.id }
            val gradesBranch = gradeItems.map { it.nombre }.distinct().sorted()
            _uiState.value = _uiState.value.copy(
                isMetaLoading = false,
                gradesOptions = gradesBranch,
                sectionsOptions = emptyList(),
                sectionsByGrade = emptyMap(),
                unitOptions = emptyList(),
                weekOptions = emptyList()
            )
        }
    }

    fun loadSectionsForGrade(gradeName: String?) {
        if (gradeName.isNullOrBlank()) return
        val gradeId = gradeNameToId[gradeName] ?: return
        viewModelScope.launch {
            val resp = RetrofitClient.apiService.getSectionsByBranch(page = 1, perPage = 200, gradeId = gradeId)
            val items = resp.body()?.data?.items ?: emptyList()
            val sections = items.map { it.nombre }.distinct().sorted()
            val currentMap = _uiState.value.sectionsByGrade.toMutableMap()
            currentMap[gradeName] = sections
            _uiState.value = _uiState.value.copy(sectionsByGrade = currentMap, sectionsOptions = sections)
        }
    }

    fun loadQuizzes(
        query: String? = null,
        grade: String? = null,
        section: String? = null,
        bimesterLabel: String? = null,
        unidadLabel: String? = null,
        assignmentId: Int? = null
    ) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val gradeId = grade?.let { gradeNameToId[it] }
            val bimesterId = bimesterLabel?.let { label ->
                when (label.uppercase()) {
                    "I BIMESTRE" -> 1
                    "II BIMESTRE" -> 2
                    "III BIMESTRE" -> 3
                    "IV BIMESTRE" -> 4
                    else -> null
                }
            }
            val res = repo.getQuizzes(page = 1, perPage = 50, query = query, gradoId = gradeId, seccionId = null, bimesterId = bimesterId, asignacionId = assignmentId)
            when (res) {
                is Result.Success -> {
                    var items = res.data.items
                    section?.let { s -> items = items.filter { (it.seccionNombre ?: "").equals(s, true) } }
                    unidadLabel?.let { uLabel ->
                        val uId = when (uLabel.uppercase()) {
                            "I UNIDAD" -> 1
                            "II UNIDAD" -> 2
                            "III UNIDAD" -> 3
                            "IV UNIDAD" -> 4
                            else -> null
                        }
                        uId?.let { uid -> items = items.filter { it.unidadId == uid } }
                    }
                    query?.let { q -> items = items.filter { (it.detalle ?: "").contains(q, true) } }
                    _uiState.value = _uiState.value.copy(isLoading = false, quizzes = items, allQuizzes = items)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, message = res.message)
                Result.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
            }
        }
    }

    private fun mapBimesterLabelToId(label: String?): Int? {
        return label?.let {
            when (it.uppercase()) {
                "I BIMESTRE" -> 1
                "II BIMESTRE" -> 2
                "III BIMESTRE" -> 3
                "IV BIMESTRE" -> 4
                else -> null
            }
        }
    }

    fun loadUnitsForBimester(bimesterLabel: String?) {
        val bimId = mapBimesterLabelToId(bimesterLabel) ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUnitsLoading = true, unitOptions = emptyList(), weekOptions = emptyList())
            when (val result = getUnits(page = 1, perPage = 50, bimesterId = bimId)) {
                is Result.Success -> {
                    val units = result.data.items
                    val map = units.associate { unit ->
                        val label = when (unit.unitNumber) {
                            1 -> "I UNIDAD"
                            2 -> "II UNIDAD"
                            3 -> "III UNIDAD"
                            4 -> "IV UNIDAD"
                            else -> "UNIDAD ${unit.unitNumber}"
                        }
                        label to unit.id
                    }
                    unitLabelToId = map
                    _uiState.value = _uiState.value.copy(isUnitsLoading = false, unitOptions = map.keys.toList().sorted())
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(isUnitsLoading = false, message = result.message)
                is Result.Loading -> _uiState.value = _uiState.value.copy(isUnitsLoading = true)
            }
        }
    }

    fun loadUnitsForBimesterById(bimesterId: Int?) {
        val bimId = bimesterId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUnitsLoading = true, unitOptions = emptyList(), weekOptions = emptyList())
            when (val result = getUnits(page = 1, perPage = 50, bimesterId = bimId)) {
                is Result.Success -> {
                    val units = result.data.items
                    val map = units.associate { unit ->
                        val label = when (unit.unitNumber) {
                            1 -> "I UNIDAD"
                            2 -> "II UNIDAD"
                            3 -> "III UNIDAD"
                            4 -> "IV UNIDAD"
                            else -> "UNIDAD ${unit.unitNumber}"
                        }
                        label to unit.id
                    }
                    unitLabelToId = map
                    _uiState.value = _uiState.value.copy(isUnitsLoading = false, unitOptions = map.keys.toList().sorted())
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(isUnitsLoading = false, message = result.message)
                is Result.Loading -> _uiState.value = _uiState.value.copy(isUnitsLoading = true)
            }
        }
    }

    fun loadWeeksForUnit(unidadLabel: String?) {
        val unitId = unidadLabel?.let { unitLabelToId[it] } ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWeeksLoading = true, weekOptions = emptyList())
            when (val result = getWeeks(page = 1, perPage = 50, unitId = unitId)) {
                is Result.Success -> {
                    val weeks = result.data.items
                    weeksByNumber = weeks.associateBy { it.weekNumber }
                    weeksById = weeks.associateBy { it.id }
                    _uiState.value = _uiState.value.copy(isWeeksLoading = false, weekOptions = weeks.map { it.weekNumber }.sorted())
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(isWeeksLoading = false, message = result.message)
                is Result.Loading -> _uiState.value = _uiState.value.copy(isWeeksLoading = true)
            }
        }
    }

    fun getWeekStartDate(weekNumber: Int?): String? {
        return weekNumber?.let { weeksByNumber[it]?.startDate }
    }

    fun getBimesterIdFromLabel(label: String?): Int? {
        return mapBimesterLabelToId(label)
    }

    fun getUnitIdFromLabel(label: String?): Int? {
        return label?.let { unitLabelToId[it] }
    }

    fun getStoredWeekNumberForItem(quiz: Quiz): Int? {
        val direct = quiz.weekNumber
        val fromId = quiz.weekId?.let { weeksById[it]?.weekNumber }
        return direct ?: fromId ?: weekNumberByQuizId[quiz.id] ?: parseWeekNumberFromDetalle(quiz.detalle)
    }

    private fun parseWeekNumberFromDetalle(detalle: String?): Int? {
        val text = detalle ?: return null
        val re = Regex("(Semana|Semanal|N°|No)\\s*[:#]?\\s*(\\d{1,2})", RegexOption.IGNORE_CASE)
        val m = re.find(text) ?: return null
        return m.groupValues.getOrNull(2)?.toIntOrNull()
    }

    fun deleteWeekly(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = repo.deleteQuiz(id)) {
                is Result.Success -> {
                    val remaining = _uiState.value.quizzes.filterNot { it.id == id }
                    weekNumberByQuizId.remove(id)
                    _uiState.value = _uiState.value.copy(isLoading = false, quizzes = remaining, allQuizzes = remaining, message = "Semanal eliminado")
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, message = res.message)
                Result.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
            }
        }
    }

    fun updateWeekly(
        id: Int,
        detalle: String?,
        bimesterId: Int? = null,
        unidadId: Int? = null,
        gradoId: Int? = null,
        seccionId: Int? = null,
        fecha: String? = null,
        numQuestions: Int? = null,
        asignacionId: Int? = null,
        weekNumber: Int? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val weekId = weekNumber?.let { weeksByNumber[it]?.id }
            when (val res = repo.updateQuiz(
                id = id,
                bimesterId = bimesterId,
                unidadId = unidadId,
                gradoId = gradoId,
                seccionId = seccionId,
                weekId = weekId,
                fecha = fecha,
                numQuestions = numQuestions,
                detalle = detalle,
                asignacionId = asignacionId
            )) {
                is Result.Success -> {
                    val updatedItem = res.data
                    val newList = _uiState.value.quizzes.map { if (it.id == id) updatedItem else it }
                    val resolvedWeekNumber = weekNumber
                        ?: updatedItem.weekId?.let { wkId -> weeksById[wkId]?.weekNumber }
                        ?: parseWeekNumberFromDetalle(updatedItem.detalle)
                    resolvedWeekNumber?.let { wn -> weekNumberByQuizId[id] = wn }
                    _uiState.value = _uiState.value.copy(isLoading = false, quizzes = newList, allQuizzes = newList, message = "Semanal actualizado")
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, message = res.message)
                Result.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
            }
        }
    }
}

