package samf.gestorestudiantil.domain.repositories

import kotlinx.coroutines.flow.Flow
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Clase
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.User

interface AdminRepository {
    fun getUsuariosPorCentro(centroId: String): Flow<List<User>>
    suspend fun aprobarUsuario(usuarioId: String)
    suspend fun eliminarUsuario(usuarioId: String)
    suspend fun actualizarDatosUsuario(usuarioId: String, updates: Map<String, Any?>)
    
    fun getCentros(): Flow<List<Centro>>
    fun getClasesPorCentro(centroId: String): Flow<List<Clase>>
    fun getCursosPorCentro(centroId: String): Flow<List<Curso>>
    fun getAsignaturasSinProfesor(turno: String): Flow<List<Asignatura>>
    fun getAsignaturasPorCurso(cursoId: String, turno: String): Flow<List<Asignatura>>
    fun getHorarios(cursoId: String, cicloNum: Int, turno: String): Flow<List<Horario>>

    suspend fun asignarAsignaturaAProfesor(asignaturaId: String, profesorId: String)
    suspend fun desasignarAsignatura(asignaturaId: String, profesorId: String)
    suspend fun asignarTutorAClase(claseId: String, tutorId: String)
    
    suspend fun guardarCentro(centro: Centro)
    suspend fun eliminarCentro(centroId: String)
    suspend fun guardarCurso(curso: Curso)
    suspend fun eliminarCurso(cursoId: String)
    suspend fun guardarAsignatura(asignatura: Asignatura)
    suspend fun eliminarAsignatura(asignaturaId: String)
    suspend fun guardarHorario(horario: Horario)
    suspend fun eliminarHorario(horarioId: String)
    
    suspend fun seedDatabase(jsonlLines: List<String>)
    suspend fun recalcularTodosLosContadores()
    suspend fun generarClasesPorDefecto(centroId: String)
}
