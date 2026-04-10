package samf.gestorestudiantil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.AuthRepository
import samf.gestorestudiantil.domain.repositories.CourseRepository
import samf.gestorestudiantil.domain.repositories.UserRepository
import samf.gestorestudiantil.domain.usecases.CompleteGoogleSetupUseCase
import samf.gestorestudiantil.domain.usecases.RegisterUserUseCase
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val requireGooglePasswordSetup: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val registerUserUseCase: RegisterUserUseCase,
    private val completeGoogleSetupUseCase: CompleteGoogleSetupUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _centros = MutableStateFlow<List<Centro>>(emptyList())
    val centros: StateFlow<List<Centro>> = _centros.asStateFlow()

    private val _cursos = MutableStateFlow<List<Curso>>(emptyList())
    val cursos: StateFlow<List<Curso>> = _cursos.asStateFlow()

    init {
        checkCurrentUser()
        loadCentros()
    }

    private fun loadCentros() {
        viewModelScope.launch {
            try {
                _centros.value = courseRepository.getCentros()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadCursosPorCentro(centroId: String) {
        viewModelScope.launch {
            try {
                _cursos.value = courseRepository.getCursosPorCentro(centroId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkCurrentUser() {
        val uid = authRepository.getCurrentUserUid()
        if (uid != null) {
            viewModelScope.launch { fetchUserFromFirestore(uid) }
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
                val uid = authRepository.loginWithEmail(email, pass)
                fetchUserFromFirestore(uid)
            } catch (_: Exception) {
                _authState.value = AuthState(errorMessage = "Credenciales incorrectas")
            }
        }
    }

    fun registerWithEmail(
        email: String, pass: String, name: String,
        rolSeleccionado: String, centroId: String, cursoId: String, cursoNombre: String, turno: String,
        imgUrl: String
    ) {
        if (email.isBlank() || pass.isBlank() || name.isBlank() || centroId.isBlank()) {
            _authState.value = AuthState(errorMessage = "Faltan datos obligatorios")
            return
        }

        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            try {
                val newUser = registerUserUseCase(
                    email, pass, name, rolSeleccionado, centroId, cursoId, cursoNombre, turno, imgUrl
                )
                _authState.value = AuthState(isSuccess = true, user = newUser)

            } catch (_: FirebaseAuthUserCollisionException) {
                _authState.value = AuthState(errorMessage = "El correo ya está registrado.")
            } catch (e: Exception) {
                _authState.value = AuthState(errorMessage = "Error en registro: ${e.message}")
            }
        }
    }

    fun loginWithGoogleToken(idToken: String) {
        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            try {
                val uid = authRepository.signInWithGoogle(idToken)
                val user = userRepository.getUser(uid)

                if (user != null && user.rol.isNotBlank()) {
                    _authState.value = AuthState(isSuccess = true, user = user)
                } else {
                    _authState.value = AuthState(requireGooglePasswordSetup = true)
                }
            } catch (e: Exception) {
                _authState.value = AuthState(errorMessage = e.localizedMessage ?: "Error con Google Sign-In")
            }
        }
    }

    fun completeGoogleSetup(
        password: String,
        rolSeleccionado: String,
        centroId: String,
        cursoId: String,
        cursoNombre: String,
        turno: String,
        name: String,
        email: String,
        imgUrl: String
    ) {
        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            try {
                val newUser = completeGoogleSetupUseCase(
                    password, rolSeleccionado, centroId, cursoId, cursoNombre, turno, name, email, imgUrl
                )
                _authState.value = AuthState(isSuccess = true, user = newUser)
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState(errorMessage = "Error al completar registro: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun fetchUserFromFirestore(uid: String) {
        try {
            val user = userRepository.getUser(uid)
            if (user != null && user.rol.isNotBlank()) {
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
