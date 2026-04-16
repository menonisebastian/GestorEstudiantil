package samf.gestorestudiantil.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Clase(
    val id: String = "",
    val centroId: String = "",
    val cursoGlobalId: String = "", // Referencia a tu Curso global
    val cicloNum: Int = 1,
    val turno: String = "",         // Ej: "matutino", "vespertino"
    val tutorId: String? = null,     // Referencia al ID del Profesor asignado

    // Relación con Estudiantes específicos de este grupo
    val estudiantesIds: List<String> = emptyList(),

    // Relación con Asignaturas específicas de este grupo
    val asignaturasIds: List<String> = emptyList(),
)
