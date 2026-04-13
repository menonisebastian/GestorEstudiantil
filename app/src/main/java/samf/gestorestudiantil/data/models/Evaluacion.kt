package samf.gestorestudiantil.data.models

import samf.gestorestudiantil.data.enums.tipoEvaluacion as TipoEvaluacionEnum

data class Evaluacion(
    var id: String = "",
    var nombre: String = "",
    var nota: Double = 0.0,
    var estudianteId: String = "",
    var asignaturaId: String = "",
    var visible: Boolean = true,
    var comentario: String? = null,
    var adjunto: AdjuntoInfo? = null,
    // Usamos el alias para declarar el tipo y acceder a sus valores
    var tipoEvaluacion: TipoEvaluacionEnum = TipoEvaluacionEnum.Examen,
    var modulosEvaluados: List<String> = emptyList()
)
