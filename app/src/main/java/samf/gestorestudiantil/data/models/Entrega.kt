package samf.gestorestudiantil.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable
import samf.gestorestudiantil.data.enums.EstadoEntrega
import samf.gestorestudiantil.domain.serializers.TimestampSerializer

@Serializable
data class Entrega(
    @DocumentId
    var id: String = "",
    var tareaId: String = "",
    var estudianteId: String = "",
    var estudianteNombre: String = "",
    var estudianteImgUrl: String? = null,
    var profesorId: String = "",
    var asignaturaId: String = "",
    @Serializable(with = TimestampSerializer::class)
    var fechaEntrega: Timestamp = Timestamp.now(),
    var estado: EstadoEntrega = EstadoEntrega.ENTREGADA,
    var calificacion: Float? = null,
    var comentarioProfesor: String? = null,
    var adjunto: AdjuntoInfo = AdjuntoInfo()
)
