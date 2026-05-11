package samf.gestorestudiantil.data.models

import samf.gestorestudiantil.data.enums.TipoEvaluacion as TipoEvaluacionEnum

data class Evaluacion(
    var id: String = "",
    var nombre: String = "",
    var nota: Double = 0.0,
    var estudianteId: String = "",
    var asignaturaId: String = "",
    var visible: Boolean = true,
    var comentario: String? = null,
    var adjunto: AdjuntoInfo? = null,
    var tipoEvaluacion: TipoEvaluacionEnum = TipoEvaluacionEnum.Examen,
    var modulosEvaluados: List<String> = emptyList()
)
