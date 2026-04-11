package samf.gestorestudiantil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.domain.repositories.EstudianteRepository
import samf.gestorestudiantil.domain.usecases.CalculateUnreadNotificationsUseCase
import javax.inject.Inject

data class EstudianteState(
    val isLoading: Boolean = false,
    val asignaturas: List<Asignatura> = emptyList(),
    val evaluaciones: List<Evaluacion> = emptyList(),
    val horarios: List<Horario> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class EstudianteViewModel @Inject constructor(
    private val estudianteRepository: EstudianteRepository,
    private val calculateUnreadNotificationsUseCase: CalculateUnreadNotificationsUseCase
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
                observarCambiosEnPosts(asignaturas.map { it.id })
                recalcularNotificaciones(asignaturas, currentUltimaVez)
            }
        }
    }

    private fun subscribeToAsignaturas(asignaturaIds: List<String>) {
        asignaturaIds.forEach { id ->
            FirebaseMessaging.getInstance().subscribeToTopic("asignatura_$id")
        }
    }

    private fun observarCambiosEnPosts(asignaturaIds: List<String>) {
        postsJob?.cancel()
        postsJob = viewModelScope.launch {
            estudianteRepository.observePostsChanges(asignaturaIds).collect {
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
            }
        }
    }

    fun cargarEvaluaciones(asignaturaId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val evaluations = estudianteRepository.getEvaluaciones(asignaturaId)
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
}
