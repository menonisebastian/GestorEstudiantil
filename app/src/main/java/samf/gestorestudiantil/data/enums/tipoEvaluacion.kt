package samf.gestorestudiantil.data.enums

import androidx.compose.ui.graphics.Color
import samf.gestorestudiantil.data.interfaces.ChipOption

enum class tipoEvaluacion(override val label: String, override val color: Color) : ChipOption
{
    Examen("Examen", Color(0xFFE53935)),   // Rojo
    Practica("Práctica", Color(0xFFFFC107)), // Naranja
    Exposicion("Exposición", Color(0xFF4CAF50)),
    Proyecto("Proyecto", Color(0xFF673AB7))
}