package samf.gestorestudiantil.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var id: String = "",
    var nombre: String = "",
    var email: String = "",
    var rol: String = "", // "ESTUDIANTE", "PROFESOR", "ADMIN"
    var cursoId: String = "", // ID del curso (para estudiantes)
    var cursoOArea: String = "", // Nombre visual del curso o departamento
    var centroId: String = "",
    var estado: String = "ACTIVO", // "PENDIENTE" (esperando admin) o "ACTIVO"
    var turno: String = "", // "matutino", "vespertino"
    var cicloNum: Int = 1, // Ciclo en el que está matriculado (1 o 2)
    var imgUrl: String = "",
    var fotoUrl: String = "", // URL de la foto de perfil en Firestore
    var fcmToken: String = "", // Token para notificaciones individuales
    var ultimaVezAsignaturas: Map<String, Long> = emptyMap(), // Map<AsignaturaId, Timestamp>
    var asignaturasImpartidas: List<String> = emptyList() // Lista de asignaturas impartidas (para profesores)
)
