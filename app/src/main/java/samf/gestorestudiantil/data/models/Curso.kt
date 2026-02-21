package samf.gestorestudiantil.data.models

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector


// 1. EL MODELO DE DATOS
data class Curso(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val centroId: String,
    val icono: ImageVector,
    val colorFondo: Color,
    val colorIcono: Color
)

// 2. DATOS DE EJEMPLO (Mock Data)
val listaCursos = listOf(
    Curso(1, "DAM", "", "xxx", Icons.Default.Laptop, Color(0xFFD0E1FF), Color(0xFF4A90E2)), // Azul
    Curso(2, "DAW", "", "xxx", Icons.Default.Web, Color(0xFFFFF0D0), Color(0xFFF5A623)),    // Naranja
    Curso(3, "ASIR", "", "xxx", Icons.Default.Wifi, Color(0xFFD0F5E1), Color(0xFF27AE60)),  // Verde
    Curso(4, "CS", "", "xxx", Icons.Default.Security, Color(0xFFF5D0FF), Color(0xFF9B51E0)), // Violeta
    Curso(5, "Big Data", "", "xxx", Icons.Default.Storage, Color(0xFFFFD0D0), Color(0xFFE53935)) // Rojo
)
