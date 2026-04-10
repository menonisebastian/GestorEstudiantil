package samf.gestorestudiantil.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.ScrapedCourse
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.AdminRepository
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : AdminRepository {

    private val gson = Gson()

    override fun getUsuariosPorCentro(centroId: String): Flow<List<User>> = callbackFlow {
        val subscription = db.collection("usuarios")
            .whereEqualTo("centroId", centroId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(User::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun aprobarUsuario(usuarioId: String) {
        db.collection("usuarios").document(usuarioId).update("estado", "ACTIVO").await()
    }

    override suspend fun eliminarUsuario(usuarioId: String) {
        db.collection("usuarios").document(usuarioId).delete().await()
    }

    override suspend fun actualizarDatosUsuario(usuarioId: String, updates: Map<String, Any?>) {
        db.collection("usuarios").document(usuarioId).update(updates).await()
    }

    override fun getCentros(): Flow<List<Centro>> = callbackFlow {
        val subscription = db.collection("centros").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                trySend(snapshot.toObjects(Centro::class.java))
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getCursosPorCentro(centroId: String): Flow<List<Curso>> = callbackFlow {
        val subscription = db.collection("cursos")
            .whereEqualTo("centroId", centroId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Curso::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getAsignaturasSinProfesor(turno: String): Flow<List<Asignatura>> = callbackFlow {
        val subscription = db.collection("asignaturas")
            .whereEqualTo("profesorId", "")
            .whereEqualTo("turno", turno.lowercase().trim())
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Asignatura::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getAsignaturasPorCurso(cursoId: String, turno: String): Flow<List<Asignatura>> = callbackFlow {
        val subscription = db.collection("asignaturas")
            .whereEqualTo("cursoId", cursoId)
            .whereEqualTo("turno", turno.lowercase().trim())
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Asignatura::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getHorarios(cursoId: String, cicloNum: Int, turno: String): Flow<List<Horario>> = callbackFlow {
        val subscription = db.collection("horarios")
            .whereEqualTo("cursoId", cursoId)
            .whereEqualTo("cicloNum", cicloNum)
            .whereEqualTo("turno", turno.lowercase().trim())
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Horario::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun asignarAsignaturaAProfesor(asignaturaId: String, profesorId: String) {
        val userDoc = db.collection("usuarios").document(profesorId).get().await()
        val nombreProfesor = userDoc.getString("nombre") ?: "Profesor desconocido"

        val updates = mapOf(
            "profesorId" to profesorId,
            "profesorNombre" to nombreProfesor
        )
        db.collection("asignaturas").document(asignaturaId).update(updates).await()
        actualizarAcronimosProfesor(profesorId)
    }

    override suspend fun desasignarAsignatura(asignaturaId: String, profesorId: String) {
        val updates = mapOf(
            "profesorId" to "",
            "profesorNombre" to ""
        )
        db.collection("asignaturas").document(asignaturaId).update(updates).await()
        actualizarAcronimosProfesor(profesorId)
    }

    private suspend fun actualizarAcronimosProfesor(profesorId: String) {
        val snapshot = db.collection("asignaturas")
            .whereEqualTo("profesorId", profesorId)
            .get().await()

        val asignaturas = snapshot.toObjects(Asignatura::class.java)
        val nuevoCursoOArea = asignaturas
            .map { it.acronimo }
            .distinct()
            .joinToString(", ")

        db.collection("usuarios").document(profesorId)
            .update("cursoOArea", nuevoCursoOArea).await()
    }

    override suspend fun guardarCentro(centro: Centro) {
        val ref = if (centro.id.isEmpty()) db.collection("centros").document() else db.collection("centros").document(centro.id)
        if (centro.id.isEmpty()) centro.id = ref.id
        ref.set(centro, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    override suspend fun eliminarCentro(centroId: String) {
        db.collection("centros").document(centroId).delete().await()
    }

    override suspend fun guardarCurso(curso: Curso) {
        val ref = if (curso.id.isEmpty()) db.collection("cursos").document() else db.collection("cursos").document(curso.id)
        if (curso.id.isEmpty()) curso.id = ref.id
        ref.set(curso, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    override suspend fun eliminarCurso(cursoId: String) {
        db.collection("cursos").document(cursoId).delete().await()
    }

    override suspend fun guardarAsignatura(asignatura: Asignatura) {
        val ref = if (asignatura.idFirestore.isEmpty()) db.collection("asignaturas").document() else db.collection("asignaturas").document(asignatura.idFirestore)
        if (asignatura.idFirestore.isEmpty()) asignatura.idFirestore = ref.id
        ref.set(asignatura, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    override suspend fun eliminarAsignatura(asignaturaId: String) {
        db.collection("asignaturas").document(asignaturaId).delete().await()
    }

    override suspend fun guardarHorario(horario: Horario) {
        horario.turno = horario.turno.lowercase().trim()
        val ref = if (horario.id.isEmpty()) db.collection("horarios").document() else db.collection("horarios").document(horario.id)
        if (horario.id.isEmpty()) horario.id = ref.id
        ref.set(horario, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    override suspend fun eliminarHorario(horarioId: String) {
        db.collection("horarios").document(horarioId).delete().await()
    }

    override suspend fun seedDatabase(jsonlLines: List<String>) {
        val idCentro = "ies_comercio"
        db.collection("centros").document(idCentro).set(
            hashMapOf(
                "id" to idCentro,
                "nombre" to "I.E.S Comercio",
                "tipo" to "Instituto de Educación Secundaria"
            )
        ).await()

        var batch = db.batch()
        var operationCount = 0

        for (line in jsonlLines) {
            if (line.trim().isEmpty()) continue
            val sc = gson.fromJson(line, ScrapedCourse::class.java)
            if (sc._status != "ok") continue

            val cursoId = "${idCentro}_${sc.acronimo ?: "DESCONOCIDO"}".replace(" ", "_")
            val cursoRef = db.collection("cursos").document(cursoId)
            val turnosNormalizados = (sc.turnos_disponibles ?: emptyList()).map { it.lowercase().trim() }

            val cursoData = hashMapOf(
                "id"                to cursoId,
                "centroId"          to idCentro,
                "acronimo"          to sc.acronimo,
                "nombre"            to sc.nombre_curso,
                "tipo"              to sc.tipo,
                "modalidad"         to (sc.modalidad ?: "presencial"),
                "turnosDisponibles" to turnosNormalizados,
                "urlInfo"           to (sc.url ?: ""),
                "horasTotalesCurso" to extraerNumero(sc.horas_totales_curso),
                "iconoName"         to (sc.iconoName ?: "School"),
                "colorFondoHex"     to (sc.colorFondoHex ?: "#D0E1FF"),
                "colorIconoHex"     to (sc.colorIconoHex ?: "#2563EB")
            )

            batch.set(cursoRef, cursoData, com.google.firebase.firestore.SetOptions.merge())
            operationCount++

            val ciclos = sc.ciclos ?: emptyList()
            val turnos = turnosNormalizados.ifEmpty { listOf("matutino") }

            for (turno in turnos) {
                for (cicloBloque in ciclos) {
                    val cicloRaw = cicloBloque.ciclo
                    val cicloNum = cicloAInt(cicloRaw)

                    for (asig in cicloBloque.asignaturas) {
                        val asigId = "${cursoId}_${cicloNum}_${asig.acronimo}_${turno}".replace(" ", "_").lowercase()
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

        if (operationCount > 0) {
            batch.commit().await()
        }
    }

    private fun extraerNumero(texto: String?): Int {
        if (texto.isNullOrEmpty()) return 0
        return texto.replace(Regex("\\D"), "").toIntOrNull() ?: 0
    }

    private fun cicloAInt(cicloStr: String?): Int {
        if (cicloStr.isNullOrEmpty()) return 1
        return cicloStr.trim().firstOrNull()?.digitToIntOrNull() ?: 1
    }
}
