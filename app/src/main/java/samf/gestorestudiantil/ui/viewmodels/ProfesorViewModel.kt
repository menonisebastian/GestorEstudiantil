package samf.gestorestudiantil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.User

data class ProfesorState(
    val isLoading: Boolean = false,
    val asignaturas: List<Asignatura> = emptyList(),
    val estudiantes: List<User> = emptyList(),
    val evaluaciones: List<Evaluacion> = emptyList(),
    val errorMessage: String? = null
)

class ProfesorViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(ProfesorState())
    val state: StateFlow<ProfesorState> = _state.asStateFlow()

    fun cargarAsignaturas(profesorId: String) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val snapshot = db.collection("asignaturas")
                    .whereEqualTo("profesorId", profesorId)
                    .get().await()
                val lista = snapshot.toObjects(Asignatura::class.java)
                _state.value = _state.value.copy(isLoading = false, asignaturas = lista)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = e.localizedMessage)
            }
        }
    }

    fun cargarEstudiantesPorCurso(cursoId: String) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val snapshot = db.collection("usuarios")
                    .whereEqualTo("rol", "ESTUDIANTE")
                    .whereEqualTo("cursoId", cursoId)
                    .get().await()
                val lista = snapshot.toObjects(User::class.java)
                _state.value = _state.value.copy(isLoading = false, estudiantes = lista)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = e.localizedMessage)
            }
        }
    }

    fun cargarEvaluacionesEstudiante(estudianteId: String, asignaturaId: String) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("evaluaciones")
                    .whereEqualTo("estudianteId", estudianteId)
                    .whereEqualTo("asignaturaId", asignaturaId)
                    .get().await()
                _state.value = _state.value.copy(evaluaciones = snapshot.toObjects(Evaluacion::class.java))
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

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
                // Recargar para ver cambios
                cargarEvaluacionesEstudiante(finalEval.estudianteId, finalEval.asignaturaId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }

    fun eliminarEvaluacion(evaluacion: Evaluacion) {
        viewModelScope.launch {
            try {
                db.collection("evaluaciones").document(evaluacion.id).delete().await()
                cargarEvaluacionesEstudiante(evaluacion.estudianteId, evaluacion.asignaturaId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }
}
