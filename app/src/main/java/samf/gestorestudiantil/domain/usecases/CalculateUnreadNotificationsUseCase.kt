package samf.gestorestudiantil.domain.usecases

import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.domain.repositories.EstudianteRepository
import javax.inject.Inject

class CalculateUnreadNotificationsUseCase @Inject constructor(
    private val estudianteRepository: EstudianteRepository
) {
    suspend operator fun invoke(
        asignaturas: List<Asignatura>,
        ultimaVezAsignaturas: Map<String, Long>
    ): List<Asignatura> = coroutineScope {
        asignaturas.map { asignatura ->
            async {
                val realId = asignatura.id.ifEmpty { asignatura.idDocumento }
                val lastRead = ultimaVezAsignaturas[realId] ?: 0L
                try {
                    val numPosts = estudianteRepository.getCountNuevosPosts(realId, lastRead)
                    val numTareas = estudianteRepository.getCountNuevasTareas(realId, lastRead)
                    asignatura.copy(numNotificaciones = numPosts + numTareas)
                } catch (e: Exception) {
                    Log.e("Notificaciones", "Error en $realId: ${e.message}")
                    asignatura
                }
            }
        }.awaitAll()
    }
}
