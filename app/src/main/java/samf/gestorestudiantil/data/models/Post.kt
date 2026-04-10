package samf.gestorestudiantil.data.models

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    @DocumentId
    var idFirestore: String = "",
    var titulo: String = "",
    var contenido: String = "",
    var autorId: String = "",
    var autorNombre: String = "",
    var asignaturaId: String = "",
    var unidadId: String = "",
    var fechaCreacion: Long = System.currentTimeMillis(),
    var fechaActualizacion: Long = System.currentTimeMillis(),
    var visible: Boolean = false
)
