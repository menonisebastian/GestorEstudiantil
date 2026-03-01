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
import samf.gestorestudiantil.data.models.listaCentros
import samf.gestorestudiantil.data.models.listaCursos

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
        loadCentros()
    }

    // ====================================================================
    // CARGA DE DATOS PARA SELECTORES (DROPDOWNS)
    // ====================================================================
    private fun loadCentros() {

        _centros.value = listaCentros

//        viewModelScope.launch {
//            try {
//                val snapshot = db.collection("centros").get().await()
//                val lista = snapshot.toObjects(Centro::class.java)
//                _centros.value = lista
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
    }

    fun loadCursosPorCentro(centroId: String) {
        _cursos.value = listaCursos.filter { it.centroId == centroId }
//        viewModelScope.launch {
//            try {
//                val snapshot = db.collection("cursos")
//                    .whereEqualTo("centroId", centroId)
//                    .get().await()
//                val lista = snapshot.toObjects(Curso::class.java)
//                _cursos.value = lista
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
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
            } catch (_: Exception) {
                _authState.value = AuthState(errorMessage = "Credenciales incorrectas")
            }
        }
    }

    // ====================================================================
    // REGISTRO CON LÓGICA DE ROLES, ESTADOS Y FOTO DE PERFIL
    // ====================================================================
    fun registerWithEmail(
        email: String, pass: String, name: String,
        rolSeleccionado: String, centroId: String, cursoId: String, cursoNombre: String,
        imgUrl: String // <-- NUEVO PARÁMETRO DE CLOUDINARY
    ) {
        if (email.isBlank() || pass.isBlank() || name.isBlank() || centroId.isBlank()) {
            _authState.value = AuthState(errorMessage = "Faltan datos obligatorios")
            return
        }

        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            try {
                var finalRol = rolSeleccionado
                var estadoInicial = "ACTIVO"
                var areaOCurso = cursoNombre

                if (rolSeleccionado == "ESTUDIANTE") {
                    estadoInicial = "PENDIENTE"
                } else if (rolSeleccionado == "PROFESOR") {
                    areaOCurso = "Sin asignar"
                    val admins = db.collection("usuarios")
                        .whereEqualTo("centroId", centroId)
                        .whereEqualTo("rol", "ADMIN")
                        .get().await()

                    if (admins.isEmpty) {
                        finalRol = "ADMIN"
                    }
                }

                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: throw Exception("Error Auth")

                // CREAMOS EL USUARIO CON LA FOTO INCLUIDA
                val newUser = User(
                    id = uid, nombre = name, email = email,
                    rol = finalRol, cursoId = cursoId, cursoOArea = areaOCurso,
                    centroId = centroId, estado = estadoInicial,
                    imgUrl = imgUrl // <-- GUARDADO EN FIRESTORE
                )

                db.collection("usuarios").document(uid).set(newUser).await()
                _authState.value = AuthState(isSuccess = true, user = newUser)

            } catch (_: FirebaseAuthUserCollisionException) {
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
                    val user = doc.toObject(User::class.java)
                    _authState.value = AuthState(isSuccess = true, user = user)
                } else {
                    _authState.value = AuthState(requireGooglePasswordSetup = true)
                }
            } catch (e: Exception) {
                _authState.value = AuthState(errorMessage = e.localizedMessage ?: "Error con Google Sign-In")
            }
        }
    }

    fun completeGoogleSetup(password: String) {
        val firebaseUser = auth.currentUser ?: return

        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            try {
                firebaseUser.updatePassword(password).await()

                val newUser = User(
                    id = firebaseUser.uid,
                    nombre = firebaseUser.displayName ?: "Usuario de Google",
                    email = firebaseUser.email ?: "",
                    rol = "ESTUDIANTE",
                    cursoOArea = "Sin asignar",
                    centroId = "",
                    estado = "PENDIENTE",
                    imgUrl = firebaseUser.photoUrl?.toString() ?: "" // Aprovechamos la foto de Google si la tiene
                )
                db.collection("usuarios").document(firebaseUser.uid).set(newUser).await()

                _authState.value = AuthState(isSuccess = true, user = newUser)
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState(errorMessage = "Error al guardar contraseña: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun fetchUserFromFirestore(uid: String) {
        try {
            val doc = db.collection("usuarios").document(uid).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null) {
                _authState.value = AuthState(isSuccess = true, user = user)
            } else {
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