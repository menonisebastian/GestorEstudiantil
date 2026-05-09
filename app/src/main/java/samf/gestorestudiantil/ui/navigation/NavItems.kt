package samf.gestorestudiantil.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Grading
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

val itemsEstudiante: Map<String, ImageVector> = mapOf(
    "Asignaturas" to Icons.Outlined.Class,
    "Horarios" to Icons.Default.Schedule,
    "Calendario" to Icons.Default.CalendarMonth,
    "Perfil" to Icons.Outlined.Person
)

val itemsProfesor: Map<String, ImageVector> = mapOf(
    "Asignaturas" to Icons.Outlined.Class,
    "Horarios" to Icons.Default.Schedule,
    "Calendario" to Icons.Default.CalendarMonth,
    "Calificaciones" to Icons.AutoMirrored.Filled.Grading,
    "Perfil" to Icons.Outlined.Person
)

val itemsAdmin: Map<String, ImageVector> = mapOf(
    "Usuarios" to Icons.Outlined.Person,
    "Centros" to Icons.Default.Business,
    "Calendario" to Icons.Default.CalendarMonth,
    "Perfil" to Icons.Outlined.Person,
)