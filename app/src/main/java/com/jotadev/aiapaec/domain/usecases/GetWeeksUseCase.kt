package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.WeeksPage
import com.jotadev.aiapaec.domain.repository.WeeksRepository

class GetWeeksUseCase(private val repository: WeeksRepository) {
    suspend operator fun invoke(
        page: Int = 1,
        perPage: Int = 20,
        unitId: Int? = null
    ): Result<WeeksPage> {
        return repository.getWeeks(page, perPage, unitId)
    }
}

