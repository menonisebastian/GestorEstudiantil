package samf.gestorestudiantil.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Curso(
    var id: String = "",
    var acronimo: String = "",
    var nombre: String = "",
    var descripcion: String = "",
    var centroId: String = "",
    var tipo: String = "",
    var modalidad: String = "",
    var turnosDisponibles: List<String> = emptyList(),
    var urlInfo: String = "",
    var horasTotalesCurso: Int = 0,
    var iconoName: String = "School",
    var colorFondoHex: String = "#D0E1FF",
    var colorIconoHex: String = "#2563EB",
    var numEstudiantes: Int = 0
)
