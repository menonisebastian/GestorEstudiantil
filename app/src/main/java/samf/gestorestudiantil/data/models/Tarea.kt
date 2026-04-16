package samf.gestorestudiantil.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable
import samf.gestorestudiantil.domain.serializers.TimestampSerializer

@Serializable
data class Tarea(
    @DocumentId
    var id: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var claseId: String = "",
    var profesorId: String = "",
    var asignaturaId: String = "",
    var centroId: String = "",
    var unidadId: String = "",
    @Serializable(with = TimestampSerializer::class)
    var fechaCreacion: Timestamp = Timestamp.now(),
    @Serializable(with = TimestampSerializer::class)
    var fechaLimiteEntrega: Timestamp = Timestamp.now(),
    var adjunto: AdjuntoInfo? = null,
    var visible: Boolean = true
)

@Serializable
data class AdjuntoInfo(
    var supabasePath: String = "",
    var nombreArchivo: String = "",
    var pesoBytes: Long = 0,
    var formato: String = "",
    var urlDescarga: String = ""
)
