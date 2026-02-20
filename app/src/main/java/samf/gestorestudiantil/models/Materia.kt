package samf.gestorestudiantil.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Materia(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val horas: String,
    val profesor: String,
    val icono: ImageVector,
    val colorFondo: Color,
    val colorIcono: Color
)

val listaMaterias = listOf(
    Materia(1, "PMM","Programación de Multimedia y Dispositivos Móviles", "5 horas", listaProfesores[0].nombre+" "+listaProfesores[0].apellidos, Icons.Default.PhoneIphone, Color(0xFFD0E1FF), Color(0xFF4A90E2)), // Azul
    Materia(2, "PSP","Programación de Servicios y Procesos", "5 horas", listaProfesores[0].nombre+" "+listaProfesores[0].apellidos, Icons.Default.Build, Color(0xFFFFF0D0), Color(0xFFF5A623)),
    Materia(3, "AD","Acceso a Datos", "6 horas", listaProfesores[1].nombre+" "+listaProfesores[1].apellidos, Icons.Default.DataObject, Color(0xFFD0F5E1), Color(0xFF27AE60)),
    Materia(4, "DCKR","Docker", "2 horas", listaProfesores[2].nombre+" "+listaProfesores[2].apellidos, Icons.Default.AllInbox, Color(0xFFF5D0FF), Color(0xFF9B51E0)),
    Materia(5, "SGE","Sistemas de Gestión Empresarial", "5 horas", listaProfesores[3].nombre+" "+listaProfesores[3].apellidos, Icons.Default.BusinessCenter, Color(0xFFFFD0D0), Color(0xFFE53935)),
    Materia(6, "DdI","Desarrollo de Interfaces", "6 horas", listaProfesores[3].nombre+" "+listaProfesores[3].apellidos, Icons.Default.BusinessCenter, Color(0xFFFFD0D0), Color(0xFFE53935)),
    Materia(7, "FOL","Formación y Orientación Laboral", "2 horas", listaProfesores[4].nombre+" "+listaProfesores[4].apellidos, Icons.Default.BusinessCenter, Color(0xFFFFD0D0), Color(0xFFE53935)),
)
