package samf.gestorestudiantil.ui.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
            _authState.value = _authState.value.copy(errorMessage = "Por favor, rellena todos los campos")
            return
        }
        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                authRepository.loginWithEmail(email, pass)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false, 
                    errorMessage = "Correo o contraseña incorrectos"
                )
            }
        }
    }

    fun registerWithEmail(
        email: String, pass: String, name: String,
        rolSeleccionado: String, centroId: String, cursoId: String, cursoNombre: String, turno: String,
        ciclo: Int, imgUrl: String, departamento: String = ""
    ) {
        if (email.isBlank() || pass.isBlank() || name.isBlank() || centroId.isBlank()) {
            _authState.value = _authState.value.copy(errorMessage = "Faltan datos obligatorios")
            return
        }

        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                registerUserUseCase(
                    email, pass, name, rolSeleccionado, centroId, cursoId, cursoNombre, turno, ciclo, imgUrl, departamento
                )
            } catch (e: FirebaseAuthUserCollisionException) {
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = "Este correo ya está registrado")
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = "No se pudo completar el registro")
            }
        }
    }

    fun loginWithGoogleToken(idToken: String) {
        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                authRepository.signInWithGoogle(idToken)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = "Error al iniciar sesión con Google")
            }
        }
    }

    fun loginWithGithub(activity: Activity) {
        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                authRepository.signInWithGithub(activity)
            } catch (e: Exception) {
                val msg = e.message ?: ""
                when {
                    msg.startsWith("COLLISION_GOOGLE:") -> {
                        // El email está registrado con Google.
                        // Hacemos sign-in con Google y luego vinculamos GitHub.
                        handleGithubGoogleCollision(activity, msg.removePrefix("COLLISION_GOOGLE:"))
                    }
                    msg.startsWith("COLLISION_EMAIL:") -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            errorMessage = "Este email ya está registrado con contraseña. " +
                                    "Inicia sesión con email y contraseña primero."
                        )
                    }
                    else -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            errorMessage = "Error con GitHub Sign-In"
                        )
                    }
                }
            }
        }
    }

    private suspend fun handleGithubGoogleCollision(activity: Activity, email: String) {
        try {
            val credentialManager = androidx.credentials.CredentialManager.create(activity)
            val token = activity.getString(samf.gestorestudiantil.R.string.id_token)
            val googleToken = samf.gestorestudiantil.domain.signInWithGoogle(
                context = activity,
                credentialManager = credentialManager,
                serverClientId = token
            )

            if (googleToken == null) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Necesitas confirmar tu cuenta de Google para vincular GitHub."
                )
                return
            }

            authRepository.signInWithGoogle(googleToken)

            authRepository.linkGithubAfterReauth(activity, "")
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(
                isLoading = false,
                errorMessage = "Error al vincular GitHub con tu cuenta de Google: ${e.localizedMessage}"
            )
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
                val newUser = completeGoogleSetupUseCase(
                    password, rolSeleccionado, centroId, cursoId, cursoNombre, turno, ciclo, name, email, imgUrl, departamento
                )

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    requireGooglePasswordSetup = false,
                    user = newUser
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

    fun signOut() {
        _authState.value = _authState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                // 1. Limpiar el token de FCM en Firestore antes de cerrar sesión
                val userId = authRepository.getCurrentUserUid()
                if (userId != null) {
                    userRepository.updateFcmToken(userId, "")
                }

                // 2. Eliminar el token del dispositivo (limpia suscripciones a topics)
                try {
                    FirebaseMessaging.getInstance().deleteToken().await()
                } catch (e: Exception) {
                    // Ignorar error de red al borrar token, pero seguir con el logout
                }

                // 3. Cerrar sesión en el repositorio
                authRepository.signOut()
                _authState.value = _authState.value.copy(isLoading = false, user = null)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = "Error al cerrar sesión")
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
