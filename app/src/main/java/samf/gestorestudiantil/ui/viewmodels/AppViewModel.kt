package samf.gestorestudiantil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.Recordatorio

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

class AppViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private var recordatoriosListener: ListenerRegistration? = null

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
        recordatoriosListener?.remove()

        recordatoriosListener = db.collection("recordatorios")
            .whereEqualTo("usuarioId", usuarioId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _state.update { it.copy(errorMessage = "Error al cargar recordatorios: ${error.localizedMessage}") }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _state.update { it.copy(recordatorios = snapshot.toObjects(Recordatorio::class.java)) }
                }
            }
    }

    fun añadirRecordatorio(recordatorio: Recordatorio) {
        viewModelScope.launch {
            try {
                db.collection("recordatorios")
                    .document(recordatorio.id)
                    .set(recordatorio)
                    .await()
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al guardar recordatorio: ${e.localizedMessage}") }
            }
        }
    }

    fun eliminarRecordatorio(recordatorioId: String) {
        viewModelScope.launch {
            try {
                db.collection("recordatorios")
                    .document(recordatorioId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al eliminar recordatorio: ${e.localizedMessage}") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        recordatoriosListener?.remove()
    }
}
