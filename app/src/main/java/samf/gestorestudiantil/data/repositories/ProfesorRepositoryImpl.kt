package samf.gestorestudiantil.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.Asignatura
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

    override fun getEstudiantesPorCursos(cursoIds: List<String>): Flow<List<User>> = callbackFlow {
        if (cursoIds.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val subscription = db.collection("usuarios")
            .whereEqualTo("rol", "ESTUDIANTE")
            .whereIn("cursoId", cursoIds)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(User::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getEstudiantesPorCurso(cursoId: String): List<User> {
        val snapshot = db.collection("usuarios")
            .whereEqualTo("rol", "ESTUDIANTE")
            .whereEqualTo("cursoId", cursoId)
            .get().await()
        return snapshot.toObjects(User::class.java)
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
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
