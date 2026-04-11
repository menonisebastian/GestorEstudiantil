package samf.gestorestudiantil.data.models

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable

@Serializable
data class Unidad(
    @DocumentId
    var id: String = "",
    var nombre: String = "",
    var descripcion: String = "",
    var asignaturaId: String = "",
    var orden: Int = 0,
    var visible: Boolean = false
)
