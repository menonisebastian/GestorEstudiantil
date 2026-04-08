package samf.gestorestudiantil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.Post
import samf.gestorestudiantil.data.models.Unidad
import samf.gestorestudiantil.data.models.User

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

class ProfesorViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(ProfesorState())
    val state: StateFlow<ProfesorState> = _state.asStateFlow()

    private var asignaturasListener: ListenerRegistration? = null
    private var estudiantesListener: ListenerRegistration? = null
    private var evaluacionesListener: ListenerRegistration? = null
    private var unidadesListener: ListenerRegistration? = null
    private var postsListener: ListenerRegistration? = null
    private var horariosListener: ListenerRegistration? = null

    // ====================================================================
    // 0. GESTIÓN DE UNIDADES Y POSTS (PROFESORES)
    // ====================================================================
    fun cargarUnidadesYPosts(asignaturaId: String) {
        unidadesListener?.remove()
        postsListener?.remove()

        unidadesListener = db.collection("unidades")
            .whereEqualTo("asignaturaId", asignaturaId)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    _state.update { it.copy(unidades = snapshot.toObjects(Unidad::class.java).sortedBy { u -> u.orden }) }
                }
            }

        postsListener = db.collection("posts")
            .whereEqualTo("asignaturaId", asignaturaId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _state.update { it.copy(posts = snapshot.toObjects(Post::class.java).sortedByDescending { p -> p.fechaCreacion }) }
                }
            }
    }

    fun crearUnidad(asignaturaId: String, nombre: String, descripcion: String, visible: Boolean) {
        val nuevaUnidad = Unidad(
            asignaturaId = asignaturaId,
            nombre = nombre,
            descripcion = descripcion,
            visible = visible,
            orden = (_state.value.unidades.maxOfOrNull { it.orden } ?: 0) + 1
        )
        db.collection("unidades").add(nuevaUnidad)
    }

    fun editarUnidad(unidadId: String, nombre: String, descripcion: String, visible: Boolean) {
        db.collection("unidades").document(unidadId)
            .update(
                "nombre", nombre,
                "descripcion", descripcion,
                "visible", visible
            )
    }

    fun eliminarUnidad(unidadId: String) {
        // Opcional: Eliminar también los posts asociados
        db.collection("unidades").document(unidadId).delete()
    }

    fun crearPost(asignaturaId: String, unidadId: String, titulo: String, contenido: String, autorId: String, autorNombre: String, visible: Boolean) {
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
        db.collection("posts").add(nuevoPost)
    }

    fun editarPost(postId: String, titulo: String, contenido: String, visible: Boolean) {
        db.collection("posts").document(postId)
            .update(
                "titulo", titulo,
                "contenido", contenido,
                "visible", visible
            )
    }

    fun eliminarPost(postId: String) {
        db.collection("posts").document(postId).delete()
    }

    // ====================================================================
    // 1. ASIGNATURAS QUE IMPARTE EL PROFESOR (tiempo real)
    // ====================================================================
    fun cargarAsignaturas(profesorId: String) {
        _state.update { it.copy(isLoading = true) }
        asignaturasListener?.remove()

        asignaturasListener = db.collection("asignaturas")
            .whereEqualTo("profesorId", profesorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _state.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lista = snapshot.toObjects(Asignatura::class.java)
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
        estudiantesListener?.remove()

        estudiantesListener = db.collection("usuarios")
            .whereEqualTo("rol", "ESTUDIANTE")
            .whereIn("cursoId", cursoIds)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    _state.update { it.copy(todosMisEstudiantes = snapshot.toObjects(User::class.java)) }
                }
            }
    }

    // ====================================================================
    // 3. ESTUDIANTES DE UN CURSO CONCRETO (para la pestaña Asignaturas -> Estudiantes)
    // ====================================================================
    fun cargarEstudiantesPorCurso(cursoId: String) {
        _state.update { it.copy(isLoading = true) }
        db.collection("usuarios")
            .whereEqualTo("rol", "ESTUDIANTE")
            .whereEqualTo("cursoId", cursoId)
            .get()
            .addOnSuccessListener { snapshot ->
                _state.update { it.copy(isLoading = false, estudiantes = snapshot.toObjects(User::class.java)) }
            }
            .addOnFailureListener { e ->
                _state.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }
    }

    // ====================================================================
    // 4. EVALUACIONES DE UN ALUMNO EN UNA ASIGNATURA (tiempo real)
    // ====================================================================
    fun cargarEvaluacionesEstudiante(estudianteId: String, asignaturaId: String) {
        evaluacionesListener?.remove()
        evaluacionesListener = db.collection("evaluaciones")
            .whereEqualTo("estudianteId", estudianteId)
            .whereEqualTo("asignaturaId", asignaturaId)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    _state.update { it.copy(evaluaciones = snapshot.toObjects(Evaluacion::class.java)) }
                }
            }
    }

    // ====================================================================
    // 5. GESTIÓN DE EVALUACIONES (Persistencia)
    // ====================================================================
    fun guardarEvaluacion(evaluacion: Evaluacion) {
        viewModelScope.launch {
            try {
                val docRef = if (evaluacion.id.isEmpty()) {
                    db.collection("evaluaciones").document()
                } else {
                    db.collection("evaluaciones").document(evaluacion.id)
                }
                val finalEval = evaluacion.copy(id = docRef.id)
                docRef.set(finalEval).await()
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al guardar: ${e.localizedMessage}") }
            }
        }
    }

    fun eliminarEvaluacion(evaluacion: Evaluacion) {
        viewModelScope.launch {
            try {
                db.collection("evaluaciones").document(evaluacion.id).delete().await()
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
        horariosListener?.remove()

        horariosListener = db.collection("horarios")
            .whereEqualTo("profesorId", profesorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _state.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lista = snapshot.toObjects(Horario::class.java)
                    _state.update { it.copy(isLoading = false, horarios = lista) }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        asignaturasListener?.remove()
        estudiantesListener?.remove()
        evaluacionesListener?.remove()
        horariosListener?.remove()
    }
}
