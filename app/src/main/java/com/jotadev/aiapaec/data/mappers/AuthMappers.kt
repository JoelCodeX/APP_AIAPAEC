package com.jotadev.aiapaec.data.mappers

import com.jotadev.aiapaec.data.api.LoginRequest
import com.jotadev.aiapaec.data.api.LoginResponse
import com.jotadev.aiapaec.data.api.UserData
import com.jotadev.aiapaec.domain.models.LoginData
import com.jotadev.aiapaec.domain.models.LoginResult
import com.jotadev.aiapaec.domain.models.User as DomainUser

// CONVERTIR DE DOMAIN A DATA
fun LoginData.toLoginRequest(): LoginRequest {
    return LoginRequest(
        email = this.email,
        password = this.password
    )
}

// CONVERTIR DE DATA A DOMAIN
fun LoginResponse.toLoginResult(): LoginResult {
    return LoginResult(
        message = this.message,
        token = this.token,
        user = this.user?.toDomainUser()
    )
}

fun UserData.toDomainUser(): DomainUser {
    return DomainUser(
        id = this.id,
        username = this.username,
        fullName = this.full_name,
        email = this.email,
        role = this.role,
        status = this.status,
        branchId = this.branch_id,
        lastLogin = this.last_login,
        createdAt = this.created_at,
        updatedAt = this.updated_at
    )
}