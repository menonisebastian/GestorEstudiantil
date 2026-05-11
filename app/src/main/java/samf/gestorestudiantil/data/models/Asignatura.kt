package samf.gestorestudiantil.data.models

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable
import samf.gestorestudiantil.domain.utils.toTurnoLetra

@Serializable
data class Asignatura(
    @DocumentId
    var idDocumento: String = "",
    var id: String = "",
    var acronimo: String = "",
    var nombre: String = "",
    var departamento: String = "",
    var descripcion: String = "",
    var profesorId: String = "",
    var profesorNombre: String = "",
    var cursoId: String = "",
    var centroId: String = "",
    var ciclo: String = "",
    var cicloNum: Int = 1,
    var turno: String = "",
    var horasTotales: Int = 0,
    var horasSemanales: Int = 0,
    var iconoName: String = "Class",
    var colorFondoHex: String = "#E8E8E8",
    var colorIconoHex: String = "#6B7280",
    var numEstudiantesCurso: Int = 0,
    var numNotificaciones: Int = 0
) {
    val turnoLetra: String
        get() = turno.toTurnoLetra()

    val cursoAcronimo: String
        get() = cursoId.substringAfterLast("_").uppercase()

    val codigoFormateado: String
        get() = "$acronimo $cursoAcronimo$turnoLetra$cicloNum"
}
