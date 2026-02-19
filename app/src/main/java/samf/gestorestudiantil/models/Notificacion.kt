package samf.gestorestudiantil.models

data class Notificacion(
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val hora: String,
    val tipo: tipoNotificacion
)

val listaNotificaciones = listOf<Notificacion>(
    Notificacion("Examen de Programación", "UD4-UD5-UD6", "20/03/2023", "10:00", tipoNotificacion.EXAMEN),
    Notificacion("Entrega Practica Moviles", "Practica de Firebase", "10/02/2026", "16:00", tipoNotificacion.TAREA),
    Notificacion("Reunion TFG", "Reunirme con Juan Manuel", "20/02/2026", "18:00", tipoNotificacion.EVENTO)
)
