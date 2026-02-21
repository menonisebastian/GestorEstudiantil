package samf.gestorestudiantil.data.models

data class Horario(
    val id: String,
    val dia: String,
    val horaInicio: String,
    val horaFin: String,
    val materiaId: String,
    val profesorId: String,
    val aula: String
)
