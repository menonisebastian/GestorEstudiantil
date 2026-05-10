package samf.gestorestudiantil.ui.viewmodels

import android.content.Context
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
import samf.gestorestudiantil.R
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.Entrega
import samf.gestorestudiantil.data.models.Tarea
import samf.gestorestudiantil.domain.notifications.NotificationScheduler
import samf.gestorestudiantil.domain.repositories.EstudianteRepository
import samf.gestorestudiantil.domain.repositories.NotificationRepository
import samf.gestorestudiantil.domain.repositories.TareaRepository
import samf.gestorestudiantil.domain.usecases.CalculateUnreadNotificationsUseCase
import samf.gestorestudiantil.domain.utils.ErrorMapper
import samf.gestorestudiantil.domain.utils.FileOpener
import samf.gestorestudiantil.domain.utils.SnackbarManager
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

data class EstudianteState(
    val isLoading: Boolean = false,
    val asignaturas: List<Asignatura> = emptyList(),
    val evaluaciones: List<Evaluacion> = emptyList(),
    val evaluacionesGlobales: List<Evaluacion> = emptyList(),
    val tareas: List<Tarea> = emptyList(),
    val miEntrega: Entrega? = null,
    val horarios: List<Horario> = emptyList(),
    val errorMessage: String? = null,
    val selectedFileData: ByteArray? = null,
    val selectedFileName: String = "",
    val selectedFileSize: Long = 0L,
    val selectedMimeType: String = "",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EstudianteState

        if (isLoading != other.isLoading) return false
        if (asignaturas != other.asignaturas) return false
        if (evaluaciones != other.evaluaciones) return false
        if (evaluacionesGlobales != other.evaluacionesGlobales) return false
        if (tareas != other.tareas) return false
        if (miEntrega != other.miEntrega) return false
        if (horarios != other.horarios) return false
        if (errorMessage != other.errorMessage) return false
        if (selectedFileData != null) {
            if (other.selectedFileData == null) return false
            if (!selectedFileData.contentEquals(other.selectedFileData)) return false
        } else if (other.selectedFileData != null) return false
        if (selectedFileName != other.selectedFileName) return false
        if (selectedFileSize != other.selectedFileSize) return false
        if (selectedMimeType != other.selectedMimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isLoading.hashCode()
        result = 31 * result + asignaturas.hashCode()
        result = 31 * result + evaluaciones.hashCode()
        result = 31 * result + evaluacionesGlobales.hashCode()
        result = 31 * result + tareas.hashCode()
        result = 31 * result + (miEntrega?.hashCode() ?: 0)
        result = 31 * result + horarios.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + (selectedFileData?.contentHashCode() ?: 0)
        result = 31 * result + selectedFileName.hashCode()
        result = 31 * result + selectedFileSize.hashCode()
        result = 31 * result + selectedMimeType.hashCode()
        return result
    }
}

