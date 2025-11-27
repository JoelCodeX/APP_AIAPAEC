package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.UnitsPage
import com.jotadev.aiapaec.domain.repository.UnitsRepository

class GetUnitsUseCase(private val repository: UnitsRepository) {
    suspend operator fun invoke(
        page: Int = 1,
        perPage: Int = 20,
        bimesterId: Int? = null
    ): Result<UnitsPage> {
        return repository.getUnits(page, perPage, bimesterId)
    }
}

