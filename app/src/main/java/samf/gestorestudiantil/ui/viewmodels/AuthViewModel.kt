package samf.gestorestudiantil.ui.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    val isCheckingSession: Boolean = true,
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
        loadCentros()
        observeAuthState()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.getAuthStateFlow().flatMapLatest { uid ->
                if (uid == null) {
                    flowOf(null)
                } else {
                    userRepository.getUserFlow(uid).catch { emit(null) }
                }
            }.collect { user ->
                val firebaseUid = authRepository.getCurrentUserUid()
                if (firebaseUid == null) {
                    _authState.value = _authState.value.copy(
                        isCheckingSession = false,
                        isLoading = false,
                        isSuccess = false,
                        user = null,
                        requireGooglePasswordSetup = false
                    )
                } else {
                    if (user != null && user.rol.isNotBlank()) {
                        _authState.value = _authState.value.copy(
                            isCheckingSession = false,
                            isSuccess = true,
                            isLoading = false,
                            user = user
                        )
                    } else {
                        // Si el usuario existe en Auth pero no en Firestore o no tiene rol
                        _authState.value = _authState.value.copy(
                            isCheckingSession = false,
                            isLoading = false,
                            requireGooglePasswordSetup = true,
                            user = user ?: User.Incompleto(
                                id = firebaseUid,
                                nombre = authRepository.getCurrentUserName() ?: "",
                                email = authRepository.getCurrentUserEmail() ?: "",
                                imgUrl = authRepository.getCurrentUserPhotoUrl() ?: "",
                                rol = ""
                            )
                        )
                    }
                }
            }
        }
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

    fun loginWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = _authState.value.copy(errorMessage = "Rellena todos los campos")
            return
        }
        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                authRepository.loginWithEmail(email, pass)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = "Credenciales incorrectas")
            }
        }
    }

    fun registerWithEmail(
        email: String, pass: String, name: String,
        rolSeleccionado: String, centroId: String, cursoId: String, cursoNombre: String, turno: String,
        ciclo: Int, imgUrl: String
    ) {
        if (email.isBlank() || pass.isBlank() || name.isBlank() || centroId.isBlank()) {
            _authState.value = _authState.value.copy(errorMessage = "Faltan datos obligatorios")
            return
        }

        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                registerUserUseCase(
                    email, pass, name, rolSeleccionado, centroId, cursoId, cursoNombre, turno, ciclo, imgUrl
                )
            } catch (e: FirebaseAuthUserCollisionException) {
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = "El correo ya está registrado.")
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = "Error en registro: ${e.message}")
            }
        }
    }

    fun loginWithGoogleToken(idToken: String) {
        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                authRepository.signInWithGoogle(idToken)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error con Google Sign-In")
            }
        }
    }

    fun loginWithGithub(activity: Activity) {
        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                authRepository.signInWithGithub(activity)

                // Temporizador de seguridad para evitar carga infinita si Firestore no responde
                launch {
                    delay(15000)
                    if (_authState.value.isLoading) {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            errorMessage = "Autenticación exitosa, pero hay problemas de conexión con la base de datos."
                        )
                    }
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error con GitHub Sign-In")
            }
        }
    }

    fun handleGitHubLoginSuccess(uid: String, name: String?, email: String?, photoUrl: String?) {
        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val user = userRepository.getUser(uid)
                if (user != null && user.rol.isNotBlank()) {
                    _authState.value = _authState.value.copy(isSuccess = true, user = user, isLoading = false)
                } else {
                    val githubUser = User.Incompleto(
                        id = uid,
                        nombre = name ?: "",
                        email = email ?: "",
                        imgUrl = photoUrl ?: "",
                        rol = ""
                    )
                    _authState.value = _authState.value.copy(requireGooglePasswordSetup = true, user = githubUser, isLoading = false)
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = "Error verificando usuario: ${e.localizedMessage}")
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
        ciclo: Int,
        name: String,
        email: String,
        imgUrl: String,
        departamento: String = "Sin asignar"
    ) {
        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                completeGoogleSetupUseCase(
                    password, rolSeleccionado, centroId, cursoId, cursoNombre, turno, ciclo, name, email, imgUrl, departamento
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = "Error al completar registro: ${e.localizedMessage}")
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            try {
                authRepository.sendPasswordResetEmail(email)
                _authState.value = _authState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }

    fun updateFcmToken(userId: String, token: String) {
        viewModelScope.launch {
            try {
                userRepository.updateFcmToken(userId, token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
