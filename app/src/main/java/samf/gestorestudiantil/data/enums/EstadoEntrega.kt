package samf.gestorestudiantil.data.enums

import androidx.compose.ui.graphics.Color
import samf.gestorestudiantil.data.interfaces.ChipOption

enum class EstadoEntrega(override val label: String, override val color: Color) : ChipOption {
    ENTREGADA("Entregada", Color(0xFF4CAF50)),
    CALIFICADA("Calificada", Color(0xFF2196F3)),
    DEVUELTA("Devuelta", Color(0xFFF44336))
}
