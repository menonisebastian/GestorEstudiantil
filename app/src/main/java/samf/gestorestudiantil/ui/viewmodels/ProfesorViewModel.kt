package samf.gestorestudiantil.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Entrega
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.Post
import samf.gestorestudiantil.data.models.Tarea
import samf.gestorestudiantil.data.models.Unidad
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.NotificationRepository
import samf.gestorestudiantil.domain.repositories.ProfesorRepository
import samf.gestorestudiantil.domain.repositories.TareaRepository
import samf.gestorestudiantil.domain.repositories.UserRepository
import samf.gestorestudiantil.ui.utils.FileOpener
import samf.gestorestudiantil.R
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
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
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
    private var contenidoJob: Job? = null

    fun cargarContenidoAsignatura(asignaturaId: String) {
        contenidoJob?.cancel()
        contenidoJob = viewModelScope.launch {
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
                    Toast.makeText(context, R.string.success_unit_created, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, R.string.error_save_item, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun editarUnidad(unidadId: String, nombre: String, descripcion: String, visible: Boolean) {
        viewModelScope.launch {
            try {
                profesorRepository.editarUnidad(unidadId, nombre, descripcion, visible)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, R.string.success_update, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, R.string.error_save_item, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun eliminarUnidad(unidad: Unidad, onUndo: () -> Unit) {
        viewModelScope.launch {
            try {
                profesorRepository.eliminarUnidad(unidad.id)
                onUndo()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, R.string.error_delete_item, Toast.LENGTH_LONG).show()
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
                    Toast.makeText(context, context.getString(R.string.success_post_published), Toast.LENGTH_SHORT).show()
                }
                if (visible) {
                    val asignatura = _state.value.asignaturas.find { it.id == asignaturaId }
                    enviarNotificacion(asignaturaId, titulo, autorNombre, asignatura?.acronimo ?: "")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.error_save_item), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun enviarNotificacion(asignaturaId: String, tituloPost: String, nombreProfesor: String, acronimoAsignatura: String) {
        viewModelScope.launch {
            try {
                val topic = "asignatura_${asignaturaId}_estudiantes"
                val title = "Nuevo post en $acronimoAsignatura"
                val body = tituloPost
                val data = mapOf("target_asignatura_id" to asignaturaId)

                notificationRepository.sendTopicNotification(topic, title, body, data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun editarPost(postId: String, titulo: String, contenido: String, visible: Boolean) {
        viewModelScope.launch {
            try {
                profesorRepository.editarPost(postId, titulo, contenido, visible)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.success_update), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.error_save_item), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun eliminarPost(post: Post, onUndo: () -> Unit) {
        viewModelScope.launch {
            try {
                profesorRepository.eliminarPost(post.id)
                onUndo()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.error_delete_item), Toast.LENGTH_LONG).show()
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
                
                val asignatura = _state.value.asignaturas.find { it.id == tarea.asignaturaId }
                val acronimo = asignatura?.acronimo ?: ""
                val nombreProf = _profesor.value?.nombre ?: "El Profesor"
                
                enviarNotificacion(tarea.asignaturaId, "Nueva Tarea: ${tarea.titulo}", nombreProf, acronimo)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.success_task_created), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = context.getString(R.string.error_save_item)) }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.error_save_item), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(context, context.getString(R.string.success_update), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = context.getString(R.string.error_save_item)) }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.error_save_item), Toast.LENGTH_LONG).show()
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun eliminarTarea(tarea: Tarea, onUndo: () -> Unit) {
        viewModelScope.launch {
            try {
                tareaRepository.eliminarTarea(tarea)
                onUndo()
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = context.getString(R.string.error_delete_item)) }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.error_delete_item), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val _entregas = MutableStateFlow<List<Entrega>>(emptyList())
    val entregas = _entregas.asStateFlow()

    private var entregasPorTareaJob: Job? = null

    fun cargarEntregas(tareaId: String) {
        entregasPorTareaJob?.cancel()
        entregasPorTareaJob = viewModelScope.launch {
            tareaRepository.getEntregasPorTarea(tareaId).collect { lista ->
                _entregas.value = lista
            }
        }
    }

    fun calificarEntrega(entregaId: String, nota: Float, comentario: String?) {
        viewModelScope.launch {
            try {
                tareaRepository.calificarEntrega(entregaId, nota, comentario)
                Toast.makeText(context, context.getString(R.string.success_save), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.error_save_item), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun descargarArchivo(supabasePath: String, nombreArchivo: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val bytes = tareaRepository.descargarArchivo(supabasePath)
                FileOpener.openFile(context, bytes, nombreArchivo)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.error_file_read), Toast.LENGTH_SHORT).show()
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
    private var tareasJob: Job? = null
    private var currentUltimaVez: Map<String, Long> = emptyMap()
    private var lastAsignaturasParams: String? = null
    private var lastHorariosParams: String? = null

    fun cargarAsignaturas(profesorId: String, ultimaVez: Map<String, Long> = emptyMap()) {
        if (profesorId == lastAsignaturasParams) return
        lastAsignaturasParams = profesorId

        currentUltimaVez = ultimaVez
        observarUsuario(profesorId)
        if (_state.value.asignaturas.isEmpty()) {
            _state.update { it.copy(isLoading = true) }
        }
        viewModelScope.launch {
            profesorRepository.getAsignaturas(profesorId).collect { lista ->
                _state.update { it.copy(isLoading = false, asignaturas = lista) }
                val ids = lista.map { it.id }
                lista.forEach { asignatura ->
                    FirebaseMessaging.getInstance().subscribeToTopic("asignatura_${asignatura.id}_profesores")
                }
                cargarTodosMisEstudiantes(lista)
                
                observarCambiosEnEntregas(ids)
                observarTodasLasTareas(ids)
                recalcularNotificaciones(lista, currentUltimaVez)
            }
        }
    }

    private fun observarTodasLasTareas(asignaturaIds: List<String>) {
        tareasJob?.cancel()
        tareasJob = viewModelScope.launch {
            tareaRepository.getTareasPorAsignaturas(asignaturaIds).collect { tareas ->
                _state.update { it.copy(tareas = tareas) }
            }
        }
    }

    private fun observarUsuario(usuarioId: String) {
        usuarioJob?.cancel()
        usuarioJob = viewModelScope.launch {
            userRepository.getUserFlow(usuarioId).collect { user ->
                if (user is User.Profesor) {
                    actualizarTiemposLectura(user.ultimaVezAsignaturas)
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observarCambiosEnEntregas(asignaturaIds: List<String>) {
        entregasJob?.cancel()
        entregasJob = viewModelScope.launch {
            profesorRepository.observeEntregasChanges(asignaturaIds)
                .debounce(300)
                .collect {
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
                // Procesar asignaturas en bloques de 5 para no saturar Firestore
                val todasProcesadas = mutableListOf<Asignatura>()
                asignaturas.chunked(5).forEach { bloque ->
                    val resultadosBloque = coroutineScope {
                        bloque.map { asignatura ->
                            async {
                                val lastRead = ultimaVez[asignatura.id] ?: 0L
                                val count = profesorRepository.getCountNuevasEntregas(asignatura.id, lastRead)
                                asignatura.copy(numNotificaciones = count)
                            }
                        }.map { it.await() }
                    }
                    todasProcesadas.addAll(resultadosBloque)
                }
                _state.update { it.copy(asignaturas = todasProcesadas) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun marcarAsignaturaComoLeida(usuarioId: String, asignaturaId: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            try {
                val nuevasUltimaVez = currentUltimaVez.toMutableMap().apply { put(asignaturaId, now) }
                currentUltimaVez = nuevasUltimaVez

                _state.update { s ->
                    val asignaturasActuales = s.asignaturas.map {
                        if (it.id == asignaturaId) it.copy(numNotificaciones = 0) else it
                    }
                    s.copy(asignaturas = asignaturasActuales)
                }

                profesorRepository.marcarAsignaturaLeida(usuarioId, asignaturaId, now)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ====================================================================
    // 2. TODOS LOS ESTUDIANTES DE LOS CURSOS DONDE IMPARTE (tiempo real)
    // ====================================================================
    private var misEstudiantesJob: Job? = null

    fun cargarTodosMisEstudiantes(asignaturas: List<Asignatura>) {
        if (asignaturas.isEmpty()) {
            _state.update { it.copy(todosMisEstudiantes = emptyList()) }
            return
        }
        misEstudiantesJob?.cancel()
        misEstudiantesJob = viewModelScope.launch {
            val cursoIds = asignaturas.map { it.cursoId }.distinct()
            profesorRepository.getEstudiantesPorCursos(cursoIds).collect { lista ->
                val filtrados = lista.filter { est ->
                    val estudiante = est as? User.Estudiante
                    if (estudiante != null) {
                        asignaturas.any { asig ->
                            asig.cursoId == estudiante.cursoId && asig.turno == estudiante.turno && asig.cicloNum == estudiante.cicloNum
                        }
                    } else false
                }.distinctBy { it.id }
                
                _state.update { it.copy(todosMisEstudiantes = filtrados) }
            }
        }
    }

    // ====================================================================
    // 3. ESTUDIANTES DE UNA ASIGNATURA CONCRETA (Ciclo y Turno específicos)
    // ====================================================================
    private var estudiantesAsigJob: Job? = null

    fun cargarEstudiantesPorAsignatura(asignatura: Asignatura) {
        _state.update { it.copy(isLoading = true) }
        estudiantesAsigJob?.cancel()
        estudiantesAsigJob = viewModelScope.launch {
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
    private var evaluacionesEstJob: Job? = null

    fun cargarEvaluacionesEstudiante(estudianteId: String, asignaturaId: String) {
        evaluacionesEstJob?.cancel()
        evaluacionesEstJob = viewModelScope.launch {
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
                    Toast.makeText(context, context.getString(R.string.success_save), Toast.LENGTH_SHORT).show()
                }
                enviarNotificacionCalificacion(evaluacion)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = context.getString(R.string.error_save_item)) }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.error_save_item), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun enviarNotificacionCalificacion(evaluacion: Evaluacion) {
        viewModelScope.launch {
            try {
                val estudiante = profesorRepository.getEstudiante(evaluacion.estudianteId)
                val token = estudiante?.fcmToken
                if (token.isNullOrEmpty()) return@launch

                val asignatura = _state.value.asignaturas.find { it.id == evaluacion.asignaturaId }
                val acronimo = asignatura?.acronimo ?: ""
                
                val title = "Nueva calificación en $acronimo"
                val body = "Se ha publicado la nota de: ${evaluacion.nombre}"
                val data = mapOf(
                    "target_asignatura_id" to evaluacion.asignaturaId,
                    "type" to "calificacion"
                )

                notificationRepository.sendTokenNotification(token, title, body, data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarEvaluacion(evaluacion: Evaluacion, onUndo: () -> Unit) {
        viewModelScope.launch {
            try {
                profesorRepository.eliminarEvaluacion(evaluacion.id)
                onUndo()
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = context.getString(R.string.error_delete_item)) }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.error_delete_item), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ====================================================================
    // 6. HORARIOS DEL PROFESOR (tiempo real)
    // ====================================================================
    fun cargarHorariosProfesor(profesorId: String) {
        if (profesorId == lastHorariosParams) return
        lastHorariosParams = profesorId

        if (_state.value.horarios.isEmpty()) {
            _state.update { it.copy(isLoading = true) }
        }
        viewModelScope.launch {
            profesorRepository.getHorarios(profesorId).collect { lista ->
                _state.update { it.copy(isLoading = false, horarios = lista) }
            }
        }
    }
}
