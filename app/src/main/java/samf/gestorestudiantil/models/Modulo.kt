package samf.gestorestudiantil.models

data class Modulo(
    val id: Int,
    val nombre: String,
    val nota: Double,
    val materia: String,
    val tipoEvaluacion: tipoEvaluacion
)
