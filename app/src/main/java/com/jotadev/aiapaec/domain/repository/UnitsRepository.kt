package com.jotadev.aiapaec.domain.repository

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.UnitsPage

interface UnitsRepository {
    suspend fun getUnits(
        page: Int = 1,
        perPage: Int = 20,
        bimesterId: Int? = null
    ): Result<UnitsPage>
}

