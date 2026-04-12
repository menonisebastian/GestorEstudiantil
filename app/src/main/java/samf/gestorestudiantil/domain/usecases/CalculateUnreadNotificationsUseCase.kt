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
                val numPosts = estudianteRepository.getCountNuevosPosts(asignatura.id, lastRead)
                val numTareas = estudianteRepository.getCountNuevasTareas(asignatura.id, lastRead)
                asignatura.copy(numNotificaciones = numPosts + numTareas)
            } catch (e: Exception) {
                asignatura
            }
        }
    }
}
