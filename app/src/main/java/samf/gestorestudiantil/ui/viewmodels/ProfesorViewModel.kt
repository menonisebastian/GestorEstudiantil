package samf.gestorestudiantil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.Post
import samf.gestorestudiantil.data.models.Unidad
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.ProfesorRepository
import javax.inject.Inject

data class ProfesorState(
    val isLoading: Boolean = false,
    val asignaturas: List<Asignatura> = emptyList(),
    val todosMisEstudiantes: List<User> = emptyList(),
    val estudiantes: List<User> = emptyList(),
    val evaluaciones: List<Evaluacion> = emptyList(),
    val unidades: List<Unidad> = emptyList(),
    val posts: List<Post> = emptyList(),
    val horarios: List<Horario> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ProfesorViewModel @Inject constructor(
    private val profesorRepository: ProfesorRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfesorState())
    val state: StateFlow<ProfesorState> = _state.asStateFlow()

    // ====================================================================
    // 0. GESTIÓN DE UNIDADES Y POSTS (PROFESORES)
    // ====================================================================
    fun cargarUnidadesYPosts(asignaturaId: String) {
        viewModelScope.launch {
            launch {
                profesorRepository.getUnidades(asignaturaId).collect { lista ->
                    _state.update { it.copy(unidades = lista) }
                }
            }
            launch {
                profesorRepository.getPosts(asignaturaId).collect { lista ->
                    _state.update { it.copy(posts = lista) }
                }
            }
        }
    }

    fun crearUnidad(asignaturaId: String, nombre: String, descripcion: String, visible: Boolean) {
        viewModelScope.launch {
            val nuevaUnidad = Unidad(
                asignaturaId = asignaturaId,
                nombre = nombre,
                descripcion = descripcion,
                visible = visible,
                orden = (_state.value.unidades.maxOfOrNull { it.orden } ?: 0) + 1
            )
            profesorRepository.crearUnidad(nuevaUnidad)
        }
    }

    fun editarUnidad(unidadId: String, nombre: String, descripcion: String, visible: Boolean) {
        viewModelScope.launch {
            profesorRepository.editarUnidad(unidadId, nombre, descripcion, visible)
        }
    }

    fun eliminarUnidad(unidadId: String) {
        viewModelScope.launch {
            profesorRepository.eliminarUnidad(unidadId)
        }
    }

    fun crearPost(asignaturaId: String, unidadId: String, titulo: String, contenido: String, autorId: String, autorNombre: String, visible: Boolean) {
        viewModelScope.launch {
            val nuevoPost = Post(
                asignaturaId = asignaturaId,
                unidadId = unidadId,
                titulo = titulo,
                contenido = contenido,
                autorId = autorId,
                autorNombre = autorNombre,
                fechaCreacion = System.currentTimeMillis(),
                visible = visible
            )
            profesorRepository.crearPost(nuevoPost)
        }
    }

    fun editarPost(postId: String, titulo: String, contenido: String, visible: Boolean) {
        viewModelScope.launch {
            profesorRepository.editarPost(postId, titulo, contenido, visible)
        }
    }

    fun eliminarPost(postId: String) {
        viewModelScope.launch {
            profesorRepository.eliminarPost(postId)
        }
    }

    // ====================================================================
    // 1. ASIGNATURAS QUE IMPARTE EL PROFESOR (tiempo real)
    // ====================================================================
    fun cargarAsignaturas(profesorId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            profesorRepository.getAsignaturas(profesorId).collect { lista ->
                _state.update { it.copy(isLoading = false, asignaturas = lista) }
                // Al cargar asignaturas, necesitamos conocer a todos los estudiantes de esos cursos para la pestaña global
                cargarTodosMisEstudiantes(lista.map { it.cursoId }.distinct())
            }
        }
    }

    // ====================================================================
    // 2. TODOS LOS ESTUDIANTES DE LOS CURSOS DONDE IMPARTE (tiempo real)
    // ====================================================================
    fun cargarTodosMisEstudiantes(cursoIds: List<String>) {
        if (cursoIds.isEmpty()) {
            _state.update { it.copy(todosMisEstudiantes = emptyList()) }
            return
        }
        viewModelScope.launch {
            profesorRepository.getEstudiantesPorCursos(cursoIds).collect { lista ->
                _state.update { it.copy(todosMisEstudiantes = lista) }
            }
        }
    }

    // ====================================================================
    // 3. ESTUDIANTES DE UN CURSO CONCRETO (para la pestaña Asignaturas -> Estudiantes)
    // ====================================================================
    fun cargarEstudiantesPorCurso(cursoId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val estudiantes = profesorRepository.getEstudiantesPorCurso(cursoId)
                _state.update { it.copy(isLoading = false, estudiantes = estudiantes) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }
        }
    }

    // ====================================================================
    // 4. EVALUACIONES DE UN ALUMNO EN UNA ASIGNATURA (tiempo real)
    // ====================================================================
    fun cargarEvaluacionesEstudiante(estudianteId: String, asignaturaId: String) {
        viewModelScope.launch {
            profesorRepository.getEvaluacionesEstudiante(estudianteId, asignaturaId).collect { lista ->
                _state.update { it.copy(evaluaciones = lista) }
            }
        }
    }

    // ====================================================================
    // 5. GESTIÓN DE EVALUACIONES (Persistencia)
    // ====================================================================
    fun guardarEvaluacion(evaluacion: Evaluacion) {
        viewModelScope.launch {
            try {
                profesorRepository.guardarEvaluacion(evaluacion)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al guardar: ${e.localizedMessage}") }
            }
        }
    }

    fun eliminarEvaluacion(evaluacion: Evaluacion) {
        viewModelScope.launch {
            try {
                profesorRepository.eliminarEvaluacion(evaluacion.id)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al eliminar: ${e.localizedMessage}") }
            }
        }
    }

    // ====================================================================
    // 6. HORARIOS DEL PROFESOR (tiempo real)
    // ====================================================================
    fun cargarHorariosProfesor(profesorId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            profesorRepository.getHorarios(profesorId).collect { lista ->
                _state.update { it.copy(isLoading = false, horarios = lista) }
            }
        }
    }
}
