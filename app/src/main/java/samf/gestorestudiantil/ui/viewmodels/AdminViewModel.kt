package samf.gestorestudiantil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.User

data class AdminState(
    val isLoading: Boolean = true,
    val usuarios: List<User> = emptyList(),
    val errorMessage: String? = null
)

class AdminViewModel : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _adminState = MutableStateFlow(AdminState())
    val adminState: StateFlow<AdminState> = _adminState.asStateFlow()

    private var usuariosListener: ListenerRegistration? = null

    // ====================================================================
    // 1. CARGAR USUARIOS DEL INSTITUTO (EN TIEMPO REAL)
    // ====================================================================
    fun cargarUsuariosPorCentro(centroId: String) {
        _adminState.value = _adminState.value.copy(isLoading = true)

        // Limpiamos el listener anterior si existía
        usuariosListener?.remove()

        // Usamos addSnapshotListener para recibir actualizaciones en tiempo real
        usuariosListener = db.collection("usuarios")
            .whereEqualTo("centroId", centroId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al obtener usuarios: ${error.localizedMessage}"
                    )
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val listaUsuarios = snapshot.toObjects(User::class.java)
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        usuarios = listaUsuarios,
                        errorMessage = null
                    )
                }
            }
    }

    // ====================================================================
    // 2. APROBAR USUARIO PENDIENTE
    // ====================================================================
    fun aprobarUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                // Cambiamos el estado a "ACTIVO" en Firestore
                db.collection("usuarios").document(usuarioId)
                    .update("estado", "ACTIVO").await()
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(
                    errorMessage = "Error al aprobar usuario: ${e.localizedMessage}"
                )
            }
        }
    }

    // ====================================================================
    // 3. RECHAZAR O ELIMINAR USUARIO
    // ====================================================================
    fun rechazarOEliminarUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                // Borramos el documento del usuario de Firestore
                // Nota: Esto no borra su cuenta de Firebase Auth (requiere Admin SDK o Cloud Functions),
                // pero sí le bloquea el acceso a la app porque se queda sin perfil de usuario.
                db.collection("usuarios").document(usuarioId).delete().await()
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(
                    errorMessage = "Error al eliminar usuario: ${e.localizedMessage}"
                )
            }
        }
    }

    // ====================================================================
    // 4. CAMBIAR ROL O CURSO DE UN USUARIO (Opcional para edición)
    // ====================================================================
    fun actualizarDatosUsuario(usuarioId: String, nuevoRol: String, nuevoCurso: String) {
        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "rol" to nuevoRol,
                    "cursoOArea" to nuevoCurso
                )
                db.collection("usuarios").document(usuarioId).update(updates).await()
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(
                    errorMessage = "Error al actualizar usuario: ${e.localizedMessage}"
                )
            }
        }
    }

    fun clearError() {
        _adminState.value = _adminState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        // Importante: Detener la escucha de la base de datos cuando el ViewModel se destruya
        usuariosListener?.remove()
    }
}