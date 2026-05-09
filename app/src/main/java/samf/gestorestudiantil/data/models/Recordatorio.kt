package samf.gestorestudiantil.data.models

import samf.gestorestudiantil.data.enums.TipoRecordatorio

data class Recordatorio(
    var id: String = "",
    var usuarioId: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var fecha: String = "",
    var hora: String = "",
    var tipo: TipoRecordatorio = TipoRecordatorio.EVENTO
)
