package samf.gestorestudiantil.data.models

import samf.gestorestudiantil.data.enums.tipoRecordatorio

data class Recordatorio(
    var id: String = "",
    var usuarioId: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var fecha: String = "",
    var hora: String = "",
    var tipo: tipoRecordatorio = tipoRecordatorio.EVENTO
)
