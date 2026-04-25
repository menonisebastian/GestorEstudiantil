package samf.gestorestudiantil.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.domain.repositories.RecordatorioRepository
import javax.inject.Inject

class RecordatorioRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : RecordatorioRepository {

    override fun getRecordatorios(usuarioId: String): Flow<List<Recordatorio>> = callbackFlow {
        val subscription = db.collection("recordatorios")
            .whereEqualTo("usuarioId", usuarioId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Recordatorio::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun guardarRecordatorio(recordatorio: Recordatorio) {
        val finalRecordatorio = if (recordatorio.id.isEmpty()) {
            recordatorio.copy(id = db.collection("recordatorios").document().id)
        } else {
            recordatorio
        }

        db.collection("recordatorios")
            .document(finalRecordatorio.id)
            .set(finalRecordatorio)
            .await()
    }

    override suspend fun eliminarRecordatorio(recordatorioId: String) {
        db.collection("recordatorios")
            .document(recordatorioId)
            .delete()
            .await()
    }

    override suspend fun actualizarRecordatorio(recordatorio: Recordatorio) {
        db.collection("recordatorios")
            .document(recordatorio.id)
            .set(recordatorio)
            .await()
    }
}
