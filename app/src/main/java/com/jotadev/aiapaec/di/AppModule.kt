package com.jotadev.aiapaec.di

import com.jotadev.aiapaec.data.repository.AuthRepositoryImpl
import com.jotadev.aiapaec.data.repository.UserRepositoryImpl
import com.jotadev.aiapaec.domain.repository.AuthRepository
import com.jotadev.aiapaec.domain.repository.UserRepository
import com.jotadev.aiapaec.domain.usecases.CheckAuthStatusUseCase
import com.jotadev.aiapaec.domain.usecases.GetCurrentUserUseCase
import com.jotadev.aiapaec.domain.usecases.LoginUseCase
import com.jotadev.aiapaec.domain.usecases.LogoutUseCase
// import dagger.Module
// import dagger.Provides
// import dagger.hilt.InstallIn
// import dagger.hilt.components.SingletonComponent
// import javax.inject.Singleton

// @Module
// @InstallIn(SingletonComponent::class)
object AppModule {
    
    // @Provides
    // @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }
    
    // @Provides
    // @Singleton
    fun provideUserRepository(): UserRepository {
        return UserRepositoryImpl()
    }
    
    // @Provides
    fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase {
        return LoginUseCase(authRepository)
    }
    
    // @Provides
    fun provideLogoutUseCase(authRepository: AuthRepository): LogoutUseCase {
        return LogoutUseCase(authRepository)
    }
    
    // @Provides
    fun provideCheckAuthStatusUseCase(authRepository: AuthRepository): CheckAuthStatusUseCase {
        return CheckAuthStatusUseCase(authRepository)
    }
    
    // @Provides
    fun provideGetCurrentUserUseCase(
        userRepository: UserRepository,
        authRepository: AuthRepository
    ): GetCurrentUserUseCase {
        return GetCurrentUserUseCase(userRepository, authRepository)
    }
}