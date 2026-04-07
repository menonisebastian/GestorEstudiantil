package samf.gestorestudiantil.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.ScrapedCourse
import samf.gestorestudiantil.data.models.User

data class AdminState(
    val isLoading: Boolean = true,
    val usuarios: List<User> = emptyList(),
    val centros: List<Centro> = emptyList(),
    val cursos: List<Curso> = emptyList(),
    val asignaturas: List<Asignatura> = emptyList(),
    val errorMessage: String? = null
)

class AdminViewModel : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val gson = Gson()

    private val _adminState = MutableStateFlow(AdminState())
    val adminState: StateFlow<AdminState> = _adminState.asStateFlow()

    private var usuariosListener: ListenerRegistration? = null
    private var centrosListener: ListenerRegistration? = null
    private var cursosListener: ListenerRegistration? = null
    private var asignaturasListener: ListenerRegistration? = null

    // --- FUNCIONES DE LIMPIEZA DE DATOS ---

    // Convierte "200h" -> 200, "6h/sem" -> 6
    private fun extraerNumero(texto: String?): Int {
        if (texto.isNullOrEmpty()) return 0
        return texto.replace(Regex("\\D"), "").toIntOrNull() ?: 0
    }

    // "1" -> 1, "2" -> 2, "2 (DAW)" -> 2, "2 (DAM)" -> 2, "único" -> 1
    private fun cicloAInt(cicloStr: String?): Int {
        if (cicloStr.isNullOrEmpty()) return 1
        return cicloStr.trim().firstOrNull()?.digitToIntOrNull() ?: 1
    }

    // --- FUNCIÓN PRINCIPAL DE MIGRACIÓN ---

    fun cargarDatosDesdeJsonl(context: Context) {
        viewModelScope.launch {
            try {
                val inputStream = context.assets.open("extract.jsonl")
                val lines = inputStream.bufferedReader().readLines()

                Log.d("DataSeeder", "Se encontraron ${lines.size} cursos para procesar.")

                val idCentro = "ies_comercio"
                val centroRef = db.collection("centros").document(idCentro)
                centroRef.set(
                    hashMapOf(
                        "id" to idCentro,
                        "nombre" to "I.E.S Comercio",
                        "tipo" to "Instituto de Educación Secundaria"
                    )
                ).await()

                // INICIALIZAMOS EL BATCH
                var batch = db.batch()
                var operationCount = 0

                for (line in lines) {
                    if (line.trim().isEmpty()) continue

                    val sc = gson.fromJson(line, ScrapedCourse::class.java)
                    if (sc._status != "ok") continue

                    // ID DETERMINISTA PARA CURSO (Centro + Acrónimo)
                    val cursoId = "${idCentro}_${sc.acronimo ?: "DESCONOCIDO"}".replace(" ", "_")
                    val cursoRef = db.collection("cursos").document(cursoId)

                    val cursoData = hashMapOf(
                        "id"                to cursoId,
                        "centroId"          to idCentro,
                        "acronimo"          to sc.acronimo,
                        "nombre"            to sc.nombre_curso,
                        "tipo"              to sc.tipo,
                        "modalidad"         to (sc.modalidad ?: "presencial"),
                        "turnosDisponibles" to (sc.turnos_disponibles ?: emptyList<String>()),
                        "urlInfo"           to (sc.url ?: ""),
                        "horasTotalesCurso" to extraerNumero(sc.horas_totales_curso),
                        "iconoName"         to (sc.iconoName ?: "School"),
                        "colorFondoHex"     to (sc.colorFondoHex ?: "#D0E1FF"),
                        "colorIconoHex"     to (sc.colorIconoHex ?: "#2563EB")
                    )

                    batch.set(cursoRef, cursoData, com.google.firebase.firestore.SetOptions.merge())
                    operationCount++

                    val ciclos = sc.ciclos ?: emptyList()
                    val turnos = sc.turnos_disponibles ?: listOf("matutino")

                    for (turno in turnos) {
                        for (cicloBloque in ciclos) {
                            val cicloRaw = cicloBloque.ciclo
                            val cicloNum = cicloAInt(cicloRaw)

                            val asignaturasList = cicloBloque.asignaturas
                            for (asig in asignaturasList) {
                                // ID DETERMINISTA PARA ASIGNATURA: CURSOID_CICLO_ACRONIMO_TURNO
                                val asigId = "${cursoId}_${cicloNum}_${asig.acronimo}_${turno}".replace(" ", "_")
                                val asigRef = db.collection("asignaturas").document(asigId)

                                val asigData = hashMapOf(
                                    "id"             to asigId,
                                    "cursoId"        to cursoId,
                                    "centroId"       to idCentro,
                                    "acronimo"       to asig.acronimo,
                                    "nombre"         to asig.nombre,
                                    "ciclo"          to cicloRaw,
                                    "cicloNum"       to cicloNum,
                                    "turno"          to turno,
                                    "horasTotales"   to extraerNumero(asig.horas_totales),
                                    "horasSemanales" to extraerNumero(asig.horas_semanales),
                                    "iconoName"      to (asig.iconoName ?: "Class"),
                                    "colorFondoHex"  to (asig.colorFondoHex ?: "#E8E8E8"),
                                    "colorIconoHex"  to (asig.colorIconoHex ?: "#6B7280")
                                )

                                batch.set(asigRef, asigData, com.google.firebase.firestore.SetOptions.merge())
                                operationCount++

                                if (operationCount >= 400) {
                                    batch.commit().await()
                                    batch = db.batch()
                                    operationCount = 0
                                }
                            }
                        }
                    }
                }

                // Subimos lo que haya quedado pendiente
                if (operationCount > 0) {
                    batch.commit().await()
                }

                Log.d("DataSeeder", "¡Proceso de carga finalizado con éxito!")

            } catch (e: Exception) {
                Log.e("DataSeeder", "Error al cargar datos: ${e.message}", e)
            }
        }
    }

    // ====================================================================
    // 1. CARGAR USUARIOS DEL INSTITUTO (EN TIEMPO REAL)
    // ====================================================================
    fun cargarUsuariosPorCentro(centroId: String) {
        _adminState.value = _adminState.value.copy(isLoading = true)

        usuariosListener?.remove()

        usuariosListener = db.collection("usuarios")
            .whereEqualTo("centroId", centroId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al obtener usuarios: ${error.localizedMessage}"
                    )
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val listaUsuarios = snapshot.toObjects(User::class.java)
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        usuarios = listaUsuarios,
                        errorMessage = null
                    )
                }
            }
    }

    // ====================================================================
    // 2. APROBAR USUARIO PENDIENTE
    // ====================================================================
    fun aprobarUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(usuarioId)
                    .update("estado", "ACTIVO").await()
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(
                    errorMessage = "Error al aprobar usuario: ${e.localizedMessage}"
                )
            }
        }
    }

    // ====================================================================
    // 3. RECHAZAR O ELIMINAR USUARIO
    // ====================================================================
    fun rechazarOEliminarUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(usuarioId).delete().await()
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(
                    errorMessage = "Error al eliminar usuario: ${e.localizedMessage}"
                )
            }
        }
    }

    // ====================================================================
    // 4. CAMBIAR ROL O CURSO DE UN USUARIO (Opcional para edición)
    // ====================================================================
    fun actualizarDatosUsuario(usuarioId: String, nuevoRol: String, nuevoCurso: String) {
        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "rol" to nuevoRol,
                    "cursoOArea" to nuevoCurso
                )
                db.collection("usuarios").document(usuarioId).update(updates).await()
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(
                    errorMessage = "Error al actualizar usuario: ${e.localizedMessage}"
                )
            }
        }
    }

    // ====================================================================
    // 5. GESTIÓN DE CENTROS, CURSOS Y ASIGNATURAS
    // ====================================================================

    fun cargarCentros() {
        _adminState.value = _adminState.value.copy(isLoading = true)
        centrosListener?.remove()
        centrosListener = db.collection("centros")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar centros: ${error.localizedMessage}"
                    )
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lista = snapshot.toObjects(Centro::class.java)
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        centros = lista
                    )
                }
            }
    }

    fun cargarCursosPorCentro(centroId: String) {
        _adminState.value = _adminState.value.copy(isLoading = true)
        cursosListener?.remove()
        cursosListener = db.collection("cursos")
            .whereEqualTo("centroId", centroId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar cursos: ${error.localizedMessage}"
                    )
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lista = snapshot.toObjects(Curso::class.java)
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        cursos = lista
                    )
                }
            }
    }

    fun cargarAsignaturasSinProfesor(turno: String) {
        _adminState.value = _adminState.value.copy(isLoading = true)
        asignaturasListener?.remove()
        asignaturasListener = db.collection("asignaturas")
            .whereEqualTo("profesorId", "")
            .whereEqualTo("turno", turno)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar asignaturas: ${error.localizedMessage}"
                    )
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lista = snapshot.toObjects(Asignatura::class.java)
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        asignaturas = lista
                    )
                }
            }
    }

    fun asignarAsignaturaAProfesor(asignaturaId: String, profesorId: String) {
        viewModelScope.launch {
            try {
                // 1. Obtener el nombre del profesor desde la colección de usuarios
                val userDoc = db.collection("usuarios").document(profesorId).get().await()
                val nombreProfesor = userDoc.getString("nombre") ?: "Profesor desconocido"

                // 2. Actualizar ID y Nombre en la asignatura
                val updates = mapOf(
                    "profesorId" to profesorId,
                    "profesorNombre" to nombreProfesor
                )
                db.collection("asignaturas").document(asignaturaId).update(updates).await()
                
                // Actualizar los acrónimos en el perfil del profesor
                actualizarAcronimosProfesor(profesorId)
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(
                    errorMessage = "Error al asignar asignatura: ${e.localizedMessage}"
                )
            }
        }
    }

    fun desasignarAsignatura(asignaturaId: String, profesorId: String) {
        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "profesorId" to "",
                    "profesorNombre" to ""
                )
                db.collection("asignaturas").document(asignaturaId)
                    .update(updates).await()
                
                // Actualizar los acrónimos en el perfil del profesor
                actualizarAcronimosProfesor(profesorId)
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(
                    errorMessage = "Error al desasignar asignatura: ${e.localizedMessage}"
                )
            }
        }
    }

    private suspend fun actualizarAcronimosProfesor(profesorId: String) {
        try {
            // 1. Obtener todas las asignaturas actuales del profesor
            val snapshot = db.collection("asignaturas")
                .whereEqualTo("profesorId", profesorId)
                .get()
                .await()

            val asignaturas = snapshot.toObjects(Asignatura::class.java)

            // 2. Unir acrónimos (ej: "SGE, PMDM, AD")
            val nuevoCursoOArea = asignaturas
                .map { it.acronimo }
                .distinct()
                .joinToString(", ")

            // 3. Actualizar el documento del usuario
            db.collection("usuarios").document(profesorId)
                .update("cursoOArea", nuevoCursoOArea)
                .await()

            Log.d("AdminViewModel", "Acrónimos actualizados para $profesorId: $nuevoCursoOArea")
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Error al actualizar acrónimos: ${e.message}")
        }
    }

    fun cargarAsignaturasPorCurso(cursoId: String, turno: String) {
        _adminState.value = _adminState.value.copy(isLoading = true)
        asignaturasListener?.remove()
        asignaturasListener = db.collection("asignaturas")
            .whereEqualTo("cursoId", cursoId)
            .whereEqualTo("turno", turno)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar asignaturas: ${error.localizedMessage}"
                    )
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lista = snapshot.toObjects(Asignatura::class.java)
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        asignaturas = lista
                    )
                }
            }
    }

    fun clearError() {
        _adminState.value = _adminState.value.copy(errorMessage = null)
    }

    // --- CRUD OPERACIONES ---

    fun guardarCentro(centro: Centro) {
        viewModelScope.launch {
            try {
                val collection = db.collection("centros")
                val ref = if (centro.id.isEmpty()) collection.document() else collection.document(centro.id)
                if (centro.id.isEmpty()) centro.id = ref.id
                ref.set(centro, com.google.firebase.firestore.SetOptions.merge()).await()
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = "Error al guardar centro: ${e.localizedMessage}")
            }
        }
    }

    fun eliminarCentro(centroId: String) {
        viewModelScope.launch {
            try {
                db.collection("centros").document(centroId).delete().await()
                // Nota: Idealmente deberías eliminar cursos y asignaturas asociadas o advertir al usuario
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = "Error al eliminar centro: ${e.localizedMessage}")
            }
        }
    }

    fun guardarCurso(curso: Curso) {
        viewModelScope.launch {
            try {
                val collection = db.collection("cursos")
                val ref = if (curso.id.isEmpty()) collection.document() else collection.document(curso.id)
                if (curso.id.isEmpty()) curso.id = ref.id
                ref.set(curso, com.google.firebase.firestore.SetOptions.merge()).await()
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = "Error al guardar curso: ${e.localizedMessage}")
            }
        }
    }

    fun eliminarCurso(cursoId: String) {
        viewModelScope.launch {
            try {
                db.collection("cursos").document(cursoId).delete().await()
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = "Error al eliminar curso: ${e.localizedMessage}")
            }
        }
    }

    fun guardarAsignatura(asignatura: Asignatura) {
        viewModelScope.launch {
            try {
                val collection = db.collection("asignaturas")
                val ref = if (asignatura.idFirestore.isEmpty()) collection.document() else collection.document(asignatura.idFirestore)
                if (asignatura.idFirestore.isEmpty()) asignatura.idFirestore = ref.id
                ref.set(asignatura, com.google.firebase.firestore.SetOptions.merge()).await()
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = "Error al guardar asignatura: ${e.localizedMessage}")
            }
        }
    }

    fun eliminarAsignatura(asignaturaId: String) {
        viewModelScope.launch {
            try {
                db.collection("asignaturas").document(asignaturaId).delete().await()
            } catch (e: Exception) {
                _adminState.value = _adminState.value.copy(errorMessage = "Error al eliminar asignatura: ${e.localizedMessage}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        usuariosListener?.remove()
        centrosListener?.remove()
        cursosListener?.remove()
        asignaturasListener?.remove()
    }
}