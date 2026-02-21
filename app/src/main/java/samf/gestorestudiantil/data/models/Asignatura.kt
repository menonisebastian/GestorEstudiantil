package samf.gestorestudiantil.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Asignatura(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val horas: String,
    val profesor: String,
    val centroId: String,
    val icono: ImageVector,
    val colorFondo: Color,
    val colorIcono: Color
)

val listaAsignaturas = listOf(
    Asignatura(1, "PMM","Programación de Multimedia y Dispositivos Móviles", "5 horas", listaProfesores[0].nombre, "xxx", Icons.Default.PhoneIphone, Color(0xFFD0E1FF), Color(0xFF4A90E2)), // Azul
    Asignatura(2, "PSP","Programación de Servicios y Procesos", "5 horas", listaProfesores[0].nombre, "xxx", Icons.Default.Build, Color(0xFFFFF0D0), Color(0xFFF5A623)),
    Asignatura(3, "AD","Acceso a Datos", "6 horas", listaProfesores[1].nombre, "xxx", Icons.Default.DataObject, Color(0xFFD0F5E1), Color(0xFF27AE60)),
    Asignatura(4, "DCKR","Docker", "2 horas", listaProfesores[2].nombre, "xxx", Icons.Default.AllInbox, Color(0xFFF5D0FF), Color(0xFF9B51E0)),
    Asignatura(5, "SGE","Sistemas de Gestión Empresarial", "5 horas", listaProfesores[3].nombre, "xxx", Icons.Default.BusinessCenter, Color(0xFFFFD0D0), Color(0xFFE53935)),
    Asignatura(6, "DDI","Desarrollo de Interfaces", "6 horas", listaProfesores[4].nombre, "xxx", Icons.Default.Devices, Color(0xFFFDDDD6), Color(0xFFFF5722)),
    Asignatura(7, "FOL","Formación y Orientación Laboral", "2 horas", listaProfesores[5].nombre, "xxx", Icons.Default.Engineering, Color(0xFFFDE0E9), Color(0xFFE91E63)),
    Asignatura(8, "SOS","Sostenibilidad", "1 horas", listaProfesores[6].nombre, "xxx", Icons.Default.Nature, Color(0xFFCBFAF5), Color(0xFF009688)),
)
