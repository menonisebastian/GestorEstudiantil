package samf.gestorestudiantil.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.AdminRepository
import samf.gestorestudiantil.domain.repositories.CourseRepository
import samf.gestorestudiantil.domain.usecases.AssignSubjectToProfessorUseCase
import samf.gestorestudiantil.domain.usecases.SeedDatabaseUseCase
import javax.inject.Inject

data class AdminState(
    val isLoading: Boolean = false,
    val usuarios: List<User> = emptyList(),
    val centros: List<Centro> = emptyList(),
    val cursos: List<Curso> = emptyList(),
    val asignaturas: List<Asignatura> = emptyList(),
    val asignaturasDisponibles: List<Asignatura> = emptyList(),
    val horarios: List<Horario> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val courseRepository: CourseRepository,
    private val seedDatabaseUseCase: SeedDatabaseUseCase,
    private val assignSubjectToProfessorUseCase: AssignSubjectToProfessorUseCase
) : ViewModel() {

    private val _adminState = MutableStateFlow(AdminState())
    val adminState: StateFlow<AdminState> = _adminState.asStateFlow()

    private var currentJob: Job? = null

    fun cargarDatosDesdeJsonl(context: Context) {
        viewModelScope.launch {
            try {
                val inputStream = context.assets.open("extract.jsonl")
                val lines = inputStream.bufferedReader().readLines()
                seedDatabaseUseCase(lines)
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = "Error al cargar datos: ${e.message}")
            }
        }
    }

    fun cargarUsuariosPorCentro(centroId: String) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            adminRepository.getUsuariosPorCentro(centroId).collect { lista ->
                _adminState.value = _adminState.value.copy(usuarios = lista, isLoading = false)
            }
        }
    }

    fun aprobarUsuario(usuario: User) {
        viewModelScope.launch {
            try {
                adminRepository.aprobarUsuario(usuario.id)
                if (usuario is User.Estudiante) {
                    courseRepository.incrementStudentCount(usuario.cursoId, usuario.turno, usuario.cicloNum)
                }
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun rechazarOEliminarUsuario(usuario: User) {
        viewModelScope.launch {
            try {
                if (usuario is User.Estudiante && usuario.estado == "ACTIVO") {
                    courseRepository.decrementStudentCount(usuario.cursoId, usuario.turno, usuario.cicloNum)
                }
                adminRepository.eliminarUsuario(usuario.id)
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun cargarCentros() {
        viewModelScope.launch {
            adminRepository.getCentros().collect { lista ->
                _adminState.value = _adminState.value.copy(centros = lista)
            }
        }
    }

    fun cargarCursosPorCentro(centroId: String) {
        viewModelScope.launch {
            adminRepository.getCursosPorCentro(centroId).collect { lista ->
                _adminState.value = _adminState.value.copy(cursos = lista)
            }
        }
    }

    fun cargarAsignaturasSinProfesor(turno: String) {
        viewModelScope.launch {
            adminRepository.getAsignaturasSinProfesor(turno).collect { lista ->
                _adminState.value = _adminState.value.copy(asignaturasDisponibles = lista)
            }
        }
    }

    fun asignarAsignaturaAProfesor(asignaturaId: String, profesorId: String) {
        viewModelScope.launch {
            try {
                assignSubjectToProfessorUseCase(asignaturaId, profesorId)
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun desasignarAsignatura(asignaturaId: String, profesorId: String) {
        viewModelScope.launch {
            try {
                adminRepository.desasignarAsignatura(asignaturaId, profesorId)
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun cargarAsignaturasPorCurso(cursoId: String, turno: String) {
        viewModelScope.launch {
            adminRepository.getAsignaturasPorCurso(cursoId, turno).collect { lista ->
                _adminState.value = _adminState.value.copy(asignaturas = lista)
            }
        }
    }

    fun cargarHorariosPorCursoYCiclo(cursoId: String, cicloNum: Int, turno: String) {
        viewModelScope.launch {
            adminRepository.getHorarios(cursoId, cicloNum, turno).collect { lista ->
                _adminState.value = _adminState.value.copy(horarios = lista)
            }
        }
    }

    fun guardarCentro(centro: Centro) {
        viewModelScope.launch {
            try { adminRepository.guardarCentro(centro) }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage) }
        }
    }

    fun eliminarCentro(centro: Centro) {
        viewModelScope.launch {
            try { 
                adminRepository.eliminarCentro(centro.id)
                // Aquí se podría disparar el snackbar global si tuviéramos acceso a AppViewModel o un flow de eventos
            }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage) }
        }
    }

    fun guardarCurso(curso: Curso) {
        viewModelScope.launch {
            try { adminRepository.guardarCurso(curso) }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage) }
        }
    }

    fun eliminarCurso(curso: Curso) {
        viewModelScope.launch {
            try { 
                adminRepository.eliminarCurso(curso.id)
            }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage) }
        }
    }

    fun guardarAsignatura(asignatura: Asignatura) {
        viewModelScope.launch {
            try { adminRepository.guardarAsignatura(asignatura) }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage) }
        }
    }

    fun eliminarAsignatura(asignatura: Asignatura) {
        viewModelScope.launch {
            try { 
                adminRepository.eliminarAsignatura(asignatura.id)
            }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage) }
        }
    }

    fun guardarHorario(horario: Horario) {
        viewModelScope.launch {
            try { adminRepository.guardarHorario(horario) }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage) }
        }
    }

    fun eliminarHorario(horarioId: String) {
        viewModelScope.launch {
            try { adminRepository.eliminarHorario(horarioId) }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage) }
        }
    }

    fun recalcularContadores() {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true)
            try {
                adminRepository.recalcularTodosLosContadores()
                _adminState.value = _adminState.value.copy(isLoading = false, errorMessage = "Contadores sincronizados")
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(isLoading = false, errorMessage = "Error: ${e.message}")
            }
        }
    }

    fun guardarUsuario(user: User) {
        viewModelScope.launch {
            try {
                // Si el usuario es un estudiante y se está re-guardando (por ejemplo, al deshacer una eliminación)
                // incrementamos el contador para restaurar el valor correcto si estaba ACTIVO.
                if (user is User.Estudiante && user.estado == "ACTIVO") {
                    courseRepository.incrementStudentCount(user.cursoId, user.turno, user.cicloNum)
                }

                val updates = mutableMapOf<String, Any?>(
                    "nombre" to user.nombre,
                    "rol" to user.rol
                )
                when (user) {
                    is User.Estudiante -> {
                        updates["cursoId"] = user.cursoId
                        updates["curso"] = user.curso
                        updates["turno"] = user.turno
                        updates["cicloNum"] = user.cicloNum
                    }
                    is User.Profesor -> {
                        updates["departamento"] = user.departamento
                        updates["turno"] = user.turno
                    }
                    else -> {}
                }
                adminRepository.actualizarDatosUsuario(user.id, updates)
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun clearError() {
        _adminState.value = _adminState.value.copy(errorMessage = null)
    }
}
