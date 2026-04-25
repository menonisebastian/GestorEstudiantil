package samf.gestorestudiantil.domain.repositories

import kotlinx.coroutines.flow.Flow
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.Post
import samf.gestorestudiantil.data.models.Unidad
import samf.gestorestudiantil.data.models.User

interface ProfesorRepository {
    fun getUnidades(asignaturaId: String): Flow<List<Unidad>>
    fun getPosts(asignaturaId: String): Flow<List<Post>>
    suspend fun crearUnidad(unidad: Unidad)
    suspend fun editarUnidad(unidadId: String, nombre: String, descripcion: String, visible: Boolean)
    suspend fun eliminarUnidad(unidadId: String)
    suspend fun crearPost(post: Post)
    suspend fun editarPost(postId: String, titulo: String, contenido: String, visible: Boolean)
    suspend fun eliminarPost(postId: String)
    
    fun getAsignaturas(profesorId: String): Flow<List<Asignatura>>
    fun observeEntregasChanges(asignaturaIds: List<String>): Flow<Unit>
    suspend fun getCountNuevasEntregas(asignaturaId: String, lastRead: Long): Int
    fun getEstudiantesPorAsignatura(asignatura: Asignatura): Flow<List<User>>
    fun getEstudiantesPorCursos(cursoIds: List<String>): Flow<List<User>>
    suspend fun getEstudiantesEspecificos(cursoId: String, cicloNum: Int, turno: String): List<User>
    
    fun getEvaluacionesEstudiante(estudianteId: String, asignaturaId: String): Flow<List<Evaluacion>>
    suspend fun guardarEvaluacion(evaluacion: Evaluacion)
    suspend fun eliminarEvaluacion(evaluacionId: String)
    
    fun getHorarios(profesorId: String): Flow<List<Horario>>
    suspend fun getProfesor(profesorId: String): User?
    suspend fun getEstudiante(estudianteId: String): User?
    suspend fun marcarAsignaturaLeida(usuarioId: String, asignaturaId: String, timestamp: Long)
}
