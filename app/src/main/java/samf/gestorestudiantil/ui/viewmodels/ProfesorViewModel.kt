package samf.gestorestudiantil.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.auth.oauth2.GoogleCredentials
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Entrega
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.Post
import samf.gestorestudiantil.data.models.Tarea
import samf.gestorestudiantil.data.models.Unidad
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.ProfesorRepository
import samf.gestorestudiantil.domain.repositories.TareaRepository
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class ProfesorState(
    val isLoading: Boolean = false,
    val asignaturas: List<Asignatura> = emptyList(),
    val todosMisEstudiantes: List<User> = emptyList(),
    val estudiantes: List<User> = emptyList(),
    val evaluaciones: List<Evaluacion> = emptyList(),
    val unidades: List<Unidad> = emptyList(),
    val posts: List<Post> = emptyList(),
    val tareas: List<Tarea> = emptyList(),
    val horarios: List<Horario> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ProfesorViewModel @Inject constructor(
    private val profesorRepository: ProfesorRepository,
    private val tareaRepository: TareaRepository,
    private val db: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ProfesorState())
    val state: StateFlow<ProfesorState> = _state.asStateFlow()

    private val _profesor = MutableStateFlow<User?>(null)
    val profesor: StateFlow<User?> = _profesor.asStateFlow()

    fun cargarProfesor(profesorId: String) {
        if (profesorId.isEmpty()) {
            _profesor.value = null
            return
        }
        viewModelScope.launch {
            try {
                val user = profesorRepository.getProfesor(profesorId)
                _profesor.value = user
            } catch (e: Exception) {
                _profesor.value = null
            }
        }
    }

    // ====================================================================
    // 0. GESTIÓN DE UNIDADES, POSTS Y TAREAS (PROFESORES)
    // ====================================================================
    fun cargarContenidoAsignatura(asignaturaId: String) {
        viewModelScope.launch {
            launch {
                profesorRepository.getUnidades(asignaturaId).collect { lista ->
                    _state.update { it.copy(unidades = lista) }
                }
            }
            launch {
                profesorRepository.getPosts(asignaturaId).collect { lista ->
                    _state.update { it.copy(posts = lista) }
                }
            }
            launch {
                tareaRepository.getTareasPorAsignatura(asignaturaId).collect { lista ->
                    _state.update { it.copy(tareas = lista) }
                }
            }
        }
    }

    fun crearUnidad(asignaturaId: String, nombre: String, descripcion: String, visible: Boolean) {
        viewModelScope.launch {
            try {
                val nuevaUnidad = Unidad(
                    asignaturaId = asignaturaId,
                    nombre = nombre,
                    descripcion = descripcion,
                    visible = visible,
                    orden = (_state.value.unidades.maxOfOrNull { it.orden } ?: 0) + 1
                )
                profesorRepository.crearUnidad(nuevaUnidad)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Unidad creada con éxito", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al crear unidad: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun editarUnidad(unidadId: String, nombre: String, descripcion: String, visible: Boolean) {
        viewModelScope.launch {
            try {
                profesorRepository.editarUnidad(unidadId, nombre, descripcion, visible)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Unidad actualizada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al editar unidad: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun eliminarUnidad(unidadId: String) {
        viewModelScope.launch {
            try {
                profesorRepository.eliminarUnidad(unidadId)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Unidad eliminada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al eliminar unidad: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun crearPost(asignaturaId: String, unidadId: String, titulo: String, contenido: String, autorId: String, autorNombre: String, visible: Boolean) {
        viewModelScope.launch {
            try {
                val nuevoPost = Post(
                    asignaturaId = asignaturaId,
                    unidadId = unidadId,
                    titulo = titulo,
                    contenido = contenido,
                    autorId = autorId,
                    autorNombre = autorNombre,
                    fechaCreacion = System.currentTimeMillis(),
                    visible = visible
                )
                profesorRepository.crearPost(nuevoPost)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Post publicado", Toast.LENGTH_SHORT).show()
                }
                if (visible) {
                    val asignatura = _state.value.asignaturas.find { it.id == asignaturaId }
                    enviarNotificacion(asignaturaId, titulo, autorNombre, asignatura?.acronimo ?: "")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al crear post: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun enviarNotificacion(asignaturaId: String, tituloPost: String, nombreProfesor: String, acronimoAsignatura: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val topic = "asignatura_${asignaturaId}_estudiantes"
                val accessToken = getAccessToken(context) ?: return@launch

                val client = OkHttpClient()
                val notificationTitle = "Nuevo post de $nombreProfesor en $acronimoAsignatura"
                
                val json = JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("topic", topic)
                        put("notification", JSONObject().apply {
                            put("title", notificationTitle)
                            put("body", tituloPost)
                        })
                        put("data", JSONObject().apply {
                            put("target_asignatura_id", asignaturaId)
                        })
                    })
                }

                val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url("https://fcm.googleapis.com/v1/projects/gestorinstituto-tfg/messages:send")
                    .post(body)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        println("Error enviando notificación: ${response.body?.string()}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getAccessToken(context: Context): String? {
        return try {
            val inputStream = context.assets.open("service-account.json")
            val googleCredentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            googleCredentials.refreshIfExpired()
            googleCredentials.accessToken.tokenValue
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun editarPost(postId: String, titulo: String, contenido: String, visible: Boolean) {
        viewModelScope.launch {
            try {
                profesorRepository.editarPost(postId, titulo, contenido, visible)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Post actualizado", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al editar post: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun eliminarPost(postId: String) {
        viewModelScope.launch {
            try {
                profesorRepository.eliminarPost(postId)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Post eliminado", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al eliminar post: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ====================================================================
    // 0.1 GESTIÓN DE TAREAS (Hybrid Firebase + Supabase)
    // ====================================================================
    fun crearTarea(tarea: Tarea, fileData: ByteArray?, fileName: String?, mimeType: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                tareaRepository.crearTarea(tarea, fileData, fileName, mimeType)
                
                // Buscar el acrónimo de la asignatura para la notificación
                val asignatura = _state.value.asignaturas.find { it.id == tarea.asignaturaId }
                val acronimo = asignatura?.acronimo ?: ""
                val nombreProf = _profesor.value?.nombre ?: "El Profesor"
                
                enviarNotificacion(tarea.asignaturaId, "Nueva Tarea: ${tarea.titulo}", nombreProf, acronimo)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Tarea creada con éxito", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al crear tarea: ${e.localizedMessage}") }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al crear tarea: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun editarTarea(tarea: Tarea, fileData: ByteArray?, fileName: String?, mimeType: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                tareaRepository.editarTarea(tarea, fileData, fileName, mimeType)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Tarea actualizada con éxito", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al editar tarea: ${e.localizedMessage}") }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al editar tarea: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun eliminarTarea(tarea: Tarea) {
        viewModelScope.launch {
            try {
                tareaRepository.eliminarTarea(tarea)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al eliminar tarea: ${e.localizedMessage}") }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al eliminar tarea: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val _entregas = MutableStateFlow<List<Entrega>>(emptyList())
    val entregas = _entregas.asStateFlow()

    fun cargarEntregas(tareaId: String) {
        viewModelScope.launch {
            tareaRepository.getEntregasPorTarea(tareaId).collect { lista ->
                _entregas.value = lista
            }
        }
    }

    fun calificarEntrega(entregaId: String, nota: Float, comentario: String?) {
        viewModelScope.launch {
            try {
                val entregaRef = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("entregas").document(entregaId)
                
                entregaRef.update(
                    "calificacion", nota,
                    "comentarioProfesor", comentario
                ).await()
                
                Toast.makeText(context, "Calificación guardada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al calificar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun descargarArchivo(supabasePath: String, nombreArchivo: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val bytes = tareaRepository.descargarArchivo(supabasePath)
                val file = File(context.cacheDir, nombreArchivo)
                FileOutputStream(file).use { it.write(bytes) }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                val mimeType = context.contentResolver.getType(uri) ?: when (file.extension.lowercase()) {
                    "pdf" -> "application/pdf"
                    "doc", "docx" -> "application/msword"
                    "jpg", "jpeg" -> "image/jpeg"
                    "png" -> "image/png"
                    else -> "*/*"
                }

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val chooser = Intent.createChooser(intent, "Abrir con...")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al abrir: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    // ====================================================================
    // 1. ASIGNATURAS QUE IMPARTE EL PROFESOR (tiempo real)
    // ====================================================================
    private var entregasJob: Job? = null
    private var recalcularJob: Job? = null
    private var usuarioJob: Job? = null
    private var currentUltimaVez: Map<String, Long> = emptyMap()

    fun cargarAsignaturas(profesorId: String, ultimaVez: Map<String, Long> = emptyMap()) {
        currentUltimaVez = ultimaVez
        observarUsuario(profesorId)
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            profesorRepository.getAsignaturas(profesorId).collect { lista ->
                _state.update { it.copy(isLoading = false, asignaturas = lista) }
                // Suscribir al profesor a sus asignaturas para recibir entregas
                lista.forEach { asignatura ->
                    FirebaseMessaging.getInstance().subscribeToTopic("asignatura_${asignatura.id}_profesores")
                }
                // Al cargar asignaturas, necesitamos conocer a todos los estudiantes de esos cursos para la pestaña global
                cargarTodosMisEstudiantes(lista)
                
                observarCambiosEnEntregas(lista.map { it.id })
                recalcularNotificaciones(lista, currentUltimaVez)
            }
        }
    }

    private fun observarUsuario(usuarioId: String) {
        usuarioJob?.cancel()
        usuarioJob = viewModelScope.launch {
            db.collection("usuarios").document(usuarioId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val user = snapshot?.toObject(User::class.java)
                    if (user != null) {
                        actualizarTiemposLectura(user.ultimaVezAsignaturas)
                    }
                }
        }
    }

    private fun observarCambiosEnEntregas(asignaturaIds: List<String>) {
        entregasJob?.cancel()
        entregasJob = viewModelScope.launch {
            profesorRepository.observeEntregasChanges(asignaturaIds).collect {
                recalcularNotificaciones(_state.value.asignaturas, currentUltimaVez)
            }
        }
    }

    fun actualizarTiemposLectura(map: Map<String, Long>) {
        if (currentUltimaVez == map) return
        currentUltimaVez = map
        recalcularNotificaciones(_state.value.asignaturas, map)
    }

    private fun recalcularNotificaciones(asignaturas: List<Asignatura>, ultimaVez: Map<String, Long>) {
        recalcularJob?.cancel()
        recalcularJob = viewModelScope.launch {
            try {
                // Usamos coroutineScope para lanzar las tareas en paralelo de forma estructurada
                val nuevasAsignaturas = coroutineScope {
                    asignaturas.map { asignatura ->
                        async {
                            val lastRead = ultimaVez[asignatura.id] ?: 0L
                            val count = profesorRepository.getCountNuevasEntregas(asignatura.id, lastRead)
                            asignatura.copy(numNotificaciones = count)
                        }
                    }.map { it.await() }
                }
                _state.update { it.copy(asignaturas = nuevasAsignaturas) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun marcarAsignaturaComoLeida(usuarioId: String, asignaturaId: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            try {
                // 1. Actualización local inmediata del mapa de tiempos
                val nuevasUltimaVez = currentUltimaVez.toMutableMap().apply { put(asignaturaId, now) }
                currentUltimaVez = nuevasUltimaVez

                // 2. Forzamos el badge a 0 localmente para feedback instantáneo
                _state.update { s ->
                    val asignaturasActuales = s.asignaturas.map {
                        if (it.id == asignaturaId) it.copy(numNotificaciones = 0) else it
                    }
                    s.copy(asignaturas = asignaturasActuales)
                }

                // 3. Persistimos en Firestore
                db.collection("usuarios").document(usuarioId)
                    .update("ultimaVezAsignaturas.$asignaturaId", now).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ====================================================================
    // 2. TODOS LOS ESTUDIANTES DE LOS CURSOS DONDE IMPARTE (tiempo real)
    // ====================================================================
    fun cargarTodosMisEstudiantes(asignaturas: List<Asignatura>) {
        if (asignaturas.isEmpty()) {
            _state.update { it.copy(todosMisEstudiantes = emptyList()) }
            return
        }
        viewModelScope.launch {
            // Recolectamos estudiantes de todas las asignaturas y los combinamos
            val flows = asignaturas.map { profesorRepository.getEstudiantesPorAsignatura(it) }
            combine(flows) { arrays: Array<List<User>> ->
                arrays.flatMap { it }.distinctBy { it.id }
            }.collect { lista ->
                _state.update { it.copy(todosMisEstudiantes = lista) }
            }
        }
    }

    // ====================================================================
    // 3. ESTUDIANTES DE UNA ASIGNATURA CONCRETA (Ciclo y Turno específicos)
    // ====================================================================
    fun cargarEstudiantesPorAsignatura(asignatura: Asignatura) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                profesorRepository.getEstudiantesPorAsignatura(asignatura).collect { lista ->
                    _state.update { it.copy(isLoading = false, estudiantes = lista) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }
        }
    }

    // ====================================================================
    // 4. EVALUACIONES DE UN ALUMNO EN UNA ASIGNATURA (tiempo real)
    // ====================================================================
    fun cargarEvaluacionesEstudiante(estudianteId: String, asignaturaId: String) {
        viewModelScope.launch {
            profesorRepository.getEvaluacionesEstudiante(estudianteId, asignaturaId).collect { lista ->
                _state.update { it.copy(evaluaciones = lista) }
            }
        }
    }

    // ====================================================================
    // 5. GESTIÓN DE EVALUACIONES (Persistencia)
    // ====================================================================
    fun guardarEvaluacion(evaluacion: Evaluacion) {
        viewModelScope.launch {
            try {
                profesorRepository.guardarEvaluacion(evaluacion)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Evaluación guardada", Toast.LENGTH_SHORT).show()
                }
                // Enviar notificación al estudiante
                enviarNotificacionCalificacion(evaluacion)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al guardar: ${e.localizedMessage}") }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al guardar evaluación: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun enviarNotificacionCalificacion(evaluacion: Evaluacion) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val estudiante = profesorRepository.getEstudiante(evaluacion.estudianteId)
                val token = estudiante?.fcmToken
                if (token.isNullOrEmpty()) return@launch

                val asignatura = _state.value.asignaturas.find { it.id == evaluacion.asignaturaId }
                val acronimo = asignatura?.acronimo ?: ""
                val accessToken = getAccessToken(context) ?: return@launch

                val client = OkHttpClient()
                val notificationTitle = "Nueva calificación en $acronimo"
                val notificationBody = "Se ha publicado la nota de: ${evaluacion.nombre}"

                val json = JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("token", token)
                        put("notification", JSONObject().apply {
                            put("title", notificationTitle)
                            put("body", notificationBody)
                        })
                        put("data", JSONObject().apply {
                            put("target_asignatura_id", evaluacion.asignaturaId)
                            put("type", "calificacion")
                        })
                    })
                }

                val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url("https://fcm.googleapis.com/v1/projects/gestorinstituto-tfg/messages:send")
                    .post(body)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        println("Error enviando notificación individual: ${response.body?.string()}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarEvaluacion(evaluacion: Evaluacion) {
        viewModelScope.launch {
            try {
                profesorRepository.eliminarEvaluacion(evaluacion.id)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Evaluación eliminada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al eliminar: ${e.localizedMessage}") }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al eliminar evaluación: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ====================================================================
    // 6. HORARIOS DEL PROFESOR (tiempo real)
    // ====================================================================
    fun cargarHorariosProfesor(profesorId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            profesorRepository.getHorarios(profesorId).collect { lista ->
                _state.update { it.copy(isLoading = false, horarios = lista) }
            }
        }
    }
}
