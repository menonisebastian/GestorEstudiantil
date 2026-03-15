package samf.gestorestudiantil.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.Recordatorio

data class EstudianteState(
    val isLoading: Boolean = true,
    val asignaturas: List<Asignatura> = emptyList(),
    val evaluaciones: List<Evaluacion> = emptyList(),   // para CalificacionesDetalle
    val horarios: List<Horario> = emptyList(),
    val recordatorios: List<Recordatorio> = emptyList(),
    val errorMessage: String? = null
)

class EstudianteViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(EstudianteState())
    val state: StateFlow<EstudianteState> = _state.asStateFlow()

    private var asignaturasListener: ListenerRegistration? = null
    private var recordatoriosListener: ListenerRegistration? = null

    // ====================================================================
    // 1. ASIGNATURAS DEL CURSO DEL ESTUDIANTE (tiempo real)
    // ====================================================================
    fun cargarAsignaturas(cursoId: String) {
        _state.value = _state.value.copy(isLoading = true)
        asignaturasListener?.remove()

        asignaturasListener = db.collection("asignaturas")
            .whereEqualTo("cursoId", cursoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar asignaturas: ${error.localizedMessage}"
                    )
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        asignaturas = snapshot.toObjects(Asignatura::class.java)
                    )
                }
            }
    }

    // ====================================================================
    // 2. EVALUACIONES DE UNA ASIGNATURA CONCRETA (una sola vez)
    // ====================================================================
    fun cargarEvaluaciones(asignaturaId: String) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val snapshot = db.collection("evaluaciones")
                    .whereEqualTo("asignaturaId", asignaturaId)
                    .get().await()
                _state.value = _state.value.copy(
                    isLoading = false,
                    evaluaciones = snapshot.toObjects(Evaluacion::class.java)
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar evaluaciones: ${e.localizedMessage}"
                )
            }
        }
    }

    // ====================================================================
    // 3. RECORDATORIOS DEL ESTUDIANTE (tiempo real)
    // ====================================================================
    fun cargarRecordatorios(usuarioId: String) {
        recordatoriosListener?.remove()

        recordatoriosListener = db.collection("recordatorios")
            .whereEqualTo("usuarioId", usuarioId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _state.value = _state.value.copy(
                        errorMessage = "Error al cargar recordatorios: ${error.localizedMessage}"
                    )
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _state.value = _state.value.copy(
                        recordatorios = snapshot.toObjects(Recordatorio::class.java)
                    )
                }
            }
    }

    // ====================================================================
    // 4. AÑADIR RECORDATORIO
    // ====================================================================
    fun añadirRecordatorio(recordatorio: Recordatorio) {
        viewModelScope.launch {
            try {
                db.collection("recordatorios")
                    .document(recordatorio.id)
                    .set(recordatorio)
                    .await()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Error al guardar recordatorio: ${e.localizedMessage}"
                )
            }
        }
    }

    // ====================================================================
    // 5. ELIMINAR RECORDATORIO
    // ====================================================================
    fun eliminarRecordatorio(recordatorioId: String) {
        viewModelScope.launch {
            try {
                db.collection("recordatorios")
                    .document(recordatorioId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Error al eliminar recordatorio: ${e.localizedMessage}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        asignaturasListener?.remove()
        recordatoriosListener?.remove()
    }
}