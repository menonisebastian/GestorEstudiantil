package samf.gestorestudiantil.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Clase(
    val id: String = "",
    val centroId: String = "",
    val cursoGlobalId: String = "",
    val cicloNum: Int = 1,
    val turno: String = "",
    val tutorId: String? = null,
    val estudiantesIds: List<String> = emptyList(),
    val asignaturasIds: List<String> = emptyList(),
)
