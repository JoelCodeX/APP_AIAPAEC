package com.jotadev.aiapaec.domain.repository

import com.jotadev.aiapaec.domain.models.GradesPage
import com.jotadev.aiapaec.domain.models.Result

interface GradesRepository {
    suspend fun getGradesByBranch(page: Int, perPage: Int): Result<GradesPage>
}
