package samf.gestorestudiantil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.User

// Estado actualizado para manejar el flujo de Google
data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val requireGooglePasswordSetup: Boolean = false // <-- NUEVO: Bandera de primera vez con Google
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                fetchUserFromFirestore(currentUser.uid)
            }
        }
    }

    // ====================================================================
    // 1. INICIO DE SESIÓN CON EMAIL Y CONTRASEÑA
    // ====================================================================
    fun loginWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState(errorMessage = "Rellena todos los campos")
            return
        }

        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                result.user?.uid?.let { uid ->
                    fetchUserFromFirestore(uid)
                } ?: throw Exception("Error al obtener ID de usuario")
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState(errorMessage = "Error de inicio de sesión: Revisa tus credenciales")
            }
        }
    }

    // ====================================================================
    // 2. REGISTRO CON EMAIL Y CONTRASEÑA
    // ====================================================================
    fun registerWithEmail(email: String, pass: String, name: String) {
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            _authState.value = AuthState(errorMessage = "Rellena todos los campos")
            return
        }

        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: throw Exception("Error al crear usuario")

                val newUser = User(
                    id = uid,
                    nombre = name,
                    email = email,
                    rol = "ESTUDIANTE",
                    cursoOArea = "Sin asignar",
                    centroId = ""
                )

                db.collection("usuarios").document(uid).set(newUser).await()
                _authState.value = AuthState(isSuccess = true, user = newUser)

            } catch (e: FirebaseAuthUserCollisionException) {
                e.printStackTrace()
                _authState.value = AuthState(errorMessage = "Este correo ya está registrado. Si usaste Google antes, inicia sesión con Google.")
            } catch (e: Exception) {
                _authState.value = AuthState(errorMessage = e.localizedMessage ?: "Error en el registro")
            }
        }
    }

    // ====================================================================
    // 3. INICIO DE SESIÓN CON GOOGLE
    // ====================================================================
    fun loginWithGoogleToken(idToken: String) {
        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val firebaseUser = result.user ?: throw Exception("No se pudo obtener el usuario")

                val doc = db.collection("usuarios").document(firebaseUser.uid).get().await()

                if (doc.exists()) {
                    // Ya existía en Firestore -> Login normal
                    val user = doc.toObject(User::class.java)
                    _authState.value = AuthState(isSuccess = true, user = user)
                } else {
                    // Es la primera vez -> Pedimos contraseña
                    _authState.value = AuthState(requireGooglePasswordSetup = true)
                }
            } catch (e: Exception) {
                _authState.value = AuthState(errorMessage = e.localizedMessage ?: "Error con Google Sign-In")
            }
        }
    }

    // ====================================================================
    // 4. COMPLETAR REGISTRO DE GOOGLE (Nueva función)
    // ====================================================================
    fun completeGoogleSetup(password: String) {
        val firebaseUser = auth.currentUser ?: return

        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            try {
                // 1. Asignamos la contraseña para que pueda entrar por Email en el futuro
                firebaseUser.updatePassword(password).await()

                // 2. Creamos su perfil oficial en Firestore
                val newUser = User(
                    id = firebaseUser.uid,
                    nombre = firebaseUser.displayName ?: "Usuario de Google",
                    email = firebaseUser.email ?: "",
                    rol = "ESTUDIANTE",
                    cursoOArea = "Sin asignar",
                    centroId = ""
                )
                db.collection("usuarios").document(firebaseUser.uid).set(newUser).await()

                // 3. Éxito final
                _authState.value = AuthState(isSuccess = true, user = newUser)
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState(errorMessage = "Error al guardar contraseña: ${e.localizedMessage}")
            }
        }
    }

    // ====================================================================
    // 5. FUNCIONES AUXILIARES
    // ====================================================================
    private suspend fun fetchUserFromFirestore(uid: String) {
        try {
            val document = db.collection("usuarios").document(uid).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                _authState.value = AuthState(isSuccess = true, user = user)
            } else {
                // Si existe en Auth pero NO en Firestore, es porque abandonó la app
                // antes de poner la contraseña. Lo mandamos a terminar el proceso.
                _authState.value = AuthState(requireGooglePasswordSetup = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _authState.value = AuthState(errorMessage = "Error al leer datos del usuario")
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }
}