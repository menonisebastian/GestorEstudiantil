package samf.gestorestudiantil.domain.usecases

import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.domain.repositories.EstudianteRepository
import javax.inject.Inject

class CalculateUnreadNotificationsUseCase @Inject constructor(
    private val estudianteRepository: EstudianteRepository
) {
    suspend operator fun invoke(
        asignaturas: List<Asignatura>,
        ultimaVezAsignaturas: Map<String, Long>
    ): List<Asignatura> {
        return asignaturas.map { asignatura ->
            val lastRead = ultimaVezAsignaturas[asignatura.id] ?: 0L
            try {
                val numNotifs = estudianteRepository.getCountNuevosPosts(asignatura.id, lastRead)
                asignatura.copy(numNotificaciones = numNotifs)
            } catch (e: Exception) {
                asignatura
            }
        }
    }
}
