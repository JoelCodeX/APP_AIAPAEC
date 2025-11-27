package com.jotadev.aiapaec.domain.repository

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.WeeksPage

interface WeeksRepository {
    suspend fun getWeeks(
        page: Int = 1,
        perPage: Int = 20,
        unitId: Int? = null
    ): Result<WeeksPage>
}

