package samf.gestorestudiantil.data.models

// 1. EL MODELO DE DATOS
data class Curso(
    var id: String = "",
    var nombre: String = "",
    var descripcion: String = "",
    var centroId: String = "",
    var tipo: String = "", // FPGS, FPGM, etc.
    var modalidad: String = "", // Presencial, Dual, etc.
    var ciclo: String = "", // 1º, 2º, etc.
    var urlInfo: String = "",
    var turnos: List<String> = emptyList(), // "matutino", "vespertino"
    // Guardamos los estilos como String en Firebase
    var iconoName: String = "Laptop",
    var colorFondoHex: String = "#D0E1FF",
    var colorIconoHex: String = "#4A90E2"
)

// Datos de ejemplo
val listaCursos = listOf(
    Curso("1", "DAM", "", "ies_comercio", "FPGS", "Presencial", "1º", "", listOf("matutino"), "Laptop", "#D0E1FF", "#4A90E2"),
    Curso("2", "DAW", "", "ies_comercio", "FPGS", "Presencial", "2º", "", listOf("vespertino"), "Web", "#FFF0D0", "#F5A623"),
    Curso("3", "ASIR", "", "ies_comercio", "FPGS", "Presencial", "1º", "", listOf("matutino"), "Wifi", "#D0F5E1", "#27AE60"),
    Curso("4", "CS", "", "ies_comercio", "Especialización", "Presencial", "Unico", "", listOf("vespertino"), "Security", "#F5D0FF", "#9B51E0"),
    Curso("5", "Big Data", "", "ies_comercio", "Especialización", "Presencial", "Unico", "", listOf("vespertino"), "Storage", "#FFFFD0D0", "#E53935"),
    Curso("6", "MRK", "", "ies_comercio", "FPGM", "Presencial", "1º", "", listOf("matutino"), "Storage", "#FFFFD0D0", "#E53935"),
    Curso("7", "A&F", "", "ies_comercio", "FPGS", "Presencial", "2º", "", listOf("matutino"), "Storage", "#FFFFD0D0", "#E53935")
)
