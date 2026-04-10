package samf.gestorestudiantil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.domain.repositories.RecordatorioRepository
import javax.inject.Inject

// Clase simple para representar al usuario actual en la UI
data class CurrentUserUiState(
    val id: String = "",
    val name: String = "",
    val role: String = "",
    val photoUrl: String = "",
    val curso: String = ""
)

data class AppState(
    val currentUser: CurrentUserUiState? = null,
    val recordatorios: List<Recordatorio> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val recordatorioRepository: RecordatorioRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    // Función para cargar al usuario al iniciar sesión
    fun setCurrentUser(user: CurrentUserUiState) {
        _state.update { it.copy(currentUser = user) }
        cargarRecordatorios(user.id)
    }

    // ESTA ES LA FUNCIÓN CLAVE: Actualiza solo la foto en el estado global
    fun updatePhotoUrl(newUrl: String) {
        _state.update { it.copy(currentUser = it.currentUser?.copy(photoUrl = newUrl)) }
    }

    // ====================================================================
    // RECORDATORIOS (Compartidos por todos los roles, pero privados por usuarioId)
    // ====================================================================
    private fun cargarRecordatorios(usuarioId: String) {
        viewModelScope.launch {
            recordatorioRepository.getRecordatorios(usuarioId).collect { lista ->
                _state.update { it.copy(recordatorios = lista) }
            }
        }
    }

    fun añadirRecordatorio(recordatorio: Recordatorio) {
        viewModelScope.launch {
            try {
                recordatorioRepository.guardarRecordatorio(recordatorio)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al guardar recordatorio: ${e.localizedMessage}") }
            }
        }
    }

    fun eliminarRecordatorio(recordatorioId: String) {
        viewModelScope.launch {
            try {
                recordatorioRepository.eliminarRecordatorio(recordatorioId)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al eliminar recordatorio: ${e.localizedMessage}") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