@HiltViewModel
class EstudianteViewModel @Inject constructor(
    private val estudianteRepository: EstudianteRepository,
    private val tareaRepository: TareaRepository,
    private val notificationRepository: NotificationRepository,
    private val calculateUnreadNotificationsUseCase: CalculateUnreadNotificationsUseCase,
    private val snackbarManager: SnackbarManager,
    @param:ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(EstudianteState())
    val state: StateFlow<EstudianteState> = _state.asStateFlow()

    private var asignaturasJob: Job? = null
    private var postsJob: Job? = null
    private var tareasJob: Job? = null
    private var horariosJob: Job? = null
    private var currentUltimaVez: Map<String, Long> = emptyMap()
    private var scheduledTareaIds: Set<String> = emptySet()
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
                val ids = asignaturas.map { it.id.ifEmpty { it.idDocumento } }
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

                val tareasParaProgramar = tareas.filter { it.id !in scheduledTareaIds }
                
                if (tareasParaProgramar.isNotEmpty()) {
                    tareasParaProgramar.forEach { tarea ->
                        NotificationScheduler.scheduleTareaNotification(context, tarea)
                    }
                    scheduledTareaIds += tareasParaProgramar.asSequence().map { it.id }.toSet()
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
            } catch (_: Exception) {
                snackbarManager.showSnackbar(context.getString(R.string.error_mark_as_read))
            }
        }
    }

    fun cargarEvaluaciones(asignaturaId: String, estudianteId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val evaluations = estudianteRepository.getEvaluaciones(asignaturaId, estudianteId)
                _state.update { it.copy(isLoading = false, evaluaciones = evaluations) }
            } catch (_: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = context.getString(R.string.error_load_evaluations)) }
            }
        }
    }

    fun cargarTodasLasEvaluaciones(estudianteId: String, asignaturaIds: List<String>) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val allEvaluations = mutableListOf<Evaluacion>()
                asignaturaIds.forEach { id ->
                    val evals = estudianteRepository.getEvaluaciones(id, estudianteId)
                    allEvaluations.addAll(evals)
                }
                _state.update { it.copy(isLoading = false, evaluacionesGlobales = allEvaluations) }
            } catch (_: Exception) {
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

    // ====================================================================
    // GESTIÓN DE TAREAS Y ENTREGAS (Estudiante)
    // ====================================================================
    fun realizarEntrega(entrega: Entrega, fileData: ByteArray, fileName: String, mimeType: String? = null, acronimoAsignatura: String = "") {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val entregaId = tareaRepository.realizarEntrega(entrega, fileData, fileName, mimeType)

                val finalAcronimo = acronimoAsignatura.ifBlank {
                    _state.value.asignaturas.find { it.id == entrega.asignaturaId }?.acronimo ?: ""
                }

                enviarNotificacionAlProfesor(entrega, finalAcronimo)

                snackbarManager.showSnackbar(
                    message = context.getString(R.string.success_delivery),
                    actionLabel = context.getString(R.string.label_undo),
                ) {
                    viewModelScope.launch {
                        tareaRepository.eliminarEntrega(entrega.copy(id = entregaId))
                    }
                }
            } catch (e: Exception) {
                val errorMsg = ErrorMapper.getFriendlyMessage(context, e)
                snackbarManager.showSnackbar(errorMsg)
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun enviarNotificacionAlProfesor(entrega: Entrega, acronimo: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                val topic = "asignatura_${entrega.asignaturaId}_profesores"
                val title = if (acronimo.isNotBlank()) "Nueva Entrega: $acronimo" else "Nueva Entrega"
                val body = "${entrega.estudianteNombre} ha entregado una tarea."
                val data = mapOf(
                    "target_asignatura_id" to entrega.asignaturaId,
                    "type" to "entrega",
                    "sender_id" to entrega.estudianteId
                )

                notificationRepository.sendTopicNotification(topic, title, body, data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarEntrega(entrega: Entrega, onUndo: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                tareaRepository.eliminarEntrega(entrega)
                onUndo?.invoke()
                snackbarManager.showSnackbar("Entrega eliminada")
            } catch (_: Exception) {
                snackbarManager.showSnackbar(context.getString(R.string.error_delete_delivery))
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

    fun descargarArchivo(supabasePath: String, nombreArchivo: String, isDirectDownload: Boolean = false) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val bytes = tareaRepository.descargarArchivo(supabasePath)
                if (isDirectDownload) {
                    FileOpener.downloadFile(context, bytes, nombreArchivo)
                    snackbarManager.showSnackbar("Archivo descargado")
                } else {
                    FileOpener.openFile(context, bytes, nombreArchivo)
                }
            } catch (_: Exception) {
                snackbarManager.showSnackbar(context.getString(R.string.error_open_file))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onFileSelected(uri: android.net.Uri?) {
        uri?.let { it ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    _state.update { it.copy(isLoading = true) }
                    val contentResolver = context.contentResolver
                    
                    var fileName = ""
                    var fileSize = 0L
                    
                    val cursor = contentResolver.query(it, null, null, null, null)
                    cursor?.use { c ->
                        val nameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (nameIndex != -1 && (c.moveToFirst())) {
                            fileName = c.getString(nameIndex)
                        }
                        if (sizeIndex != -1) {
                            fileSize = c.getLong(sizeIndex)
                        }
                    }
                    if (fileName.isEmpty()) {
                        fileName = it.lastPathSegment ?: "archivo"
                    }

                    val initialMimeType = contentResolver.getType(it) ?: FileOpener.getMimeType(fileName)
                    var finalMimeType = initialMimeType
                    var fileData: ByteArray? = null

                    if (initialMimeType.contains("google-apps")) {
                        val streamTypes = contentResolver.getStreamTypes(it, "*/*")
                        val hasDocx = streamTypes?.any { type -> type.contains("wordprocessingml.document") } == true
                        
                        val exportType = when {
                            initialMimeType.contains("document") && hasDocx -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            initialMimeType.contains("document") -> "application/msword"
                            initialMimeType.contains("spreadsheet") -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            initialMimeType.contains("presentation") -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                            else -> "application/pdf"
                        }
                        
                        contentResolver.openTypedAssetFileDescriptor(it, exportType, null)?.use { ad ->
                            finalMimeType = exportType
                            fileData = ad.createInputStream().use { stream -> stream.readBytes() }
                        }
                    } else {
                        fileData = contentResolver.openInputStream(it)?.use { stream -> stream.readBytes() }
                    }

                    _state.update { s -> 
                        s.copy(
                            selectedFileData = fileData,
                            selectedFileName = fileName,
                            selectedFileSize = fileData?.size?.toLong() ?: fileSize,
                            selectedMimeType = finalMimeType,
                            isLoading = false
                        )
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false) }
                    snackbarManager.showSnackbar("Error al cargar archivo: ${e.localizedMessage}")
                }
            }
        }
    }
}
