package com.jotadev.aiapaec.domain.usecases

import com.jotadev.aiapaec.domain.models.GradesPage
import com.jotadev.aiapaec.domain.models.Result
import com.jotadev.aiapaec.domain.repository.GradesRepository

class GetGradesByBranchUseCase(
    private val repository: GradesRepository
) {
    suspend operator fun invoke(page: Int, perPage: Int): Result<GradesPage> {
        return repository.getGradesByBranch(page, perPage)
    }
}
