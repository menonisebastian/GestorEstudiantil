package samf.gestorestudiantil.ui.panels.profesor

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.ui.components.WeekNavBar
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun HorariosProfesorPanel(
    paddingValues: PaddingValues,
    horarios: List<Horario>,
    asignaturas: List<Asignatura>
) {
    var selectedDay by remember { mutableStateOf("Lunes") }

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        Column(modifier = Modifier.padding(horizontal = 20.dp))
        {
            Text(
                text = "Mi Horario Docente",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Barra de selección de días
        WeekNavBar(
            selectedItem = selectedDay,
            onItemSelected = { nuevoDia -> selectedDay = nuevoDia }
        )

        // Contenido cambiante con animación
        AnimatedContent(
            targetState = selectedDay,
            label = "HorarioTransition",
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            }
        ) { dia ->
            HorarioDelDiaProfesor(dia, horarios, asignaturas)
        }
    }
}

@Composable
fun HorarioDelDiaProfesor(dia: String, horarios: List<Horario>, asignaturas: List<Asignatura>) {
    // Para el profesor, mostramos solo sus bloques asignados, ordenados por hora
    val clasesHoy = horarios.filter { it.dia == dia }
        .sortedBy { it.horaInicio }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (clasesHoy.isEmpty()) {
            item {
                Text(
                    text = "No tienes clases asignadas este día",
                    color = surfaceDimColor,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }
        } else {
            items(clasesHoy) { h ->
                val asig = asignaturas.find { it.idFirestore == h.asignaturaId }
                ItemHorarioProfesor(h, asig)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ItemHorarioProfesor(horario: Horario, asignatura: Asignatura?) {
    val colorFondo = try {
        Color(android.graphics.Color.parseColor(asignatura?.colorFondoHex ?: "#E8E8E8"))
    } catch (e: Exception) {
        Color.LightGray.copy(alpha = 0.2f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = horario.asignaturaAcronimo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.8f)
                )
                if (asignatura != null) {
                    Text(
                        text = asignatura.nombre,
                        fontSize = 12.sp,
                        color = Color.Black.copy(alpha = 0.6f),
                        lineHeight = 14.sp
                    )
                }
                Text(
                    text = "${horario.horaInicio} - ${horario.horaFin}",
                    fontSize = 13.sp,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                // Generamos el código del grupo: Acrónimo + Inicial Turno + Ciclo (Ej: DAM V1)
                val inicialTurno = when(horario.turno.lowercase().trim()) {
                    "matutino" -> "M"
                    "vespertino" -> "V"
                    else -> horario.turno.take(1).uppercase()
                }
                
                // Extraemos el acrónimo del cursoId (ej: ies_comercio_DAM -> DAM)
                val acronimoCurso = horario.cursoId.substringAfterLast("_").uppercase()

                Text(
                    text = "$acronimoCurso $inicialTurno${horario.cicloNum}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black.copy(alpha = 0.8f)
                )
                if (horario.aula.isNotEmpty()) {
                    Text(
                        text = "Aula: ${horario.aula}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
