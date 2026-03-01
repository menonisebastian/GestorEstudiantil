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
    var id: String = "",
    var nombre: String = "",
    var descripcion: String = "",
    var centroId: String = "",
    // Guardamos los estilos como String en Firebase
    var iconoName: String = "Laptop",
    var colorFondoHex: String = "#D0E1FF",
    var colorIconoHex: String = "#4A90E2"
)

// Datos de ejemplo
val listaCursos = listOf(
    Curso("1", "DAM", "", "ies_comercio", "Laptop", "#D0E1FF", "#4A90E2"),
    Curso("2", "DAW", "", "ies_comercio", "Web", "#FFF0D0", "#F5A623"),
    Curso("3", "ASIR", "", "ies_comercio", "Wifi", "#D0F5E1", "#27AE60"),
    Curso("4", "CS", "", "ies_comercio", "Security", "#F5D0FF", "#9B51E0"),
    Curso("5", "Big Data", "", "ies_comercio", "Storage", "#FFFFD0D0", "#E53935"),
    Curso("6", "MRK", "", "ies_comercio", "Storage", "#FFFFD0D0", "#E53935"),
    Curso("7", "A&F", "", "ies_comercio", "Storage", "#FFFFD0D0", "#E53935")
)
