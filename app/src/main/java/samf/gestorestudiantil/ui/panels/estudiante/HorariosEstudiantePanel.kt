package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.ui.components.WeekNavBar
import samf.gestorestudiantil.ui.theme.textColor

// Definimos los colores aproximados de la imagen
val ColorDesarrollo = Color(0xFFC5E1A5) // Verde claro
val ColorSostenibilidad = Color(0xFFE1BEE7) // Lila
val ColorAccesoDatos = Color(0xFFFFF59D) // Amarillo
val ColorSistemas = Color(0xFFFFCC80) // Naranja
val ColorProgMovil = Color(0xFFEF9A9A) // Rojo suave
val ColorDocker = Color(0xFFD1C4E9) // Morado suave
val ColorFOL = Color(0xFF80DEEA) // Cian/Turquesa
val ColorServicios = Color(0xFF90CAF9) // Azul claro
val ColorReceso = Color.LightGray

data class ClaseHorario(
    val materia: String,
    val profesor: String,
    val hora: String,
    val color: Color
)

@Composable
fun HorariosEstudiantePanel(paddingValues: PaddingValues) {

    var selectedDay by remember { mutableStateOf("Lunes") }

    val curso = "DAMV2"

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        // Título
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Horario Personal - $curso",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de selección de días
        WeekNavBar(
            selectedItem = selectedDay,
            onItemSelected = { nuevoDia -> selectedDay = nuevoDia }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido cambiante con animación
        AnimatedContent(
            targetState = selectedDay,
            label = "HorarioTransition",
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            }
        ) { dia ->
            HorarioDelDia(dia)
        }
    }
}

@Composable
fun HorarioDelDia(dia: String) {
    val clases = getClasesPorDia(dia)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()), // Scroll por si hay muchas clases
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (clases.isEmpty()) {
            Text(
                text = "No hay clases programadas",
                color = samf.gestorestudiantil.ui.theme.surfaceDimColor,
                modifier = Modifier.padding(top = 32.dp)
            )
        } else {
            clases.forEach { clase ->
                ItemHorario(clase)
            }
            Spacer(modifier = Modifier.height(20.dp)) // Espacio final
        }
    }
}

@Composable
fun ItemHorario(clase: ClaseHorario) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = clase.color), // Usamos el color de la materia
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = clase.materia,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black.copy(alpha = 0.8f) // Texto oscuro para contraste con colores pastel
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row (verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = clase.hora,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = clase.profesor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// Función auxiliar con los datos de la imagen
fun getClasesPorDia(dia: String): List<ClaseHorario> {
    val profesorInterfaces = "Juan Manuel"
    val profesorServicios = "Eduardo"
    val profesorDocker = "Miguel"
    val profesorAccesoDatos = "Oscar"
    val profesorSistemas = "Ruben"
    val profesorProgMovil = "Eduardo"
    val profesorFOL = "Raquel"
    val profesorSostenibilidad = "Mariluz"

    return when (dia) {
        "Lunes" -> listOf(
            ClaseHorario("Desarrollo de Interfaces", profesorInterfaces,"16:00 - 16:50", ColorDesarrollo),
            ClaseHorario("Desarrollo de Interfaces", profesorInterfaces, "16:55 - 17:45", ColorDesarrollo),
            ClaseHorario("Prog. Multiplataforma y Móvil", profesorProgMovil, "17:50 - 18:40", ColorProgMovil),
            ClaseHorario("--- RECREO ---", "", "18:40 - 19:05", ColorReceso),
            ClaseHorario("Prog. Multiplataforma y Móvil", profesorProgMovil, "19:05 - 19:55", ColorProgMovil),
            ClaseHorario("Prog. Servicios y Procesos", profesorServicios, "20:00 - 20:50", ColorServicios),
            ClaseHorario("Prog. Servicios y Procesos", profesorServicios, "20:55 - 21:45", ColorServicios)
        )
        "Martes" -> listOf(
            ClaseHorario("Sostenibilidad", profesorSostenibilidad, "16:00 - 16:50", ColorSostenibilidad),
            ClaseHorario("Sistemas de Gestión Empresarial", profesorSistemas, "16:55 - 17:45", ColorSistemas),
            ClaseHorario("Sistemas de Gestión Empresarial", profesorSistemas, "17:50 - 18:40", ColorSistemas),
            ClaseHorario("--- RECREO ---", "", "18:40 - 19:05", ColorReceso),
            ClaseHorario("FOL", profesorFOL, "19:05 - 19:55", ColorFOL),
            ClaseHorario("Prog. Servicios y Procesos", profesorServicios, "20:00 - 20:50", ColorServicios),
            ClaseHorario("Prog. Servicios y Procesos", profesorServicios, "20:55 - 21:45", ColorServicios)
        )
        "Miércoles" -> listOf(
            ClaseHorario("Acceso a Datos", profesorAccesoDatos, "16:00 - 16:50", ColorAccesoDatos),
            ClaseHorario("Acceso a Datos", profesorAccesoDatos, "16:55 - 17:45", ColorAccesoDatos),
            ClaseHorario("Docker", profesorDocker, "17:50 - 18:40", ColorDocker),
            ClaseHorario("--- RECREO ---", "", "18:40 - 19:05", ColorReceso),
            ClaseHorario("FOL", profesorFOL, "19:05 - 19:55", ColorFOL),
            ClaseHorario("Desarrollo de Interfaces", profesorInterfaces, "20:00 - 20:50", ColorDesarrollo),
            ClaseHorario("Desarrollo de Interfaces", profesorInterfaces, "20:55 - 21:45", ColorDesarrollo)
        )
        "Jueves" -> listOf(
            ClaseHorario("Acceso a Datos", profesorAccesoDatos, "16:00 - 16:50", ColorAccesoDatos),
            ClaseHorario("Acceso a Datos", profesorAccesoDatos, "16:55 - 17:45", ColorAccesoDatos),
            ClaseHorario("Prog. Multiplataforma y Móvil", profesorProgMovil, "17:50 - 18:40", ColorProgMovil),
            ClaseHorario("--- RECREO ---", "", "18:40 - 19:05", ColorReceso),
            ClaseHorario("Prog. Multiplataforma y Móvil", profesorProgMovil, "19:05 - 19:55", ColorProgMovil),
            ClaseHorario("Docker", profesorDocker, "20:00 - 20:50", ColorDocker),
            ClaseHorario("Sistemas de Gestión Empresarial", profesorSistemas, "20:55 - 21:45", ColorSistemas)
        )
        "Viernes" -> listOf(
            ClaseHorario("Sistemas de Gestión Empresarial", profesorSistemas, "16:00 - 16:50", ColorSistemas),
            ClaseHorario("Sistemas de Gestión Empresarial", profesorSistemas, "16:55 - 17:45", ColorSistemas),
            ClaseHorario("Desarrollo de Interfaces", profesorInterfaces, "17:50 - 18:40", ColorDesarrollo),
            ClaseHorario("--- RECREO ---", "", "18:40 - 19:05", ColorReceso),
            ClaseHorario("Desarrollo de Interfaces", profesorInterfaces, "19:05 - 19:55", ColorDesarrollo),
            ClaseHorario("Acceso a Datos", profesorAccesoDatos, "20:00 - 20:50", ColorAccesoDatos),
            ClaseHorario("Acceso a Datos", profesorAccesoDatos, "20:55 - 21:45", ColorAccesoDatos)
        )
        else -> emptyList()
    }
}