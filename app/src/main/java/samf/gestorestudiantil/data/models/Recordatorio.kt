package samf.gestorestudiantil.data.models

import samf.gestorestudiantil.data.enums.tipoRecordatorio

data class Recordatorio(
    var id: String = "",
    var usuarioId: String = "", // Para saber a quién pertenece
    var titulo: String = "",
    var descripcion: String = "",
    var fecha: String = "",
    var hora: String = "",
    var tipo: tipoRecordatorio = tipoRecordatorio.EVENTO
)

// Datos de ejemplo
val listaRecordatorios = listOf(
    Recordatorio("rec_1", "user_1", "Examen de Programación", "UD4-UD5-UD6", "20/03/2023", "10:00", tipoRecordatorio.EXAMEN),
    Recordatorio("rec_2", "user_1", "Entrega Practica Moviles", "Practica de Firebase", "10/02/2026", "16:00", tipoRecordatorio.TAREA),
    Recordatorio("rec_3", "user_1", "Reunion TFG", "Reunirme con Juan Manuel", "20/02/2026", "18:00", tipoRecordatorio.EVENTO)
)
