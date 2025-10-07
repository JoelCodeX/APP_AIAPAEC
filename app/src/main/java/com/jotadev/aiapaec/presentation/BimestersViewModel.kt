package com.jotadev.aiapaec.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jotadev.aiapaec.data.api.RetrofitClient
import com.jotadev.aiapaec.data.repository.BimestersRepositoryImpl
import com.jotadev.aiapaec.domain.models.Bimester
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.usecases.GetBimestersUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class BimestersUiState(
    val bimesters: List<Bimester> = emptyList(),
    val query: String = "",
    val year: Int? = null,
    val page: Int = 1,
    val perPage: Int = 20,
    val total: Int = 0,
    val pages: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class BimestersViewModel : ViewModel() {
    private val getBimesters = GetBimestersUseCase(
        BimestersRepositoryImpl(RetrofitClient.apiService)
    )
    private val _uiState = MutableStateFlow(BimestersUiState())
    val uiState: StateFlow<BimestersUiState> = _uiState

    init {
        fetchBimesters()
        // Auto-refresh every 30s similar to other viewmodels
        viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                fetchBimesters(page = 1)
            }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
    }

    fun onYearChange(value: Int?) {
        _uiState.update { it.copy(year = value) }
    }

    fun fetchBimesters(
        page: Int = _uiState.value.page,
        perPage: Int = _uiState.value.perPage,
        query: String? = _uiState.value.query.takeIf { it.isNotBlank() },
        year: Int? = _uiState.value.year
    ) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, page = page, perPage = perPage) }
        viewModelScope.launch {
            when (val result = getBimesters(page, perPage, query, year)) {
                is Result.Success -> {
                    val pageData = result.data
                    _uiState.update {
                        it.copy(
                            bimesters = pageData.items,
                            page = pageData.page,
                            perPage = pageData.perPage,
                            total = pageData.total,
                            pages = pageData.pages,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }

                Result.Loading -> TODO()
            }
        }
    }
}