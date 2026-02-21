package samf.gestorestudiantil.data.models

data class User(
    var id: String = "",
    var nombre: String = "",
    var email: String = "",
    var rol: String = "", // Ej: "ESTUDIANTE", "PROFESOR", "ADMIN"
    var cursoOArea: String = "", // Ej: "DAMV2" o "Dpto. Informática"
    var centroId: String = ""
)
