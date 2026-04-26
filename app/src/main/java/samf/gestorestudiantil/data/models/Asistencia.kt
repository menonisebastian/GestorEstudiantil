package samf.gestorestudiantil.data.models

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable

@Serializable
data class Asistencia(
    @DocumentId
    var idDocumento: String = "",
    var id: String = "",
    var asignaturaId: String = "",
    var estudianteId: String = "",
    var estudianteNombre: String = "",
    var fecha: Long = 0L, // Medianoche del día en milisegundos
    var estado: AsistenciaEstado = AsistenciaEstado.PRESENTE,
    var comentario: String = ""
)

enum class AsistenciaEstado {
    PRESENTE, AUSENTE, TARDE, JUSTIFICADO
}
