package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.BimestersPage
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.repository.BimestersRepository

class GetBimestersUseCase(private val repository: BimestersRepository) {
    suspend operator fun invoke(
        page: Int = 1,
        perPage: Int = 20,
        query: String? = null,
        year: Int? = null
    ): Result<BimestersPage> {
        return repository.getBimesters(page, perPage, query, year)
    }
}