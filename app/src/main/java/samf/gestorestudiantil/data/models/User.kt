package samf.gestorestudiantil.data.models

data class User(
    var id: String = "",
    var nombre: String = "",
    var email: String = "",
    var rol: String = "", // "ESTUDIANTE", "PROFESOR", "ADMIN"
    var cursoId: String = "", // ID del curso (para estudiantes)
    var cursoOArea: String = "", // Nombre visual del curso o departamento
    var centroId: String = "",
    var estado: String = "ACTIVO", // "PENDIENTE" (esperando admin) o "ACTIVO"
    var imgUrl: String = ""

)
