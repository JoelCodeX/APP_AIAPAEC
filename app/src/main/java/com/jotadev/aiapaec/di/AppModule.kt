package com.jotadev.aiapaec.di

import com.jotadev.aiapaec.data.repository.AuthRepositoryImpl
import com.jotadev.aiapaec.data.repository.UserRepositoryImpl
import com.jotadev.aiapaec.data.repository.StudentRepositoryImpl
import com.jotadev.aiapaec.data.repository.ClassesRepositoryImpl
import com.jotadev.aiapaec.data.repository.BimestersRepositoryImpl
import com.jotadev.aiapaec.data.repository.QuizzesRepositoryImpl
import com.jotadev.aiapaec.data.api.RetrofitClient
import com.jotadev.aiapaec.domain.repository.AuthRepository
import com.jotadev.aiapaec.domain.repository.UserRepository
import com.jotadev.aiapaec.domain.repository.StudentRepository
import com.jotadev.aiapaec.domain.repository.ClassesRepository
import com.jotadev.aiapaec.domain.repository.BimestersRepository
import com.jotadev.aiapaec.domain.repository.QuizzesRepository
import com.jotadev.aiapaec.domain.usecases.CheckAuthStatusUseCase
import com.jotadev.aiapaec.domain.usecases.GetCurrentUserUseCase
import com.jotadev.aiapaec.domain.usecases.GetStudentsUseCase
import com.jotadev.aiapaec.domain.usecases.GetClassesUseCase
import com.jotadev.aiapaec.domain.usecases.GetBimestersUseCase
import com.jotadev.aiapaec.domain.usecases.GetQuizzesUseCase
import com.jotadev.aiapaec.domain.usecases.CreateQuizUseCase
import com.jotadev.aiapaec.domain.usecases.UpdateQuizUseCase
import com.jotadev.aiapaec.domain.usecases.DeleteQuizUseCase
import com.jotadev.aiapaec.domain.usecases.LoginUseCase
import com.jotadev.aiapaec.domain.usecases.LogoutUseCase
import com.jotadev.aiapaec.domain.usecases.UploadAnswerKeyUseCase
import com.jotadev.aiapaec.domain.usecases.ListAnswerKeysUseCase
import com.jotadev.aiapaec.ui.screens.students.StudentsViewModel
import com.jotadev.aiapaec.ui.screens.classes.ClassesViewModel
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
    
    // @Provides
    // @Singleton
    fun provideClassesRepository(): ClassesRepository {
        return ClassesRepositoryImpl()
    }
    
    // @Provides
    // @Singleton
    fun provideBimestersRepository(): BimestersRepository {
        return BimestersRepositoryImpl(RetrofitClient.apiService)
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
    
    // @Provides
    fun provideGetClassesUseCase(classesRepository: ClassesRepository): GetClassesUseCase {
        return GetClassesUseCase(classesRepository)
    }
    
    // @Provides
    fun provideGetBimestersUseCase(bimestersRepository: BimestersRepository): GetBimestersUseCase {
        return GetBimestersUseCase(bimestersRepository)
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
    
    // @Provides
    fun provideClassesViewModel(getClassesUseCase: GetClassesUseCase): ClassesViewModel {
        return ClassesViewModel(getClassesUseCase)
    }
    
    // @Provides
    fun provideBimestersViewModel(): BimestersViewModel {
        return BimestersViewModel()
    }
}