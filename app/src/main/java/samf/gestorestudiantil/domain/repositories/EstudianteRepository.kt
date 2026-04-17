package samf.gestorestudiantil.domain.repositories

import kotlinx.coroutines.flow.Flow
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario

interface EstudianteRepository {
    fun getAsignaturas(cursoId: String, turno: String, cicloNum: Int): Flow<List<Asignatura>>
    fun observePostsAndTareasChanges(asignaturaIds: List<String>): Flow<Unit>
    suspend fun getCountNuevosPosts(asignaturaId: String, lastRead: Long): Int
    suspend fun getCountNuevasTareas(asignaturaId: String, lastRead: Long): Int
    suspend fun marcarAsignaturaLeida(usuarioId: String, asignaturaId: String, timestamp: Long)
    suspend fun getEvaluaciones(asignaturaId: String, estudianteId: String): List<Evaluacion>
    fun getHorarios(cursoId: String, turno: String, cicloNum: Int): Flow<List<Horario>>
}
