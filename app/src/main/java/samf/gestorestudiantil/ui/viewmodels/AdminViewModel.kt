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
import samf.gestorestudiantil.domain.usecases.AssignSubjectToProfessorUseCase
import samf.gestorestudiantil.domain.usecases.SeedDatabaseUseCase
import javax.inject.Inject

data class AdminState(
    val isLoading: Boolean = false,
    val usuarios: List<User> = emptyList(),
    val centros: List<Centro> = emptyList(),
    val cursos: List<Curso> = emptyList(),
    val asignaturas: List<Asignatura> = emptyList(), // Asignaturas del curso actual
    val asignaturasDisponibles: List<Asignatura> = emptyList(), // Para diálogos de asignación
    val horarios: List<Horario> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
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

    fun aprobarUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                adminRepository.aprobarUsuario(usuarioId)
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun rechazarOEliminarUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                adminRepository.eliminarUsuario(usuarioId)
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun actualizarDatosUsuario(usuarioId: String, nuevoRol: String, nuevoCurso: String) {
        viewModelScope.launch {
            try {
                val updates = mutableMapOf<String, Any?>("rol" to nuevoRol)
                when (nuevoRol) {
                    "ESTUDIANTE" -> updates["curso"] = nuevoCurso
                    "PROFESOR" -> updates["departamento"] = nuevoCurso
                }
                adminRepository.actualizarDatosUsuario(usuarioId, updates)
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

    fun guardarUsuario(user: User) {
        viewModelScope.launch {
            try { adminRepository.actualizarDatosUsuario(user.id, mapOf("nombre" to user.nombre, "rol" to user.rol)) }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = e.localizedMessage) }
        }
    }

    fun clearError() {
        _adminState.value = _adminState.value.copy(errorMessage = null)
    }
}
