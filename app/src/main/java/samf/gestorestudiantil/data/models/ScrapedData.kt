package samf.gestorestudiantil.data.models

data class ScrapedCourse(
    val _status: String,
    val centro: String,
    val tipo: String,
    val acronimo: String,
    val nombre_curso: String,
    val url: String?,
    val modalidad: String?,
    val turnos_disponibles: List<String>?,
    val horas_totales_curso: String?,
    val ciclos: List<ScrapedCiclo>?,
    val iconoName: String?,
    val colorFondoHex: String?,
    val colorIconoHex: String?
)

data class ScrapedCiclo(
    val ciclo: String,
    val asignaturas: List<ScrapedAsignatura>
)

data class ScrapedAsignatura(
    val acronimo: String,
    val nombre: String,
    val horas_totales: String?,
    val horas_semanales: String?,
    val profesorId: String?,
    val iconoName: String?,
    val colorFondoHex: String?,
    val colorIconoHex: String?
)