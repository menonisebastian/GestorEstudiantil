package samf.gestorestudiantil.data.repositories

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Clase
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.Post
import samf.gestorestudiantil.data.models.Unidad
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.ProfesorRepository
import javax.inject.Inject

class ProfesorRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ProfesorRepository {

    suspend fun getAlumnosDeMiClase(claseId: String): List<User.Estudiante> {
        // 1. Obtenemos el documento de la clase para sacar los IDs
        val claseDoc = db.collection("clases").document(claseId).get().await()
        val clase = claseDoc.toObject(Clase::class.java) ?: return emptyList()

        val ids = clase.estudiantesIds
        if (ids.isEmpty()) return emptyList()

        // 2. Traemos a los estudiantes exactos usando el operador 'in'
        // Nota: Firestore permite buscar hasta 10 IDs de golpe con 'in'.
        // Si la clase tiene más de 10 alumnos, hay que dividir la lista en bloques (chunks).
        val estudiantes = mutableListOf<User.Estudiante>()

        ids.chunked(10).forEach { bloqueIds ->
            val query = db.collection("usuarios")
                .whereIn(FieldPath.documentId(), bloqueIds)
                .get()
                .await()
            estudiantes.addAll(query.toObjects(User.Estudiante::class.java))
        }

        return estudiantes
    }

    override fun getUnidades(asignaturaId: String): Flow<List<Unidad>> = callbackFlow {
        val subscription = db.collection("unidades")
            .whereEqualTo("asignaturaId", asignaturaId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Unidad::class.java).sortedBy { it.orden })
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getPosts(asignaturaId: String): Flow<List<Post>> = callbackFlow {
        val subscription = db.collection("posts")
            .whereEqualTo("asignaturaId", asignaturaId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Post::class.java).sortedByDescending { it.fechaCreacion })
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun crearUnidad(unidad: Unidad) {
        db.collection("unidades").add(unidad).await()
    }

    override suspend fun editarUnidad(unidadId: String, nombre: String, descripcion: String, visible: Boolean) {
        db.collection("unidades").document(unidadId).update(
            "nombre", nombre,
            "descripcion", descripcion,
            "visible", visible
        ).await()
    }

    override suspend fun eliminarUnidad(unidadId: String) {
        db.collection("unidades").document(unidadId).delete().await()
    }

    override suspend fun crearPost(post: Post) {
        db.collection("posts").add(post).await()
    }

    override suspend fun editarPost(postId: String, titulo: String, contenido: String, visible: Boolean) {
        db.collection("posts").document(postId).update(
            "titulo", titulo,
            "contenido", contenido,
            "visible", visible,
            "fechaActualizacion", System.currentTimeMillis()
        ).await()
    }

    override suspend fun eliminarPost(postId: String) {
        db.collection("posts").document(postId).delete().await()
    }

    override fun getAsignaturas(profesorId: String): Flow<List<Asignatura>> = callbackFlow {
        val subscription = db.collection("asignaturas")
            .whereEqualTo("profesorId", profesorId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Asignatura::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun observeEntregasChanges(asignaturaIds: List<String>): Flow<Unit> = callbackFlow {
        if (asignaturaIds.isEmpty()) {
            trySend(Unit)
            close()
            return@callbackFlow
        }
        val subscription = db.collection("entregas")
            .whereIn("asignaturaId", asignaturaIds)
            .addSnapshotListener { _, _ ->
                trySend(Unit)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getCountNuevasEntregas(asignaturaId: String, lastRead: Long): Int {
        val snapshot = db.collection("entregas")
            .whereEqualTo("asignaturaId", asignaturaId)
            .whereGreaterThan("fechaEntrega", com.google.firebase.Timestamp(lastRead / 1000, ((lastRead % 1000) * 1000000).toInt()))
            .get()
            .await()
        return snapshot.size()
    }

    override fun getEstudiantesPorAsignatura(asignatura: Asignatura): Flow<List<User>> = callbackFlow {
        val subscription = db.collection("usuarios")
            .whereEqualTo("rol", "ESTUDIANTE")
            .whereEqualTo("cursoId", asignatura.cursoId)
            .whereEqualTo("cicloNum", asignatura.cicloNum)
            .whereEqualTo("turno", asignatura.turno)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(User.Estudiante::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getEstudiantesPorCursos(cursoIds: List<String>): Flow<List<User>> = callbackFlow {
        if (cursoIds.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        // Firestore whereIn allows up to 30 elements in newer SDKs, 10 in older ones.
        // Assuming 10 for safety, but if there are many courses we might need more complex logic.
        // For a professor, it's unlikely they have > 10 unique courses.
        val subscription = db.collection("usuarios")
            .whereEqualTo("rol", "ESTUDIANTE")
            .whereIn("cursoId", cursoIds)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(User.Estudiante::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getEstudiantesEspecificos(cursoId: String, cicloNum: Int, turno: String): List<User> {
        val snapshot = db.collection("usuarios")
            .whereEqualTo("rol", "ESTUDIANTE")
            .whereEqualTo("cursoId", cursoId)
            .whereEqualTo("cicloNum", cicloNum)
            .whereEqualTo("turno", turno)
            .get().await()
        return snapshot.toObjects(User.Estudiante::class.java)
    }

    override fun getEvaluacionesEstudiante(estudianteId: String, asignaturaId: String): Flow<List<Evaluacion>> = callbackFlow {
        val subscription = db.collection("evaluaciones")
            .whereEqualTo("estudianteId", estudianteId)
            .whereEqualTo("asignaturaId", asignaturaId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Evaluacion::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun guardarEvaluacion(evaluacion: Evaluacion) {
        val docRef = if (evaluacion.id.isEmpty()) db.collection("evaluaciones").document() else db.collection("evaluaciones").document(evaluacion.id)
        val finalEval = evaluacion.copy(id = docRef.id)
        docRef.set(finalEval).await()
    }

    override suspend fun eliminarEvaluacion(evaluacionId: String) {
        db.collection("evaluaciones").document(evaluacionId).delete().await()
    }

    override fun getHorarios(profesorId: String): Flow<List<Horario>> = callbackFlow {
        val subscription = db.collection("horarios")
            .whereEqualTo("profesorId", profesorId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Horario::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getProfesor(profesorId: String): User? {
        return try {
            val doc = db.collection("usuarios").document(profesorId).get().await()
            doc.toObject(User.Profesor::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getEstudiante(estudianteId: String): User? {
        return try {
            val doc = db.collection("usuarios").document(estudianteId).get().await()
            doc.toObject(User.Estudiante::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
