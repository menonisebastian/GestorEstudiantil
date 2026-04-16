package samf.gestorestudiantil.domain.repositories

import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.User

interface CourseRepository {
    suspend fun incrementStudentCount(cursoId: String, turno: String, ciclo: Int)
    suspend fun decrementStudentCount(cursoId: String, turno: String, ciclo: Int)
    suspend fun getCentros(): List<Centro>
    suspend fun getCursosPorCentro(centroId: String): List<Curso>
    suspend fun getEstudiante(userId: String): User.Estudiante?
}
