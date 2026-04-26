package samf.gestorestudiantil.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.Asistencia
import samf.gestorestudiantil.domain.repositories.AsistenciaRepository
import java.util.UUID
import javax.inject.Inject

class AsistenciaRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AsistenciaRepository {

    override fun getAsistenciasPorDia(asignaturaId: String, fecha: Long): Flow<List<Asistencia>> = callbackFlow {
        val subscription = firestore.collection("asistencias")
            .whereEqualTo("asignaturaId", asignaturaId)
            .whereEqualTo("fecha", fecha)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshot?.documents?.mapNotNull { it.toObject(Asistencia::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    override fun getAsistenciasPorEstudiante(estudianteId: String): Flow<List<Asistencia>> = callbackFlow {
        val subscription = firestore.collection("asistencias")
            .whereEqualTo("estudianteId", estudianteId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshot?.documents?.mapNotNull { it.toObject(Asistencia::class.java) } ?: emptyList()
                trySend(list.sortedByDescending { it.fecha })
            }
        awaitClose { subscription.remove() }
    }

    override fun getDiasConAsistencia(asignaturaId: String): Flow<List<Long>> = callbackFlow {
        val subscription = firestore.collection("asistencias")
            .whereEqualTo("asignaturaId", asignaturaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshot?.documents?.mapNotNull { it.getLong("fecha") }?.distinct()?.sortedDescending() ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun guardarAsistencias(asistencias: List<Asistencia>) {
        val batch = firestore.batch()
        asistencias.forEach { asistencia ->
            val docRef = if (asistencia.id.isEmpty()) {
                val newId = UUID.randomUUID().toString()
                asistencia.id = newId
                firestore.collection("asistencias").document(newId)
            } else {
                firestore.collection("asistencias").document(asistencia.id)
            }
            batch.set(docRef, asistencia)
        }
        batch.commit().await()
    }

    override suspend fun eliminarAsistencia(asistenciaId: String) {
        firestore.collection("asistencias").document(asistenciaId).delete().await()
    }
}
