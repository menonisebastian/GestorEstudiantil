package samf.gestorestudiantil.domain.repositories

import kotlinx.coroutines.flow.Flow
import samf.gestorestudiantil.data.models.Asistencia

interface AsistenciaRepository {
    fun getAsistenciasPorDia(asignaturaId: String, fecha: Long): Flow<List<Asistencia>>
    fun getAsistenciasPorEstudiante(estudianteId: String): Flow<List<Asistencia>>
    fun getDiasConAsistencia(asignaturaId: String): Flow<List<Long>>
    suspend fun guardarAsistencias(asistencias: List<Asistencia>)
    suspend fun eliminarAsistencia(asistenciaId: String)
}
