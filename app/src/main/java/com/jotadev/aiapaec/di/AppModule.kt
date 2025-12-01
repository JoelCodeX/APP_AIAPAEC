package com.jotadev.aiapaec.di

import com.jotadev.aiapaec.data.repository.AuthRepositoryImpl
import com.jotadev.aiapaec.data.repository.UserRepositoryImpl
import com.jotadev.aiapaec.data.repository.StudentRepositoryImpl
// Eliminado ClassesRepositoryImpl
import com.jotadev.aiapaec.data.repository.GradesRepositoryImpl
import com.jotadev.aiapaec.data.repository.BimestersRepositoryImpl
import com.jotadev.aiapaec.data.repository.QuizzesRepositoryImpl
import com.jotadev.aiapaec.data.repository.UnitsRepositoryImpl
import com.jotadev.aiapaec.data.repository.WeeksRepositoryImpl
import com.jotadev.aiapaec.data.api.RetrofitClient
import com.jotadev.aiapaec.domain.repository.AuthRepository
import com.jotadev.aiapaec.domain.repository.UserRepository
import com.jotadev.aiapaec.domain.repository.StudentRepository
// Eliminado ClassesRepository
import com.jotadev.aiapaec.domain.repository.GradesRepository
import com.jotadev.aiapaec.domain.repository.BimestersRepository
import com.jotadev.aiapaec.domain.repository.QuizzesRepository
import com.jotadev.aiapaec.domain.repository.UnitsRepository
import com.jotadev.aiapaec.domain.repository.WeeksRepository
import com.jotadev.aiapaec.domain.usecases.CheckAuthStatusUseCase
import com.jotadev.aiapaec.domain.usecases.GetCurrentUserUseCase
import com.jotadev.aiapaec.domain.usecases.GetStudentsUseCase
// Eliminado GetClassesUseCase
import com.jotadev.aiapaec.domain.usecases.GetGradesByBranchUseCase
import com.jotadev.aiapaec.domain.usecases.GetBimestersUseCase
import com.jotadev.aiapaec.domain.usecases.GetQuizzesUseCase
import com.jotadev.aiapaec.domain.usecases.GetUnitsUseCase
import com.jotadev.aiapaec.domain.usecases.GetWeeksUseCase
import com.jotadev.aiapaec.domain.usecases.CreateQuizUseCase
import com.jotadev.aiapaec.domain.usecases.UpdateQuizUseCase
import com.jotadev.aiapaec.domain.usecases.DeleteQuizUseCase
import com.jotadev.aiapaec.domain.usecases.LoginUseCase
import com.jotadev.aiapaec.domain.usecases.LogoutUseCase
import com.jotadev.aiapaec.domain.usecases.UploadAnswerKeyUseCase
import com.jotadev.aiapaec.domain.usecases.ListAnswerKeysUseCase
import com.jotadev.aiapaec.ui.screens.students.StudentsViewModel
// Eliminado ClassesViewModel
import com.jotadev.aiapaec.presentation.BimestersViewModel
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
    // @Singleton
    fun provideStudentRepository(): StudentRepository {
        return StudentRepositoryImpl()
    }
    
    // Eliminado provideClassesRepository

    fun provideGradesRepository(): GradesRepository {
        return GradesRepositoryImpl()
    }
    
    // @Provides
    // @Singleton
    fun provideBimestersRepository(): BimestersRepository {
        return BimestersRepositoryImpl(RetrofitClient.apiService)
    }

    // @Provides
    // @Singleton
    fun provideUnitsRepository(): UnitsRepository {
        return UnitsRepositoryImpl(RetrofitClient.apiService)
    }

    // @Provides
    // @Singleton
    fun provideWeeksRepository(): WeeksRepository {
        return WeeksRepositoryImpl(RetrofitClient.apiService)
    }

    // Quizzes
    fun provideQuizzesRepository(): QuizzesRepository {
        return QuizzesRepositoryImpl()
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
    
    // @Provides
    fun provideGetStudentsUseCase(studentRepository: StudentRepository): GetStudentsUseCase {
        return GetStudentsUseCase(studentRepository)
    }
    
    // Eliminado provideGetClassesUseCase

    fun provideGetGradesByBranchUseCase(gradesRepository: GradesRepository): GetGradesByBranchUseCase {
        return GetGradesByBranchUseCase(gradesRepository)
    }
    
    // @Provides
    fun provideGetBimestersUseCase(bimestersRepository: BimestersRepository): GetBimestersUseCase {
        return GetBimestersUseCase(bimestersRepository)
    }

    fun provideGetUnitsUseCase(unitsRepository: UnitsRepository): GetUnitsUseCase {
        return GetUnitsUseCase(unitsRepository)
    }

    fun provideGetWeeksUseCase(weeksRepository: WeeksRepository): GetWeeksUseCase {
        return GetWeeksUseCase(weeksRepository)
    }

    fun provideGetQuizzesUseCase(quizzesRepository: QuizzesRepository): GetQuizzesUseCase {
        return GetQuizzesUseCase(quizzesRepository)
    }

    fun provideCreateQuizUseCase(quizzesRepository: QuizzesRepository): CreateQuizUseCase {
        return CreateQuizUseCase(quizzesRepository)
    }

    fun provideUpdateQuizUseCase(quizzesRepository: QuizzesRepository): UpdateQuizUseCase {
        return UpdateQuizUseCase(quizzesRepository)
    }

    fun provideDeleteQuizUseCase(quizzesRepository: QuizzesRepository): DeleteQuizUseCase {
        return DeleteQuizUseCase(quizzesRepository)
    }

    fun provideUploadAnswerKeyUseCase(quizzesRepository: QuizzesRepository): UploadAnswerKeyUseCase {
        return UploadAnswerKeyUseCase(quizzesRepository)
    }

    fun provideListAnswerKeysUseCase(quizzesRepository: QuizzesRepository): ListAnswerKeysUseCase {
        return ListAnswerKeysUseCase(quizzesRepository)
    }
    
    // @Provides
    fun provideStudentsViewModel(getStudentsUseCase: GetStudentsUseCase): StudentsViewModel {
        return StudentsViewModel(getStudentsUseCase)
    }
    
    // Eliminado provideClassesViewModel
}
    
    // @Provides
    fun provideBimestersViewModel(): BimestersViewModel {
        return BimestersViewModel()
    }

