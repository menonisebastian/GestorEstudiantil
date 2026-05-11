package samf.gestorestudiantil.data.models

import kotlinx.serialization.Serializable
import samf.gestorestudiantil.domain.utils.toTurnoLetra

@Serializable
data class Horario(
    var id: String = "",
    var cursoId: String = "",
    var claseId: String = "",
    var cicloNum: Int = 1,
    var turno: String = "",
    var dia: String = "",
    var horaInicio: String = "",
    var horaFin: String = "",
    var asignaturaId: String = "",
    var asignaturaAcronimo: String = "",
    var profesorId: String = "",
    var profesorNombre: String = "",
    var aula: String = ""
) {
    val cursoAcronimo: String
        get() = cursoId.substringAfterLast("_").uppercase()

    val turnoLetra: String
        get() = turno.toTurnoLetra()

    companion object {
        val HORAS_MATUTINO = listOf(
            "08:30 - 09:20",
            "09:25 - 10:15",
            "10:20 - 11:10",
            "11:10 - 11:35", // RECREO
            "11:35 - 12:25",
            "12:30 - 13:20",
            "13:25 - 14:15"
        )

        val HORAS_VESPERTINO = listOf(
            "16:00 - 16:50",
            "16:55 - 17:45",
            "17:50 - 18:40",
            "18:40 - 19:05", // RECREO
            "19:05 - 19:55",
            "20:00 - 20:50",
            "20:55 - 21:45"
        )
        
        val DIAS_SEMANA = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
    }
}
