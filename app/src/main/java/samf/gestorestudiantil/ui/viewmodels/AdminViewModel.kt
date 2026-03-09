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

    // Convierte el campo "ciclo" del JSONL a Int para Firestore
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

                // 1. Crear o verificar el Centro principal (solo se hace una vez)
                val idCentro = "ies_comercio"
                val centroRef = db.collection("centros").document(idCentro)
                val centroData = hashMapOf(
                    "id" to idCentro,
                    "nombre" to "I.E.S Comercio",
                    "tipo" to "Instituto de Educación Secundaria"
                )
                centroRef.set(centroData).await()

                // 2. Procesar cada curso scrapeado
                for (line in lines) {
                    if (line.trim().isEmpty()) continue

                    val scrapedCourse = gson.fromJson(line, ScrapedCourse::class.java)
                    if (scrapedCourse._status != "ok") continue

                    val nombreBase = scrapedCourse.nombre_curso
                        .replace("FPGS – ", "")
                        .replace("FPGM – ", "")

                    val materias = scrapedCourse.materias ?: emptyList()

                    // Agrupar materias por el valor normalizado del campo "ciclo"
                    val materiasPorCiclo = materias.groupBy { it.ciclo ?: "único" }

                    for ((cicloRaw, materiasCiclo) in materiasPorCiclo) {
                        val cursoRef = db.collection("cursos").document()

                        // Nombre del curso: incluye el ciclo si hay más de uno
                        val nombreCurso = if (materiasPorCiclo.size > 1)
                            "$nombreBase ($cicloRaw)"
                        else
                            nombreBase

                        val cursoData = hashMapOf(
                            "id" to cursoRef.id,
                            "centroId" to idCentro,
                            "nombre" to nombreCurso,
                            "tipo" to scrapedCourse.tipo,
                            "modalidad" to (scrapedCourse.modalidad ?: "presencial"),
                            "ciclo" to cicloRaw,
                            "cicloNum" to cicloAInt(cicloRaw),
                            "horasTotalesCurso" to extraerNumero(scrapedCourse.horas_totales_curso),
                            "urlInfo" to (scrapedCourse.url ?: ""),
                            "turnos" to (scrapedCourse.turnos_disponibles ?: emptyList())
                        )
                        cursoRef.set(cursoData).await()

                        // Crear las Asignaturas para este ciclo
                        for (materia in materiasCiclo) {
                            val asignaturaRef = db.collection("asignaturas").document()

                            val asignaturaData = hashMapOf(
                                "id" to asignaturaRef.id,
                                "cursoId" to cursoRef.id,
                                "nombre" to materia.materia,
                                "ciclo" to cicloRaw,
                                "cicloNum" to cicloAInt(cicloRaw),
                                "horasTotales" to extraerNumero(materia.horas_totales),
                                "horasSemanales" to extraerNumero(materia.horas_semanales),
                                "profesorId" to ""
                            )
                            asignaturaRef.set(asignaturaData).await()
                        }
                        Log.d("DataSeeder", "Curso subido: $nombreCurso")
                    }
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

    fun cargarAsignaturasPorCurso(cursoId: String) {
        _adminState.value = _adminState.value.copy(isLoading = true)
        asignaturasListener?.remove()
        asignaturasListener = db.collection("asignaturas")
            .whereEqualTo("cursoId", cursoId)
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

    override fun onCleared() {
        super.onCleared()
        usuariosListener?.remove()
        centrosListener?.remove()
        cursosListener?.remove()
        asignaturasListener?.remove()
    }
}