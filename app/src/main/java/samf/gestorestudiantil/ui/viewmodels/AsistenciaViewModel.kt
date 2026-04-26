package samf.gestorestudiantil.ui.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Asistencia
import samf.gestorestudiantil.data.models.AsistenciaEstado
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.AsistenciaRepository
import samf.gestorestudiantil.domain.repositories.ProfesorRepository
import java.util.*
import javax.inject.Inject

data class AsistenciaUiState(
    val isLoading: Boolean = false,
    val estudiantes: List<User> = emptyList(),
    val asistencias: List<Asistencia> = emptyList(),
    val diasHistorial: List<Long> = emptyList(),
    val fechaSeleccionada: Long = 0L
)

@HiltViewModel
class AsistenciaViewModel @Inject constructor(
    private val asistenciaRepository: AsistenciaRepository,
    private val profesorRepository: ProfesorRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(AsistenciaUiState())
    val state: StateFlow<AsistenciaUiState> = _state.asStateFlow()

    fun setFechaActual() {
        _state.update { it.copy(fechaSeleccionada = obtenerMedianoche(System.currentTimeMillis())) }
    }

    fun cambiarFecha(fecha: Long) {
        _state.update { it.copy(fechaSeleccionada = obtenerMedianoche(fecha)) }
    }

    fun cargarDatosAsignatura(asignatura: Asignatura) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Cargar estudiantes en tiempo real
            launch {
                profesorRepository.getEstudiantesPorAsignatura(asignatura).collect { lista ->
                    _state.update { it.copy(estudiantes = lista) }
                }
            }

            // Cargar asistencias para la fecha seleccionada
            launch {
                _state.map { it.fechaSeleccionada }.distinctUntilChanged().collectLatest { fecha ->
                    if (fecha > 0) {
                        asistenciaRepository.getAsistenciasPorDia(asignatura.id, fecha).collect { lista ->
                            _state.update { it.copy(asistencias = lista, isLoading = false) }
                        }
                    }
                }
            }

            // Cargar historial de días
            launch {
                asistenciaRepository.getDiasConAsistencia(asignatura.id).collect { dias ->
                    _state.update { it.copy(diasHistorial = dias) }
                }
            }
        }
    }


    fun actualizarEstadoAsistencia(estudianteId: String, nuevoEstado: AsistenciaEstado, asignaturaId: String) {
        val currentList = _state.value.asistencias.toMutableList()
        val index = currentList.indexOfFirst { it.estudianteId == estudianteId }
        
        val asistencia = if (index != -1) {
            currentList[index].copy(estado = nuevoEstado).also { currentList[index] = it }
        } else {
            val estudiante = _state.value.estudiantes.find { it.id == estudianteId }
            Asistencia(
                asignaturaId = asignaturaId,
                estudianteId = estudianteId,
                estudianteNombre = estudiante?.nombre ?: "",
                fecha = _state.value.fechaSeleccionada,
                estado = nuevoEstado
            ).also { currentList.add(it) }
        }
        
        _state.update { it.copy(asistencias = currentList) }
        
        // Auto-guardar solo este registro
        viewModelScope.launch {
            try {
                asistenciaRepository.guardarAsistencias(listOf(asistencia))
            } catch (e: Exception) {
                Toast.makeText(context, "Error al guardar asistencia automáticamente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun cargarAsistenciasEstudiante(estudianteId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            asistenciaRepository.getAsistenciasPorEstudiante(estudianteId).collect { lista ->
                _state.update { it.copy(asistencias = lista, isLoading = false) }
            }
        }
    }

    private fun obtenerMedianoche(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
