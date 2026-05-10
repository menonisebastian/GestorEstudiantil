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
import kotlinx.coroutines.flow.SharedFlow
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.domain.notifications.NotificationScheduler
import samf.gestorestudiantil.domain.repositories.RecordatorioRepository
import samf.gestorestudiantil.domain.repositories.UserRepository
import samf.gestorestudiantil.domain.utils.SnackbarEvent
import samf.gestorestudiantil.domain.utils.SnackbarManager
import samf.gestorestudiantil.domain.utils.UiText
import javax.inject.Inject

data class CurrentUserUiState(
    val id: String = "",
    val name: String = "",
    val role: String = "",
    val photoUrl: String = "",
    val curso: String = "",
)

data class AppState(
    val currentUser: CurrentUserUiState? = null,
    val recordatorios: List<Recordatorio> = emptyList(),
    val errorMessage: String? = null,
    val pendingNotificationData: Map<String, String>? = null,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val recordatorioRepository: RecordatorioRepository,
    private val userRepository: UserRepository,
    private val snackbarManager: SnackbarManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(value = false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _updateMessage = MutableStateFlow<String?>(null)
    val updateMessage: StateFlow<String?> = _updateMessage.asStateFlow()

    val snackbarEvents: SharedFlow<SnackbarEvent> = snackbarManager.events

    fun showSnackbar(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        viewModelScope.launch {
            snackbarManager.showSnackbar(message, actionLabel, onAction)
        }
    }

    fun showSnackbar(message: UiText, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        viewModelScope.launch {
            snackbarManager.showSnackbar(message.asString(context), actionLabel, onAction)
        }
    }

    fun setCurrentUser(user: CurrentUserUiState) {
        _state.update { it.copy(currentUser = user) }
        cargarRecordatorios(user.id)
    }

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
                val isNew = recordatorio.id.isEmpty() || recordatorio.id.startsWith("temp_")

                val finalRecordatorio = if (isNew) {
                    recordatorio.copy(id = "rec_${System.currentTimeMillis()}")
                } else {
                    recordatorio
                }
                recordatorioRepository.guardarRecordatorio(finalRecordatorio)
                NotificationScheduler.scheduleRecordatorioNotification(context, finalRecordatorio)
                
                if (isNew) {
                    showSnackbar(
                        message = "Recordatorio creado",
                        actionLabel = "Deshacer",
                    ) {
                        viewModelScope.launch {
                            recordatorioRepository.eliminarRecordatorio(finalRecordatorio.id)
                            NotificationScheduler.cancelNotification(context, finalRecordatorio.id)
                        }
                    }
                } else {
                    showSnackbar("Recordatorio actualizado")
                }
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
                ) {
                    agregarRecordatorio(recordatorio)
                }
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
                showSnackbar("Recordatorio actualizado")
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al actualizar recordatorio: ${e.localizedMessage}") }
            }
        }
    }

    fun checkLatestVersion(currentVersion: String) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            _updateMessage.value = null
            try {
                val latestTag = userRepository.getLatestVersionTag()
                if (latestTag != null) {
                    val cleanTag = latestTag.removePrefix("v").trim()
                    if (cleanTag == currentVersion.trim()) {
                        _updateMessage.value = "Ya tienes la última versión"
                    } else {
                        _updateMessage.value = "Nueva versión disponible: $latestTag"
                    }
                } else {
                    _updateMessage.value = "No se pudo determinar la versión"
                }
            } catch (_: Exception) {
                _updateMessage.value = "Error de conexión"
            } finally {
                _isCheckingUpdate.value = false
            }
        }
    }

    fun clearUpdateMessage() {
        _updateMessage.value = null
    }

    fun handleIntent(intent: android.content.Intent?) {
        val data = mutableMapOf<String, String>()
        intent?.extras?.let { extras ->
            for (key in extras.keySet()) {
                @Suppress("DEPRECATION")
                val value = extras[key]?.toString()
                value?.let {
                    data[key] = it
                }
            }
        }
        if (data.isNotEmpty()) {
            _state.update { it.copy(pendingNotificationData = data) }
        }
    }

    fun clearNotificationData() {
        _state.update { it.copy(pendingNotificationData = null) }
    }
}
