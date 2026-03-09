package samf.gestorestudiantil.data.models

data class ScrapedCourse(
    val _status: String,
    val centro: String,
    val tipo: String,
    val nombre_curso: String,
    val url: String?,
    val modalidad: String?,
    val horas_totales_curso: String?,
    val materias: List<ScrapedMateria>?,
    val turnos_disponibles: List<String>?
)

data class ScrapedMateria(
    val ciclo: String?,
    val materia: String,
    val horas_totales: String?,
    val horas_semanales: String?
)
