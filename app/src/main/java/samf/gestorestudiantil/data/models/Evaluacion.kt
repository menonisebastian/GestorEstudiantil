package samf.gestorestudiantil.data.models

import samf.gestorestudiantil.data.enums.tipoEvaluacion as TipoEvaluacionEnum

data class Evaluacion(
    var id: String = "",
    var nombre: String = "",
    var nota: Double = 0.0,
    var estudianteId: String = "",
    var asignaturaId: String = "",
    var visible: Boolean = true,
    // Usamos el alias para declarar el tipo y acceder a sus valores
    var tipoEvaluacion: TipoEvaluacionEnum = TipoEvaluacionEnum.Examen,
    var modulosEvaluados: List<String> = emptyList()
)

// Datos de ejemplo demostrando el uso de los distintos tipos del enum
val listaEvaluaciones = listOf(
    Evaluacion("1", "UD1", 8.0, "estudiante_1", "asignatura_1", true, TipoEvaluacionEnum.Examen, listOf("M1", "M2")),
    Evaluacion("2", "UD2", 7.5, "estudiante_1", "asignatura_1", true, TipoEvaluacionEnum.Practica, listOf("M3")),
    Evaluacion("3", "UD3", 9.0, "estudiante_1", "asignatura_1", true, TipoEvaluacionEnum.Exposicion, listOf("M4")),
    Evaluacion("4", "TFG", 10.0, "estudiante_1", "asignatura_1", true, TipoEvaluacionEnum.Proyecto, listOf("Todos los módulos"))
)
