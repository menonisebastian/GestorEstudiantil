package samf.gestorestudiantil.data.models

data class Curso(
    var id: String = "",
    var acronimo: String = "",          // "DAM", "DAW", "ASIR", "MKT" ...
    var nombre: String = "",            // Nombre completo del curso
    var descripcion: String = "",
    var centroId: String = "",
    var tipo: String = "",              // "FP Grado Superior", "FP Grado Medio", etc.
    var modalidad: String = "",         // "presencial", "dual"
    var turnosDisponibles: List<String> = emptyList(), // ["matutino"], ["vespertino"], ["matutino","vespertino"]
    var urlInfo: String = "",
    var horasTotalesCurso: Int = 0,
    // Estilos visuales almacenados como String en Firestore
    var iconoName: String = "School",
    var colorFondoHex: String = "#D0E1FF",
    var colorIconoHex: String = "#2563EB",
    var numEstudiantes: Int = 0
)
