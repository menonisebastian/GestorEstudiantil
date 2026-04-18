package samf.gestorestudiantil.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Clase
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.domain.repositories.EstudianteRepository
import javax.inject.Inject

class EstudianteRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : EstudianteRepository {

    // Importante: Importar FieldPath y Filter si usas Firestore
    fun getMiClase(estudianteId: String): Flow<Clase?> {
        return db.collection("clases")
            // Buscamos la clase cuyo array contiene el ID de este estudiante
            .whereArrayContains("estudiantesIds", estudianteId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Clase::class.java).firstOrNull()
            }
    }

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

    override fun observePostsAndTareasChanges(asignaturaIds: List<String>): Flow<Unit> = callbackFlow {
        if (asignaturaIds.isEmpty()) {
            trySend(Unit)
            close()
            return@callbackFlow
        }
        
        val postsSubscription = db.collection("posts")
            .whereIn("asignaturaId", asignaturaIds)
            .addSnapshotListener { _, _ -> trySend(Unit) }
            
        val tareasSubscription = db.collection("tareas")
            .whereIn("asignaturaId", asignaturaIds)
            .addSnapshotListener { _, _ -> trySend(Unit) }

        awaitClose { 
            postsSubscription.remove()
            tareasSubscription.remove()
        }
    }

    override suspend fun getCountNuevosPosts(asignaturaId: String, lastRead: Long): Int {
        val postsSnapshot = db.collection("posts")
            .whereEqualTo("asignaturaId", asignaturaId)
            .whereEqualTo("visible", true)
            .whereGreaterThan("fechaActualizacion", lastRead)
            .get()
            .await()
        return postsSnapshot.size()
    }

    override suspend fun getCountNuevasTareas(asignaturaId: String, lastRead: Long): Int {
        val tareasSnapshot = db.collection("tareas")
            .whereEqualTo("asignaturaId", asignaturaId)
            .whereEqualTo("visible", true)
            .whereGreaterThan("fechaCreacion", com.google.firebase.Timestamp(lastRead / 1000, ((lastRead % 1000) * 1000000).toInt()))
            .get()
            .await()
        return tareasSnapshot.size()
    }

    override suspend fun marcarAsignaturaLeida(usuarioId: String, asignaturaId: String, timestamp: Long) {
        db.collection("usuarios").document(usuarioId)
            .update("ultimaVezAsignaturas.$asignaturaId", timestamp).await()
    }

    override suspend fun getEvaluaciones(asignaturaId: String, estudianteId: String): List<Evaluacion> {
        val snapshot = db.collection("evaluaciones")
            .whereEqualTo("asignaturaId", asignaturaId)
            .whereEqualTo("estudianteId", estudianteId)
            .whereEqualTo("visible", true)
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
