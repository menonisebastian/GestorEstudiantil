package samf.gestorestudiantil.data.repositories

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Clase
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.ScrapedCourse
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.AdminRepository
import samf.gestorestudiantil.R
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : AdminRepository {

    private val gson = Gson()

    override fun getUsuariosPorCentro(centroId: String): Flow<List<User>> = callbackFlow {
        val subscription = db.collection("usuarios")
            .whereEqualTo("centroId", centroId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { doc ->
                        val rol = doc.getString("rol")
                        when (rol) {
                            "ESTUDIANTE" -> doc.toObject(User.Estudiante::class.java)
                            "PROFESOR" -> doc.toObject(User.Profesor::class.java)
                            "ADMIN" -> doc.toObject(User.Admin::class.java)
                            else -> doc.toObject(User.Incompleto::class.java)
                        }
                    }
                    trySend(users)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun aprobarUsuario(usuarioId: String) {
        val userRef = db.collection("usuarios").document(usuarioId)
        val userSnap = userRef.get().await()

        // Actualizamos el estado a ACTIVO
        userRef.update("estado", "ACTIVO").await()

        // Si es estudiante, lo añadimos a su clase correspondiente
        if (userSnap.getString("rol") == "ESTUDIANTE") {
            val cursoId = userSnap.getString("cursoId") ?: ""
            val turno = userSnap.getString("turno") ?: ""
            val cicloNum = userSnap.getLong("cicloNum")?.toInt() ?: 1

            val clasesQuery = db.collection("clases")
                .whereEqualTo("cursoGlobalId", cursoId)
                .whereEqualTo("turno", turno.lowercase().trim())
                .whereEqualTo("cicloNum", cicloNum)
                .get().await()

            // arrayUnion evita duplicados automáticamente
            for (doc in clasesQuery.documents) {
                doc.reference.update("estudiantesIds", FieldValue.arrayUnion(usuarioId))
            }
        }
    }

    override suspend fun eliminarUsuario(usuarioId: String) {
        val userRef = db.collection("usuarios").document(usuarioId)
        val userSnap = userRef.get().await()

        // Si era un estudiante, lo sacamos de la clase antes de eliminarlo
        if (userSnap.getString("rol") == "ESTUDIANTE") {
            val cursoId = userSnap.getString("cursoId") ?: ""
            val turno = userSnap.getString("turno") ?: ""
            val cicloNum = userSnap.getLong("cicloNum")?.toInt() ?: 1

            val clasesQuery = db.collection("clases")
                .whereEqualTo("cursoGlobalId", cursoId)
                .whereEqualTo("turno", turno.lowercase().trim())
                .whereEqualTo("cicloNum", cicloNum)
                .get().await()

            // arrayRemove saca ese ID específico de la lista
            for (doc in clasesQuery.documents) {
                doc.reference.update("estudiantesIds", FieldValue.arrayRemove(usuarioId))
            }
        }

        // Finalmente eliminamos el usuario
        userRef.delete().await()
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

    override fun getClasesPorCentro(centroId: String): Flow<List<Clase>> = callbackFlow {
        val subscription = db.collection("clases")
            .whereEqualTo("centroId", centroId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Clase::class.java))
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
        val query = db.collection("asignaturas")
            .whereEqualTo("profesorId", "")
            
        val finalQuery = if (turno.isNotEmpty()) {
            query.whereEqualTo("turno", turno.lowercase().trim())
        } else {
            query
        }

        val subscription = finalQuery.addSnapshotListener { snapshot, _ ->
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
        val nombreProfesor = userDoc.getString("nombre") ?: context.getString(R.string.label_unknown_professor)

        // 1. Actualizar la asignatura
        val updates = mapOf(
            "profesorId" to profesorId,
            "profesorNombre" to nombreProfesor
        )
        db.collection("asignaturas").document(asignaturaId).update(updates).await()
        
        // 2. Actualizar todos los slots de horario que referencian esta asignatura
        val horariosSnapshot = db.collection("horarios")
            .whereEqualTo("asignaturaId", asignaturaId)
            .get().await()
            
        val batch = db.batch()
        for (doc in horariosSnapshot.documents) {
            batch.update(doc.reference, mapOf(
                "profesorId" to profesorId,
                "profesorNombre" to nombreProfesor
            ))
        }
        batch.commit().await()

        actualizarAcronimosProfesor(profesorId)
    }

    override suspend fun desasignarAsignatura(asignaturaId: String, profesorId: String) {
        // 1. Limpiar la asignatura
        val updates = mapOf(
            "profesorId" to "",
            "profesorNombre" to ""
        )
        db.collection("asignaturas").document(asignaturaId).update(updates).await()

        // 2. Limpiar todos los slots de horario que referencian esta asignatura
        val horariosSnapshot = db.collection("horarios")
            .whereEqualTo("asignaturaId", asignaturaId)
            .get().await()

        val batch = db.batch()
        for (doc in horariosSnapshot.documents) {
            batch.update(doc.reference, mapOf(
                "profesorId" to "",
                "profesorNombre" to ""
            ))
        }
        batch.commit().await()

        actualizarAcronimosProfesor(profesorId)
    }

    override suspend fun asignarTutorAClase(claseId: String, tutorId: String) {
        db.collection("clases").document(claseId).update("tutorId", tutorId).await()
    }

    private suspend fun actualizarAcronimosProfesor(profesorId: String) {
        val snapshot = db.collection("asignaturas")
            .whereEqualTo("profesorId", profesorId)
            .get().await()

        val asignaturas = snapshot.toObjects(Asignatura::class.java)

        val listaIdsAsignaturas = asignaturas.map { it.id }

        // Actualizamos el perfil del profesor con ambos campos
        db.collection("usuarios").document(profesorId)
            .update(
                mapOf(
                    "asignaturasImpartidas" to listaIdsAsignaturas
                )
            ).await()
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
        val ref = if (asignatura.id.isEmpty()) db.collection("asignaturas").document() else db.collection("asignaturas").document(asignatura.id)
        if (asignatura.id.isEmpty()) asignatura.id = ref.id
        ref.set(asignatura, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    override suspend fun eliminarAsignatura(asignaturaId: String) {
        db.collection("asignaturas").document(asignaturaId).delete().await()
    }

    override suspend fun guardarHorario(horario: Horario) {
        horario.turno = horario.turno.lowercase().trim()
        
        // Antes de guardar, aseguramos que el profesor esté sincronizado desde la asignatura
        if (horario.asignaturaId.isNotEmpty()) {
            val asigDoc = db.collection("asignaturas").document(horario.asignaturaId).get().await()
            if (asigDoc.exists()) {
                horario.profesorId = asigDoc.getString("profesorId") ?: ""
                horario.profesorNombre = asigDoc.getString("profesorNombre") ?: ""
            }
        }

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

            val cursoId = "${idCentro}_${sc.acronimo}".replace(" ", "_")
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

    override suspend fun recalcularTodosLosContadores() {
        val clasesSnapshot = db.collection("clases").get().await()
        val cursosSnapshot = db.collection("cursos").get().await()
        val asignaturasSnapshot = db.collection("asignaturas").get().await()

        val batch = db.batch()

        // 1. Actualizar cursos sumando el tamaño de los arrays 'estudiantesIds'
        // de todas las clases que pertenecen a ese curso
        cursosSnapshot.documents.forEach { cursoDoc ->
            val cursoId = cursoDoc.id
            var numEstudiantes = 0

            clasesSnapshot.documents.forEach { claseDoc ->
                if (claseDoc.getString("cursoGlobalId") == cursoId) {
                    val listaIds = claseDoc.get("estudiantesIds") as? List<*>
                    numEstudiantes += listaIds?.size ?: 0
                }
            }
            batch.update(cursoDoc.reference, "numEstudiantes", numEstudiantes)
        }

        // 2. Actualizar asignaturas buscando la clase correspondiente
        asignaturasSnapshot.documents.forEach { asigDoc ->
            val cursoId = asigDoc.getString("cursoId") ?: ""
            val turno = asigDoc.getString("turno") ?: ""
            val cicloNum = asigDoc.getLong("cicloNum")?.toInt() ?: 1

            val claseCorrespondiente = clasesSnapshot.documents.find {
                it.getString("cursoGlobalId") == cursoId &&
                        it.getString("turno")?.lowercase()?.trim() == turno.lowercase().trim() &&
                        it.getLong("cicloNum")?.toInt() == cicloNum
            }

            val numEstudiantes = (claseCorrespondiente?.get("estudiantesIds") as? List<*>)?.size ?: 0
            batch.update(asigDoc.reference, "numEstudiantesCurso", numEstudiantes)
        }

        batch.commit().await()
    }

    override suspend fun generarClasesPorDefecto(centroId: String) {
        val batch = db.batch()

        val asignaturasGlobales = db.collection("asignaturas").get().await().toObjects(Asignatura::class.java)

        val cursosGlobales = db.collection("cursos")
            .whereEqualTo("centroId", centroId)
            .get()
            .await()
            .toObjects(Curso::class.java)

        // 1. CAMBIO CLAVE: Obtenemos '.documents' crudos, sin pasarlos por toObjects()
        val estudiantesSnapshots = db.collection("usuarios")
            .whereEqualTo("centroId", centroId)
            .whereEqualTo("rol", "ESTUDIANTE")
            .whereEqualTo("estado", "ACTIVO")
            .get()
            .await()
            .documents

        val asignaturasAgrupadas = asignaturasGlobales.groupBy { asignatura ->
            Triple(asignatura.cursoId, asignatura.turno.uppercase(), asignatura.cicloNum)
        }

        asignaturasAgrupadas.forEach { (claveAgrupacion, listaMaterias) ->
            val (cursoId, turnoMayuscula, cicloNum) = claveAgrupacion

            if (cursoId.isEmpty() || turnoMayuscula.isEmpty()) return@forEach

            val cursoReal = cursosGlobales.find { it.id == cursoId }
            val acronimoReal = cursoReal?.acronimo ?: return@forEach

            val letraTurno = turnoMayuscula.first().uppercaseChar()
            val idClase = "${acronimoReal}${letraTurno}${cicloNum}"

            // 2. CAMBIO CLAVE: Leemos los campos exactos de la base de datos a mano
            val idsEstudiantes = estudiantesSnapshots.filter { doc ->
                // Extraemos los valores ignorando la clase User
                val docCursoId = doc.getString("cursoId") ?: ""
                val docTurno = doc.getString("turno") ?: ""
                val docCicloNum = doc.getLong("cicloNum")?.toInt() ?: -1

                // Filtramos asegurando mayúsculas vs mayúsculas
                docCursoId == cursoId &&
                        docTurno.uppercase() == turnoMayuscula &&
                        docCicloNum == cicloNum
            }.map { it.id } // Sacamos el ID literal del documento

            val claseRef = db.collection("clases").document(idClase)

            val nuevaClase = Clase(
                id = idClase,
                centroId = centroId,
                cursoGlobalId = cursoId,
                cicloNum = cicloNum,
                turno = listaMaterias.first().turno,
                tutorId = null,
                estudiantesIds = idsEstudiantes, // ¡Esta vez estará lleno con los IDs correctos!
                asignaturasIds = listaMaterias.map { it.id }
            )

            batch.set(claseRef, nuevaClase)
        }

        batch.commit().await()
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
