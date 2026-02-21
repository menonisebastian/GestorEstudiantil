package samf.gestorestudiantil.data.models

import samf.gestorestudiantil.data.enums.tipoEvaluacion

data class Evaluacion(
    val id: Int,
    val nombre: String,
    val nota: Double,
    val estudianteId: String,
    val asignatura: String,
    val tipoEvaluacion: tipoEvaluacion
)
