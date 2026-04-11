package samf.gestorestudiantil.data.models

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable

@Serializable
data class Asignatura(
    @DocumentId
    var idDocumento: String = "",
    var id: String = "",                // Campo "id" en el documento Firestore
    var acronimo: String = "",          // "PMM", "PSP", "AD", "DWEC" ...
    var nombre: String = "",            // Nombre completo de la asignatura
    var descripcion: String = "",
    var profesorId: String = "",
    var profesorNombre: String = "",
    var cursoId: String = "",
    var centroId: String = "",
    var ciclo: String = "",             // "1", "2", "único", "2 (DAW)", "2 (DAM)"
    var cicloNum: Int = 1,              // Int para ordenar/filtrar: 1 o 2
    var turno: String = "",             // "matutino", "vespertino"
    var horasTotales: Int = 0,
    var horasSemanales: Int = 0,
    // Estilos visuales almacenados como String en Firestore
    var iconoName: String = "Class",
    var colorFondoHex: String = "#E8E8E8",
    var colorIconoHex: String = "#6B7280",
    var numEstudiantesCurso: Int = 0,
    var numNotificaciones: Int = 0
)