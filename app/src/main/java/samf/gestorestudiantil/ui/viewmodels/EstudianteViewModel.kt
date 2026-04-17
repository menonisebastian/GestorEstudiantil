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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.Entrega
import samf.gestorestudiantil.data.models.Tarea
import samf.gestorestudiantil.domain.repositories.EstudianteRepository
import samf.gestorestudiantil.domain.repositories.TareaRepository
import samf.gestorestudiantil.domain.usecases.CalculateUnreadNotificationsUseCase
import com.google.auth.oauth2.GoogleCredentials
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
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
    private val calculateUnreadNotificationsUseCase: CalculateUnreadNotificationsUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(EstudianteState())
    val state: StateFlow<EstudianteState> = _state.asStateFlow()

    private var asignaturasJob: Job? = null
    private var postsJob: Job? = null
    private var horariosJob: Job? = null
    private var currentUltimaVez: Map<String, Long> = emptyMap()

    fun cargarAsignaturas(cursoId: String, turno: String, cicloNum: Int, ultimaVezAsignaturas: Map<String, Long> = emptyMap()) {
        currentUltimaVez = ultimaVezAsignaturas
        _state.update { it.copy(isLoading = true) }
        
        asignaturasJob?.cancel()
        asignaturasJob = viewModelScope.launch {
            estudianteRepository.getAsignaturas(cursoId, turno, cicloNum).collect { asignaturas ->
                _state.update { it.copy(isLoading = false, asignaturas = asignaturas) }
                subscribeToAsignaturas(asignaturas.map { it.id })
                observarCambiosEnPostsYTareas(asignaturas.map { it.id })
                recalcularNotificaciones(asignaturas, currentUltimaVez)
            }
        }
    }

    private fun subscribeToAsignaturas(asignaturaIds: List<String>) {
        asignaturaIds.forEach { id ->
            FirebaseMessaging.getInstance().subscribeToTopic("asignatura_${id}_estudiantes")
        }
    }

    private fun observarCambiosEnPostsYTareas(asignaturaIds: List<String>) {
        postsJob?.cancel()
        postsJob = viewModelScope.launch {
            estudianteRepository.observePostsAndTareasChanges(asignaturaIds).collect {
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
                _state.update { it.copy(errorMessage = e.localizedMessage) }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al marcar como leída: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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
                _state.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }
        }
    }

    fun cargarHorarios(cursoId: String, turno: String, cicloNum: Int) {
        _state.update { it.copy(isLoading = true) }
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
                
                // Enviar notificación al profesor
                enviarNotificacionAlProfesor(entrega, acronimoAsignatura)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Tarea entregada con éxito", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al entregar: ${e.localizedMessage}") }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al entregar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun enviarNotificacionAlProfesor(entrega: Entrega, acronimo: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val accessToken = getAccessToken(context) ?: return@launch
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                val topic = "asignatura_${entrega.asignaturaId}_profesores"
                val title = "Nueva Entrega: $acronimo"
                val body = "${entrega.estudianteNombre} ha entregado una tarea."

                val json = JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("topic", topic)
                        put("notification", JSONObject().apply {
                            put("title", title)
                            put("body", body)
                        })
                        put("data", JSONObject().apply {
                            put("target_asignatura_id", entrega.asignaturaId)
                            put("type", "entrega")
                        })
                    })
                }

                val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url("https://fcm.googleapis.com/v1/projects/gestorinstituto-tfg/messages:send")
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                println("Error enviando notificación: ${response.body?.string()}")
                            }
                        }
                    }
                })
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

    fun eliminarEntrega(entrega: Entrega) {
        viewModelScope.launch {
            try {
                tareaRepository.eliminarEntrega(entrega)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Entrega eliminada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al eliminar entrega: ${e.localizedMessage}") }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al eliminar entrega: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun cargarMiEntrega(tareaId: String, estudianteId: String) {
        viewModelScope.launch {
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
}
