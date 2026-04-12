package samf.gestorestudiantil.domain.repositories

import kotlinx.coroutines.flow.Flow
import samf.gestorestudiantil.data.models.Entrega
import samf.gestorestudiantil.data.models.Tarea

interface TareaRepository {
    // Tareas
    fun getTareasPorUnidad(unidadId: String): Flow<List<Tarea>>
    fun getTareasPorAsignatura(asignaturaId: String): Flow<List<Tarea>>
    suspend fun crearTarea(tarea: Tarea, fileData: ByteArray?, fileName: String?, mimeType: String? = null): String
    suspend fun editarTarea(tarea: Tarea, fileData: ByteArray?, fileName: String?, mimeType: String? = null): String
    suspend fun eliminarTarea(tarea: Tarea)

    // Entregas
    fun getEntregasPorTarea(tareaId: String): Flow<List<Entrega>>
    fun getEntregaEstudiante(tareaId: String, estudianteId: String): Flow<Entrega?>
    suspend fun realizarEntrega(entrega: Entrega, fileData: ByteArray, fileName: String, mimeType: String? = null)
    suspend fun eliminarEntrega(entrega: Entrega)

    // Supabase URL
    suspend fun getUrlFirmada(supabasePath: String): String
}
