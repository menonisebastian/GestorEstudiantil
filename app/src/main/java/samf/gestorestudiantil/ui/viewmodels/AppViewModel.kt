package samf.gestorestudiantil.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.domain.NotificationScheduler
import samf.gestorestudiantil.domain.repositories.RecordatorioRepository
import javax.inject.Inject

data class SnackbarEvent(
    val message: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

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
    private val recordatorioRepository: RecordatorioRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<SnackbarEvent>()
    val snackbarEvents: SharedFlow<SnackbarEvent> = _snackbarEvents.asSharedFlow()

    fun showSnackbar(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        viewModelScope.launch {
            _snackbarEvents.emit(SnackbarEvent(message, actionLabel, onAction))
        }
    }

    // Función para cargar al usuario al iniciar sesión
    fun setCurrentUser(user: CurrentUserUiState) {
        _state.update { it.copy(currentUser = user) }
        cargarRecordatorios(user.id)
    }

    // ====================================================================
    // RECORDATORIOS (Compartidos por todos los roles, pero privados por usuarioId)
    // ====================================================================
    private fun cargarRecordatorios(usuarioId: String) {
        viewModelScope.launch {
            recordatorioRepository.getRecordatorios(usuarioId).collect { lista ->
                _state.update { it.copy(recordatorios = lista) }
                lista.forEach { recordatorio ->
                    NotificationScheduler.scheduleRecordatorioNotification(context, recordatorio)
                }
            }
        }
    }

    fun agregarRecordatorio(recordatorio: Recordatorio) {
        viewModelScope.launch {
            try {
                recordatorioRepository.guardarRecordatorio(recordatorio)
                NotificationScheduler.scheduleRecordatorioNotification(context, recordatorio)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al guardar recordatorio: ${e.localizedMessage}") }
            }
        }
    }

    fun eliminarRecordatorio(recordatorio: Recordatorio) {
        viewModelScope.launch {
            try {
                recordatorioRepository.eliminarRecordatorio(recordatorio.id)
                NotificationScheduler.cancelNotification(context, recordatorio.id)
                showSnackbar(
                    message = "Recordatorio eliminado",
                    actionLabel = "Deshacer",
                    onAction = {
                        agregarRecordatorio(recordatorio)
                    }
                )
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al eliminar recordatorio: ${e.localizedMessage}") }
            }
        }
    }

    fun actualizarRecordatorio(recordatorio: Recordatorio) {
        viewModelScope.launch {
            try {
                recordatorioRepository.actualizarRecordatorio(recordatorio)
                NotificationScheduler.scheduleRecordatorioNotification(context, recordatorio)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al actualizar recordatorio: ${e.localizedMessage}") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
