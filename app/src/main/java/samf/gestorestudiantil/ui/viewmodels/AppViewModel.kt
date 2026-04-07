package samf.gestorestudiantil.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Clase simple para representar al usuario actual en la UI
data class CurrentUserUiState(
    val id: String = "",
    val name: String = "",
    val role: String = "",
    val photoUrl: String = "",
    val curso: String = ""
)

class AppViewModel : ViewModel() {
    private val _currentUser = MutableStateFlow<CurrentUserUiState?>(null)
    val currentUser: StateFlow<CurrentUserUiState?> = _currentUser.asStateFlow()

    // Función para cargar al usuario al iniciar sesión
    fun setCurrentUser(user: CurrentUserUiState) {
        _currentUser.value = user
    }

    // ESTA ES LA FUNCIÓN CLAVE: Actualiza solo la foto en el estado global
    fun updatePhotoUrl(newUrl: String) {
        _currentUser.update { currentState ->
            currentState?.copy(photoUrl = newUrl)
        }
    }
}