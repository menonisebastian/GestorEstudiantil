package samf.gestorestudiantil.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.domain.repositories.EstudianteRepository
import javax.inject.Inject

class EstudianteRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : EstudianteRepository {

    override fun getAsignaturas(cursoId: String, turno: String, cicloNum: Int): Flow<List<Asignatura>> = callbackFlow {
        val subscription = db.collection("asignaturas")
            .whereEqualTo("cursoId", cursoId)
            .whereEqualTo("turno", turno.lowercase().trim())
            .whereEqualTo("cicloNum", cicloNum)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Asignatura::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun observePostsChanges(asignaturaIds: List<String>): Flow<Unit> = callbackFlow {
        if (asignaturaIds.isEmpty()) {
            trySend(Unit)
            close()
            return@callbackFlow
        }
        val subscription = db.collection("posts")
            .whereIn("asignaturaId", asignaturaIds)
            .addSnapshotListener { _, _ ->
                trySend(Unit)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getCountNuevosPosts(asignaturaId: String, lastRead: Long): Int {
        val postsSnapshot = db.collection("posts")
            .whereEqualTo("asignaturaId", asignaturaId)
            .whereGreaterThan("fechaCreacion", lastRead)
            .get()
            .await()
        return postsSnapshot.size()
    }

    override suspend fun marcarAsignaturaLeida(usuarioId: String, asignaturaId: String, timestamp: Long) {
        db.collection("usuarios").document(usuarioId)
            .update("ultimaVezAsignaturas.$asignaturaId", timestamp).await()
    }

    override suspend fun getEvaluaciones(asignaturaId: String): List<Evaluacion> {
        val snapshot = db.collection("evaluaciones")
            .whereEqualTo("asignaturaId", asignaturaId)
            .get().await()
        return snapshot.toObjects(Evaluacion::class.java)
    }

    override fun getHorarios(cursoId: String, turno: String, cicloNum: Int): Flow<List<Horario>> = callbackFlow {
        val subscription = db.collection("horarios")
            .whereEqualTo("cursoId", cursoId)
            .whereEqualTo("turno", turno.lowercase().trim())
            .whereEqualTo("cicloNum", cicloNum)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Horario::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }
}
