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

data class EstudianteState(
    val isLoading: Boolean = true,
    val asignaturas: List<Asignatura> = emptyList(),
    val evaluaciones: List<Evaluacion> = emptyList(),   // para CalificacionesDetalle
    val horarios: List<Horario> = emptyList(),
    val errorMessage: String? = null
)

class EstudianteViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(EstudianteState())
    val state: StateFlow<EstudianteState> = _state.asStateFlow()

    private var asignaturasListener: ListenerRegistration? = null
    private var postsListener: ListenerRegistration? = null
    private var currentUltimaVez: Map<String, Long> = emptyMap()

    // ====================================================================
    // 1. ASIGNATURAS DEL CURSO DEL ESTUDIANTE (tiempo real)
    // ====================================================================
    fun cargarAsignaturas(cursoId: String, turno: String, cicloNum: Int, ultimaVezAsignaturas: Map<String, Long> = emptyMap()) {
        currentUltimaVez = ultimaVezAsignaturas
        _state.value = _state.value.copy(isLoading = true)
        asignaturasListener?.remove()

        asignaturasListener = db.collection("asignaturas")
            .whereEqualTo("cursoId", cursoId)
            .whereEqualTo("turno", turno)
            .whereEqualTo("cicloNum", cicloNum)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar asignaturas: ${error.localizedMessage}"
                    )
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val asignaturas = snapshot.toObjects(Asignatura::class.java)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        asignaturas = asignaturas
                    )
                    
                    // Iniciar/actualizar listener de posts para cambios en tiempo real
                    observarCambiosEnPosts(asignaturas.map { it.idFirestore })
                    
                    // Conteo inicial
                    contarNotificaciones(asignaturas, currentUltimaVez)
                }
            }
    }

    private fun observarCambiosEnPosts(asignaturaIds: List<String>) {
        postsListener?.remove()
        if (asignaturaIds.isEmpty()) return

        // Escuchamos cualquier cambio en la colección de posts de estas asignaturas
        // Nota: whereIn tiene un límite de 30 elementos, suficiente para asignaturas de un curso.
        postsListener = db.collection("posts")
            .whereIn("asignaturaId", asignaturaIds)
            .addSnapshotListener { _, _ ->
                // Cuando hay cualquier cambio en posts, recalculamos notificaciones
                contarNotificaciones(_state.value.asignaturas, currentUltimaVez)
            }
    }

    fun actualizarTiemposLectura(map: Map<String, Long>) {
        if (currentUltimaVez == map) return
        currentUltimaVez = map
        contarNotificaciones(_state.value.asignaturas, map)
    }

    private fun contarNotificaciones(asignaturas: List<Asignatura>, ultimaVezAsignaturas: Map<String, Long>) {
        viewModelScope.launch {
            val nuevasAsignaturas = asignaturas.map { asignatura ->
                val lastRead = ultimaVezAsignaturas[asignatura.idFirestore] ?: 0L

                try {
                    val postsSnapshot = db.collection("posts")
                        .whereEqualTo("asignaturaId", asignatura.idFirestore)
                        .whereGreaterThan("fechaCreacion", lastRead)
                        .get()
                        .await()

                    val numNotifs = postsSnapshot.size()
                    asignatura.copy(numNotificaciones = numNotifs)
                } catch (e: Exception) {
                    asignatura
                }
            }
            _state.update { it.copy(asignaturas = nuevasAsignaturas) }
        }
    }

    fun marcarAsignaturaComoLeida(usuarioId: String, asignaturaId: String) {
        val ahora = System.currentTimeMillis()
        
        // Actualizar mapa local para feedback inmediato
        val nuevoMapa = currentUltimaVez.toMutableMap()
        nuevoMapa[asignaturaId] = ahora
        currentUltimaVez = nuevoMapa

        db.collection("usuarios").document(usuarioId)
            .update("ultimaVezAsignaturas.$asignaturaId", ahora)

        // Limpiar el contador localmente para feedback visual instantáneo
        _state.update { currentState ->
            val nuevas = currentState.asignaturas.map {
                if (it.idFirestore == asignaturaId) it.copy(numNotificaciones = 0) else it
            }
            currentState.copy(asignaturas = nuevas)
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

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        asignaturasListener?.remove()
        postsListener?.remove()
    }
}
