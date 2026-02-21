package samf.gestorestudiantil.domain

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

// Estado de la pantalla de autenticación
data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Comprobar si ya hay una sesión iniciada al abrir la app
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
                // 1. Crear usuario en Auth
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: throw Exception("Error al crear usuario")

                // 2. Crear documento en Firestore
                val newUser = User(
                    id = uid,
                    nombre = name,
                    email = email,
                    rol = "ESTUDIANTE", // Por defecto al registrarse
                    cursoOArea = "Sin asignar",
                    centroId = ""
                )

                db.collection("usuarios").document(uid).set(newUser).await()

                _authState.value = AuthState(isSuccess = true, user = newUser)

            } catch (e: FirebaseAuthUserCollisionException) {
                // Si el usuario ya existe (ej. Entró con Google antes)
                _authState.value = AuthState(errorMessage = "Este correo ya está registrado. Si usaste Google antes, inicia sesión con Google.")
            } catch (e: Exception) {
                _authState.value = AuthState(errorMessage = e.localizedMessage ?: "Error en el registro")
            }
        }
    }

    // ====================================================================
    // 3. INICIO DE SESIÓN CON GOOGLE (Recibe el token de la UI)
    // ====================================================================
    fun loginWithGoogleToken(idToken: String) {
        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val firebaseUser = result.user ?: throw Exception("No se pudo obtener el usuario")

                // Comprobamos si el usuario ya existe en nuestra base de datos
                val doc = db.collection("usuarios").document(firebaseUser.uid).get().await()

                if (doc.exists()) {
                    // Si existe, lo descargamos (NO SOBREESCRIBIMOS LOS DATOS)
                    val user = doc.toObject(User::class.java)
                    _authState.value = AuthState(isSuccess = true, user = user)
                } else {
                    // Si es la primera vez que entra con Google, lo registramos en Firestore
                    val newUser = User(
                        id = firebaseUser.uid,
                        nombre = firebaseUser.displayName ?: "Usuario de Google",
                        email = firebaseUser.email ?: "",
                        rol = "ESTUDIANTE",
                        cursoOArea = "Sin asignar",
                        centroId = ""
                    )
                    db.collection("usuarios").document(firebaseUser.uid).set(newUser).await()
                    _authState.value = AuthState(isSuccess = true, user = newUser)
                }
            } catch (e: Exception) {
                _authState.value = AuthState(errorMessage = e.localizedMessage ?: "Error con Google Sign-In")
            }
        }
    }

    // ====================================================================
    // 4. UNIFICAR CUENTA: AÑADIR CONTRASEÑA A UN USUARIO DE GOOGLE
    // ====================================================================
    /**
     * Si un usuario se registró solo con Google, no tiene contraseña.
     * Llamando a esta función desde la pantalla de "Mi Cuenta/Preferencias",
     * podrá establecer una contraseña y acceder también mediante Email/Contraseña.
     */
    fun addPasswordToGoogleAccount(newPassword: String) {
        val user = auth.currentUser
        if (user != null) {
            _authState.value = _authState.value.copy(isLoading = true)
            viewModelScope.launch {
                try {
                    user.updatePassword(newPassword).await()
                    // Si es exitoso, Firebase añade el proveedor Email/Password automáticamente.
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Contraseña configurada. Ya puedes iniciar sesión con tu email y esta contraseña."
                    )
                } catch (e: Exception) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al configurar la contraseña. Es posible que debas cerrar sesión y volver a entrar."
                    )
                }
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
                _authState.value = AuthState(errorMessage = "El usuario no existe en la base de datos")
            }
        } catch (e: Exception) {
            _authState.value = AuthState(errorMessage = "Error al leer datos del usuario")
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }
}