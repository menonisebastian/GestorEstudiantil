package samf.gestorestudiantil.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import samf.gestorestudiantil.data.repositories.AdminRepositoryImpl
import samf.gestorestudiantil.data.repositories.AuthRepositoryImpl
import samf.gestorestudiantil.data.repositories.CourseRepositoryImpl
import samf.gestorestudiantil.data.repositories.EstudianteRepositoryImpl
import samf.gestorestudiantil.data.repositories.NotificationRepositoryImpl
import samf.gestorestudiantil.data.repositories.ProfesorRepositoryImpl
import samf.gestorestudiantil.data.repositories.RecordatorioRepositoryImpl
import samf.gestorestudiantil.data.repositories.TareaRepositoryImpl
import samf.gestorestudiantil.data.repositories.UserRepositoryImpl
import samf.gestorestudiantil.domain.repositories.AdminRepository
import samf.gestorestudiantil.domain.repositories.AuthRepository
import samf.gestorestudiantil.domain.repositories.CourseRepository
import samf.gestorestudiantil.domain.repositories.EstudianteRepository
import samf.gestorestudiantil.domain.repositories.NotificationRepository
import samf.gestorestudiantil.domain.repositories.ProfesorRepository
import samf.gestorestudiantil.domain.repositories.RecordatorioRepository
import samf.gestorestudiantil.domain.repositories.TareaRepository
import samf.gestorestudiantil.domain.repositories.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindCourseRepository(
        courseRepositoryImpl: CourseRepositoryImpl
    ): CourseRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(
        adminRepositoryImpl: AdminRepositoryImpl
    ): AdminRepository

    @Binds
    @Singleton
    abstract fun bindEstudianteRepository(
        estudianteRepositoryImpl: EstudianteRepositoryImpl
    ): EstudianteRepository

    @Binds
    @Singleton
    abstract fun bindProfesorRepository(
        profesorRepositoryImpl: ProfesorRepositoryImpl
    ): ProfesorRepository

    @Binds
    @Singleton
    abstract fun bindRecordatorioRepository(
        recordatorioRepositoryImpl: RecordatorioRepositoryImpl
    ): RecordatorioRepository

    @Binds
    @Singleton
    abstract fun bindTareaRepository(
        tareaRepositoryImpl: TareaRepositoryImpl
    ): TareaRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository
}
