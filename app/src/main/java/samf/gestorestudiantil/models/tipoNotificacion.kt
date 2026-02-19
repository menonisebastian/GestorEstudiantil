package samf.gestorestudiantil.models

import androidx.compose.ui.graphics.Color

enum class tipoNotificacion(val label: String, val color: Color)
{
    EXAMEN("Examen", Color(0xFFE53935)),   // Rojo
    TAREA("Tarea", Color(0xFF0048FF)), // Naranja
    EVENTO("Evento", Color(0xFF673AB7))
}