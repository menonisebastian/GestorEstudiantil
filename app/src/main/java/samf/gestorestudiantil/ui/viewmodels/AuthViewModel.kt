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
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.User

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val requireGooglePasswordSetup: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Listas para los Dropdowns en el Registro
    private val _centros = MutableStateFlow<List<Centro>>(emptyList())
    val centros: StateFlow<List<Centro>> = _centros.asStateFlow()

    private val _cursos = MutableStateFlow<List<Curso>>(emptyList())
    val cursos: StateFlow<List<Curso>> = _cursos.asStateFlow()

    init {
        checkCurrentUser()
        loadCentros() // Cargamos los institutos al iniciar la pantalla
    }

    // ====================================================================
    // CARGA DE DATOS PARA SELECTORES (DROPDOWNS)
    // ====================================================================
    private fun loadCentros() {
        // PARA PRUEBAS: Usamos la lista de prueba de Centro.kt
        _centros.value = samf.gestorestudiantil.data.models.listaCentros

        /* CUANDO TENGAS FIREBASE LISTO, BORRAS LO DE ARRIBA Y DESCOMENTAS ESTO:
        viewModelScope.launch {
            try {
                val snapshot = db.collection("centros").get().await()
                val lista = snapshot.toObjects(Centro::class.java)
                _centros.value = lista
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        */
    }

    fun loadCursosPorCentro(centroId: String) {
        // PARA PRUEBAS: Filtramos la lista local de Curso.kt
        _cursos.value = samf.gestorestudiantil.data.models.listaCursos.filter { it.centroId == centroId }

        /* CUANDO TENGAS FIREBASE LISTO, USAS ESTO:
        viewModelScope.launch {
            try {
                val snapshot = db.collection("cursos")
                    .whereEqualTo("centroId", centroId)
                    .get().await()
                val lista = snapshot.toObjects(Curso::class.java)
                _cursos.value = lista
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        */
    }

    // ====================================================================
    // FLUJOS DE SESIÓN Y REGISTRO
    // ====================================================================
    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch { fetchUserFromFirestore(currentUser.uid) }
        }
    }

    fun loginWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState(errorMessage = "Rellena todos los campos")
            return
        }
        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                result.user?.uid?.let { fetchUserFromFirestore(it) }
            } catch (e: Exception) {
                _authState.value = AuthState(errorMessage = "Credenciales incorrectas")
            }
        }
    }

    // ====================================================================
    // REGISTRO CON LÓGICA DE ROLES Y ESTADOS (Email/Contraseña)
    // ====================================================================
    fun registerWithEmail(
        email: String, pass: String, name: String,
        rolSeleccionado: String, centroId: String, cursoId: String, cursoNombre: String
    ) {
        if (email.isBlank() || pass.isBlank() || name.isBlank() || centroId.isBlank()) {
            _authState.value = AuthState(errorMessage = "Faltan datos obligatorios")
            return
        }

        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            try {
                // 1. Logica del Rol y Estado inicial
                var finalRol = rolSeleccionado
                var estadoInicial = "ACTIVO"
                var areaOCurso = cursoNombre

                if (rolSeleccionado == "ESTUDIANTE") {
                    estadoInicial = "PENDIENTE" // Requiere aprobación
                } else if (rolSeleccionado == "PROFESOR") {
                    areaOCurso = "Sin asignar"
                    // AUTO-ADMIN: Verificamos si ya hay un admin en este centro
                    val admins = db.collection("usuarios")
                        .whereEqualTo("centroId", centroId)
                        .whereEqualTo("rol", "ADMIN")
                        .get().await()

                    if (admins.isEmpty) {
                        finalRol = "ADMIN" // ¡Es el primero! Lo hacemos Admin
                    }
                }

                // 2. Crear Auth User
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: throw Exception("Error Auth")

                // 3. Crear Perfil en Firestore
                val newUser = User(
                    id = uid, nombre = name, email = email,
                    rol = finalRol, cursoId = cursoId, cursoOArea = areaOCurso,
                    centroId = centroId, estado = estadoInicial
                )

                db.collection("usuarios").document(uid).set(newUser).await()
                _authState.value = AuthState(isSuccess = true, user = newUser)

            } catch (e: FirebaseAuthUserCollisionException) {
                _authState.value = AuthState(errorMessage = "El correo ya está registrado.")
            } catch (e: Exception) {
                _authState.value = AuthState(errorMessage = "Error en registro: ${e.message}")
            }
        }
    }

    // ====================================================================
    // INICIO DE SESIÓN CON GOOGLE
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
                    // Es la primera vez -> Pedimos contraseña y completar perfil (redirección)
                    _authState.value = AuthState(requireGooglePasswordSetup = true)
                }
            } catch (e: Exception) {
                _authState.value = AuthState(errorMessage = e.localizedMessage ?: "Error con Google Sign-In")
            }
        }
    }

    // ====================================================================
    // COMPLETAR REGISTRO DE GOOGLE (Tras establecer la contraseña)
    // ====================================================================
    fun completeGoogleSetup(password: String) {
        val firebaseUser = auth.currentUser ?: return

        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            try {
                // 1. Asignamos la contraseña para que pueda entrar por Email en el futuro
                firebaseUser.updatePassword(password).await()

                // 2. Creamos su perfil oficial en Firestore (Por defecto Estudiante, luego se puede modificar para añadir selectores si lo deseas)
                val newUser = User(
                    id = firebaseUser.uid,
                    nombre = firebaseUser.displayName ?: "Usuario de Google",
                    email = firebaseUser.email ?: "",
                    rol = "ESTUDIANTE",
                    cursoOArea = "Sin asignar",
                    centroId = "",
                    estado = "PENDIENTE" // Lo ponemos pendiente por seguridad hasta que lo asigne un admin
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
    // FUNCIONES AUXILIARES
    // ====================================================================
    private suspend fun fetchUserFromFirestore(uid: String) {
        try {
            val doc = db.collection("usuarios").document(uid).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null) {
                _authState.value = AuthState(isSuccess = true, user = user)
            } else {
                // Si existe en Auth pero NO en Firestore, abandonó la app antes de poner la contraseña
                _authState.value = AuthState(requireGooglePasswordSetup = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _authState.value = AuthState(errorMessage = "Error al leer datos")
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }
}