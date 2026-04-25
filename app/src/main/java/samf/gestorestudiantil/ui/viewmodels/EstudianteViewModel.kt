package samf.gestorestudiantil.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import samf.gestorestudiantil.R
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.Entrega
import samf.gestorestudiantil.data.models.Tarea
import samf.gestorestudiantil.domain.NotificationScheduler
import samf.gestorestudiantil.domain.repositories.EstudianteRepository
import samf.gestorestudiantil.domain.repositories.NotificationRepository
import samf.gestorestudiantil.domain.repositories.TareaRepository
import samf.gestorestudiantil.domain.usecases.CalculateUnreadNotificationsUseCase
import samf.gestorestudiantil.ui.utils.ErrorMapper
import samf.gestorestudiantil.ui.utils.FileOpener
import kotlinx.coroutines.FlowPreview
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class EstudianteState(
    val isLoading: Boolean = false,
    val asignaturas: List<Asignatura> = emptyList(),
    val evaluaciones: List<Evaluacion> = emptyList(),
    val tareas: List<Tarea> = emptyList(),
    val miEntrega: Entrega? = null,
    val horarios: List<Horario> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class EstudianteViewModel @Inject constructor(
    private val estudianteRepository: EstudianteRepository,
    private val tareaRepository: TareaRepository,
    private val notificationRepository: NotificationRepository,
    private val calculateUnreadNotificationsUseCase: CalculateUnreadNotificationsUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(EstudianteState())
    val state: StateFlow<EstudianteState> = _state.asStateFlow()

    private var asignaturasJob: Job? = null
    private var postsJob: Job? = null
    private var tareasJob: Job? = null
    private var horariosJob: Job? = null
    private var currentUltimaVez: Map<String, Long> = emptyMap()
    private var lastAsignaturasParams: String? = null
    private var lastHorariosParams: String? = null

    fun cargarAsignaturas(cursoId: String, turno: String, cicloNum: Int, ultimaVezAsignaturas: Map<String, Long> = emptyMap()) {
        val params = "$cursoId|$turno|$cicloNum"
        if (params == lastAsignaturasParams) return
        lastAsignaturasParams = params

        currentUltimaVez = ultimaVezAsignaturas
        if (_state.value.asignaturas.isEmpty()) {
            _state.update { it.copy(isLoading = true) }
        }
        
        asignaturasJob?.cancel()
        asignaturasJob = viewModelScope.launch {
            estudianteRepository.getAsignaturas(cursoId, turno, cicloNum).collect { asignaturas ->
                _state.update { it.copy(isLoading = false, asignaturas = asignaturas) }
                val ids = asignaturas.map { it.id }
                subscribeToAsignaturas(ids)
                observarCambiosEnPostsYTareas(ids)
                observarTodasLasTareas(ids)
                recalcularNotificaciones(asignaturas, currentUltimaVez)
            }
        }
    }

    private fun observarTodasLasTareas(asignaturaIds: List<String>) {
        tareasJob?.cancel()
        tareasJob = viewModelScope.launch {
            tareaRepository.getTareasPorAsignaturas(asignaturaIds).collect { tareas ->
                _state.update { it.copy(tareas = tareas) }
                tareas.forEach { tarea ->
                    NotificationScheduler.scheduleTareaNotification(context, tarea)
                }
            }
        }
    }

    private fun subscribeToAsignaturas(asignaturaIds: List<String>) {
        asignaturaIds.forEach { id ->
            FirebaseMessaging.getInstance().subscribeToTopic("asignatura_${id}_estudiantes")
        }
    }

    @OptIn(FlowPreview::class)
    private fun observarCambiosEnPostsYTareas(asignaturaIds: List<String>) {
        postsJob?.cancel()
        postsJob = viewModelScope.launch {
            estudianteRepository.observePostsAndTareasChanges(asignaturaIds)
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

    private fun recalcularNotificaciones(asignaturas: List<Asignatura>, ultimaVezAsignaturas: Map<String, Long>) {
        viewModelScope.launch {
            val nuevasAsignaturas = calculateUnreadNotificationsUseCase(asignaturas, ultimaVezAsignaturas)
            _state.update { it.copy(asignaturas = nuevasAsignaturas) }
        }
    }

    fun marcarAsignaturaComoLeida(usuarioId: String, asignaturaId: String) {
        val ahora = System.currentTimeMillis()
        val nuevoMapa = currentUltimaVez.toMutableMap()
        nuevoMapa[asignaturaId] = ahora
        currentUltimaVez = nuevoMapa

        viewModelScope.launch {
            try {
                estudianteRepository.marcarAsignaturaLeida(usuarioId, asignaturaId, ahora)
                _state.update { currentState ->
                    val nuevas = currentState.asignaturas.map {
                        if (it.id == asignaturaId) it.copy(numNotificaciones = 0) else it
                    }
                    currentState.copy(asignaturas = nuevas)
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = context.getString(R.string.error_mark_as_read)) }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.error_mark_as_read), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun cargarEvaluaciones(asignaturaId: String, estudianteId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val evaluations = estudianteRepository.getEvaluaciones(asignaturaId, estudianteId)
                _state.update { it.copy(isLoading = false, evaluaciones = evaluations) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = context.getString(R.string.error_load_evaluations)) }
            }
        }
    }

    fun cargarHorarios(cursoId: String, turno: String, cicloNum: Int) {
        val params = "$cursoId|$turno|$cicloNum"
        if (params == lastHorariosParams) return
        lastHorariosParams = params

        if (_state.value.horarios.isEmpty()) {
            _state.update { it.copy(isLoading = true) }
        }
        horariosJob?.cancel()
        horariosJob = viewModelScope.launch {
            estudianteRepository.getHorarios(cursoId, turno, cicloNum).collect { horarios ->
                _state.update { it.copy(isLoading = false, horarios = horarios) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    // ====================================================================
    // GESTIÓN DE TAREAS Y ENTREGAS (Estudiante)
    // ====================================================================
    fun realizarEntrega(entrega: Entrega, fileData: ByteArray, fileName: String, mimeType: String? = null, acronimoAsignatura: String = "") {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                tareaRepository.realizarEntrega(entrega, fileData, fileName, mimeType)
                
                enviarNotificacionAlProfesor(entrega, acronimoAsignatura)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.success_delivery), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                val errorMsg = ErrorMapper.getFriendlyMessage(context, e)
                _state.update { it.copy(errorMessage = errorMsg) }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun enviarNotificacionAlProfesor(entrega: Entrega, acronimo: String) {
        viewModelScope.launch {
            try {
                val topic = "asignatura_${entrega.asignaturaId}_profesores"
                val title = "Nueva Entrega: $acronimo"
                val body = "${entrega.estudianteNombre} ha entregado una tarea."
                val data = mapOf(
                    "target_asignatura_id" to entrega.asignaturaId,
                    "type" to "entrega"
                )

                notificationRepository.sendTopicNotification(topic, title, body, data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarEntrega(entrega: Entrega, onUndo: () -> Unit) {
        viewModelScope.launch {
            try {
                tareaRepository.eliminarEntrega(entrega)
                onUndo()
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = context.getString(R.string.error_delete_delivery)) }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.error_delete_delivery), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private var miEntregaJob: Job? = null

    fun cargarMiEntrega(tareaId: String, estudianteId: String) {
        miEntregaJob?.cancel()
        miEntregaJob = viewModelScope.launch {
            tareaRepository.getEntregaEstudiante(tareaId, estudianteId).collect { entrega ->
                _state.update { it.copy(miEntrega = entrega) }
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
                    Toast.makeText(context, context.getString(R.string.error_open_file), Toast.LENGTH_SHORT).show()
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
