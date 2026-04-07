package samf.gestorestudiantil.data.models

data class ScrapedCourse(
    val _status: String,
    val centro: String,
    val tipo: String,
    val acronimo: String,                   // nuevo: "DAM", "MKT", "ASIR"...
    val nombre_curso: String,
    val url: String?,
    val modalidad: String?,
    val turnos_disponibles: List<String>?,  // movido arriba para consistencia
    val horas_totales_curso: String?,
    val ciclos: List<ScrapedCiclo>?,        // reemplaza materias: estructura anidada
    // Estilos del curso (vienen ya calculados en el JSONL)
    val iconoName: String?,
    val colorFondoHex: String?,
    val colorIconoHex: String?
)

data class ScrapedCiclo(
    val ciclo: String,                      // "1", "2", "único", "2 (DAW)", "2 (DAM)"
    val asignaturas: List<ScrapedAsignatura>
)

data class ScrapedAsignatura(
    val acronimo: String,
    val nombre: String,
    val horas_totales: String?,
    val horas_semanales: String?,
    val profesorId: String?,
    // Estilos de la asignatura (vienen ya calculados en el JSONL)
    val iconoName: String?,
    val colorFondoHex: String?,
    val colorIconoHex: String?
)