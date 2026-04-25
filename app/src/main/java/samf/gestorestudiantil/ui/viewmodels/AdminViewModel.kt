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
import samf.gestorestudiantil.R
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Clase
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.AdminRepository
import samf.gestorestudiantil.domain.repositories.CourseRepository
import samf.gestorestudiantil.domain.usecases.AssignSubjectToProfessorUseCase
import samf.gestorestudiantil.domain.usecases.SeedDatabaseUseCase
import samf.gestorestudiantil.ui.utils.ErrorMapper
import javax.inject.Inject

data class AdminState(
    val isLoading: Boolean = false,
    val usuarios: List<User> = emptyList(),
    val centros: List<Centro> = emptyList(),
    val clases: List<Clase> = emptyList(),
    val cursos: List<Curso> = emptyList(),
    val asignaturas: List<Asignatura> = emptyList(),
    val asignaturasProfesor: List<Asignatura> = emptyList(),
    val asignaturasDisponibles: List<Asignatura> = emptyList(),
    val horarios: List<Horario> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val courseRepository: CourseRepository,
    private val seedDatabaseUseCase: SeedDatabaseUseCase,
    private val assignSubjectToProfessorUseCase: AssignSubjectToProfessorUseCase,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
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
                _adminState.value = _adminState.value.copy(errorMessage = context.getString(R.string.error_load_data))
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
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = ErrorMapper.getFriendlyMessage(context, e))
            }
        }
    }

    fun rechazarOEliminarUsuario(usuario: User, onUndo: () -> Unit) {
        viewModelScope.launch {
            try {
                adminRepository.eliminarUsuario(usuario.id)
                onUndo()
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = ErrorMapper.getFriendlyMessage(context, e))
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

    fun cargarClasesPorCentro(centroId: String) {
        viewModelScope.launch {
            adminRepository.getClasesPorCentro(centroId).collect { lista ->
                _adminState.value = _adminState.value.copy(clases = lista)
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

    fun cargarAsignaturasProfesor(profesorId: String) {
        viewModelScope.launch {
            adminRepository.getAsignaturasPorProfesor(profesorId).collect { lista ->
                _adminState.value = _adminState.value.copy(asignaturasProfesor = lista)
            }
        }
    }

    fun asignarAsignaturaAProfesor(asignaturaId: String, profesorId: String) {
        viewModelScope.launch {
            try {
                assignSubjectToProfessorUseCase(asignaturaId, profesorId)
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = ErrorMapper.getFriendlyMessage(context, e))
            }
        }
    }

    fun asignarTutorAClase(claseId: String, tutorId: String) {
        viewModelScope.launch {
            try {
                adminRepository.asignarTutorAClase(claseId, tutorId)
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = ErrorMapper.getFriendlyMessage(context, e))
            }
        }
    }

    fun desasignarAsignatura(asignaturaId: String, profesorId: String) {
        viewModelScope.launch {
            try {
                adminRepository.desasignarAsignatura(asignaturaId, profesorId)
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = ErrorMapper.getFriendlyMessage(context, e))
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
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = ErrorMapper.getFriendlyMessage(context, e)) }
        }
    }

    fun eliminarCentro(centro: Centro, onUndo: () -> Unit) {
        viewModelScope.launch {
            try { 
                adminRepository.eliminarCentro(centro.id)
                onUndo()
            }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = ErrorMapper.getFriendlyMessage(context, e)) }
        }
    }

    fun guardarCurso(curso: Curso) {
        viewModelScope.launch {
            try { adminRepository.guardarCurso(curso) }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = ErrorMapper.getFriendlyMessage(context, e)) }
        }
    }

    fun eliminarCurso(curso: Curso, onUndo: () -> Unit) {
        viewModelScope.launch {
            try { 
                adminRepository.eliminarCurso(curso.id)
                onUndo()
            }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = ErrorMapper.getFriendlyMessage(context, e)) }
        }
    }

    fun guardarAsignatura(asignatura: Asignatura) {
        viewModelScope.launch {
            try { adminRepository.guardarAsignatura(asignatura) }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = ErrorMapper.getFriendlyMessage(context, e)) }
        }
    }

    fun eliminarAsignatura(asignatura: Asignatura, onUndo: () -> Unit) {
        viewModelScope.launch {
            try { 
                adminRepository.eliminarAsignatura(asignatura.id)
                onUndo()
            }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = ErrorMapper.getFriendlyMessage(context, e)) }
        }
    }

    fun guardarHorario(horario: Horario) {
        viewModelScope.launch {
            try { adminRepository.guardarHorario(horario) }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = ErrorMapper.getFriendlyMessage(context, e)) }
        }
    }

    fun eliminarHorario(horario: Horario, onUndo: () -> Unit) {
        viewModelScope.launch {
            try { 
                adminRepository.eliminarHorario(horario.id)
                onUndo()
            }
            catch (e: Exception) { _adminState.value = _adminState.value.copy(errorMessage = ErrorMapper.getFriendlyMessage(context, e)) }
        }
    }

    fun recalcularContadores() {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true)
            try {
                adminRepository.recalcularTodosLosContadores()
                _adminState.value = _adminState.value.copy(isLoading = false, errorMessage = context.getString(R.string.success_counters_synced))
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(isLoading = false, errorMessage = context.getString(R.string.error_sync_counters))
            }
        }
    }

    fun generarClasesPorDefecto(centroId: String) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true)
            try {
                adminRepository.generarClasesPorDefecto(centroId)
                _adminState.value = _adminState.value.copy(isLoading = false, errorMessage = context.getString(R.string.success_classes_generated))
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(isLoading = false, errorMessage = context.getString(R.string.error_generate_classes))
            }
        }
    }

    fun guardarUsuario(user: User) {
        viewModelScope.launch {
            try {
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
                _adminState.value = _adminState.value.copy(errorMessage = ErrorMapper.getFriendlyMessage(context, e))
            }
        }
    }

    fun clearError() {
        _adminState.value = _adminState.value.copy(errorMessage = null)
    }
}
