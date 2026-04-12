package samf.gestorestudiantil.data.repositories

import io.github.jan.supabase.storage.Storage
import io.ktor.http.ContentType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.AdjuntoInfo
import samf.gestorestudiantil.data.models.Entrega
import samf.gestorestudiantil.data.models.Tarea
import samf.gestorestudiantil.domain.repositories.TareaRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

class TareaRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: Storage
) : TareaRepository {

    override fun getTareasPorUnidad(unidadId: String): Flow<List<Tarea>> = callbackFlow {
        val subscription = db.collection("tareas")
            .whereEqualTo("unidadId", unidadId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Tarea::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getTareasPorAsignatura(asignaturaId: String): Flow<List<Tarea>> = callbackFlow {
        val subscription = db.collection("tareas")
            .whereEqualTo("asignaturaId", asignaturaId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Tarea::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun crearTarea(tarea: Tarea, fileData: ByteArray?, fileName: String?, mimeType: String?): String {
        val tareaId = "tarea_${tarea.asignaturaId}_${System.currentTimeMillis()}"
        var adjunto: AdjuntoInfo? = null

        if (fileData != null && fileName != null) {
            val path = "tareas/$tareaId/$fileName"
            val bucket = storage.from("gestor-estudiantil")
            bucket.upload(path, fileData) {
                upsert = true
                try {
                    mimeType?.let { contentType = ContentType.parse(it) }
                } catch (e: Exception) {
                    // Si falla el parseo, dejamos que Supabase decida o asignamos uno genérico
                    contentType = ContentType.Application.OctetStream
                }
            }
            
            val url = bucket.createSignedUrl(path, expiresIn = 24.hours)
            
            adjunto = AdjuntoInfo(
                supabasePath = path,
                nombreArchivo = fileName,
                pesoBytes = fileData.size.toLong(),
                formato = fileName.substringAfterLast(".", ""),
                urlDescarga = url
            )
        }

        val finalTarea = tarea.copy(id = tareaId, adjunto = adjunto)
        db.collection("tareas").document(tareaId).set(finalTarea).await()
        return tareaId
    }

    override suspend fun editarTarea(tarea: Tarea, fileData: ByteArray?, fileName: String?, mimeType: String?): String {
        var adjunto = tarea.adjunto

        if (fileData != null && fileName != null) {
            // Borrar anterior si existe y es distinto path? 
            // Para simplificar, subimos el nuevo.
            val path = "tareas/${tarea.id}/$fileName"
            val bucket = storage.from("gestor-estudiantil")
            bucket.upload(path, fileData) { 
                upsert = true
                try {
                    mimeType?.let { contentType = ContentType.parse(it) }
                } catch (e: Exception) {
                    contentType = ContentType.Application.OctetStream
                }
            }
            
            val url = bucket.createSignedUrl(path, expiresIn = 24.hours)
            
            adjunto = AdjuntoInfo(
                supabasePath = path,
                nombreArchivo = fileName,
                pesoBytes = fileData.size.toLong(),
                formato = fileName.substringAfterLast(".", ""),
                urlDescarga = url
            )
        }

        val finalTarea = tarea.copy(adjunto = adjunto)
        db.collection("tareas").document(tarea.id).set(finalTarea).await()
        return tarea.id
    }

    override suspend fun eliminarTarea(tarea: Tarea) {
        // Eliminar adjunto de Supabase
        tarea.adjunto?.let {
            try {
                storage.from("gestor-estudiantil").delete(it.supabasePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Eliminar de Firestore
        db.collection("tareas").document(tarea.id).delete().await()
    }

    override fun getEntregasPorTarea(tareaId: String): Flow<List<Entrega>> = callbackFlow {
        val subscription = db.collection("entregas")
            .whereEqualTo("tareaId", tareaId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Entrega::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getEntregaEstudiante(tareaId: String, estudianteId: String): Flow<Entrega?> = callbackFlow {
        val subscription = db.collection("entregas")
            .document("entrega_${tareaId}_${estudianteId}")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(Entrega::class.java))
                } else {
                    trySend(null)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun realizarEntrega(entrega: Entrega, fileData: ByteArray, fileName: String, mimeType: String?) {
        val entregaId = "entrega_${entrega.tareaId}_${entrega.estudianteId}"
        val path = "entregas/${entrega.tareaId}/${entrega.estudianteId}/$fileName"
        
        val bucket = storage.from("gestor-estudiantil")
        bucket.upload(path, fileData) {
            upsert = true
            try {
                mimeType?.let { contentType = ContentType.parse(it) }
            } catch (e: Exception) {
                // Si falla el parseo, dejamos que Supabase decida o asignamos uno genérico
                contentType = ContentType.Application.OctetStream
            }
        }
        
        val url = bucket.createSignedUrl(path, expiresIn = 24.hours)
        
        val adjunto = AdjuntoInfo(
            supabasePath = path,
            nombreArchivo = fileName,
            pesoBytes = fileData.size.toLong(),
            formato = fileName.substringAfterLast(".", ""),
            urlDescarga = url
        )

        val finalEntrega = entrega.copy(id = entregaId, adjunto = adjunto)
        db.collection("entregas").document(entregaId).set(finalEntrega).await()
    }

    override suspend fun eliminarEntrega(entrega: Entrega) {
        try {
            storage.from("gestor-estudiantil").delete(entrega.adjunto.supabasePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        db.collection("entregas").document(entrega.id).delete().await()
    }

    override suspend fun getUrlFirmada(supabasePath: String): String {
        return storage.from("gestor-estudiantil").createSignedUrl(supabasePath, expiresIn = 2.hours)
    }

    override suspend fun descargarArchivo(path: String): ByteArray {
        return storage.from("gestor-estudiantil").downloadPublic(path)
    }
}
