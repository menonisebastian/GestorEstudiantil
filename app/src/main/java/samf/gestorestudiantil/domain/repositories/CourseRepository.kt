package samf.gestorestudiantil.domain.repositories

import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso

interface CourseRepository {
    suspend fun incrementStudentCount(cursoId: String, turno: String, ciclo: Int)
    suspend fun getCentros(): List<Centro>
    suspend fun getCursosPorCentro(centroId: String): List<Curso>
}
