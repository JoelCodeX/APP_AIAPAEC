package com.jotadev.aiapaec.domain.repository

import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.models.BimestersPage

interface BimestersRepository {
    suspend fun getBimesters(
        page: Int = 1,
        perPage: Int = 20,
        query: String? = null,
        year: Int? = null
    ): Result<BimestersPage>
}