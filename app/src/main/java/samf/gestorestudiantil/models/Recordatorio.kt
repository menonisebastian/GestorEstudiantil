package samf.gestorestudiantil.models

data class Recordatorio(
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val hora: String,
    val tipo: tipoRecordatorio
)

val listaRecordatorios = listOf<Recordatorio>(
    Recordatorio("Examen de Programación", "UD4-UD5-UD6", "20/03/2023", "10:00", tipoRecordatorio.EXAMEN),
    Recordatorio("Entrega Practica Moviles", "Practica de Firebase", "10/02/2026", "16:00", tipoRecordatorio.TAREA),
    Recordatorio("Reunion TFG", "Reunirme con Juan Manuel", "20/02/2026", "18:00", tipoRecordatorio.EVENTO)
)
