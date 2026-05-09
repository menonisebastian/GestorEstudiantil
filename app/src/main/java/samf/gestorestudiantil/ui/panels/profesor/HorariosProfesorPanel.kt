package samf.gestorestudiantil.ui.panels.profesor

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import samf.gestorestudiantil.R
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import androidx.core.graphics.toColorInt

@Composable
fun HorariosProfesorPanel(
    paddingValues: PaddingValues,
    horarios: List<Horario>,
    asignaturas: List<Asignatura>,
    turno: String = "matutino"
) {
    var selectedDay by remember { mutableStateOf("Lunes") }

    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        AnimatedContent(
            targetState = selectedDay,
            label = "HorarioTransition",
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            modifier = Modifier.fillMaxSize()
        ) { dia ->
            HorarioDelDiaProfesor(dia, horarios, asignaturas, turno)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_my_schedule_teacher),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp)
                )

                WeekNavBar(
                    selectedItem = selectedDay,
                    onItemSelected = { nuevoDia -> selectedDay = nuevoDia }
                )
            }
        }
    }
}

@Composable
fun HorarioDelDiaProfesor(dia: String, horarios: List<Horario>, asignaturas: List<Asignatura>, turno: String) {
    val slots = remember(turno) {
        if (turno.lowercase().trim() == "matutino") Horario.HORAS_MATUTINO else Horario.HORAS_VESPERTINO
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 140.dp, bottom = 120.dp)
    ) {
        items(slots) { slot ->
            val h = remember(horarios, dia, slot) {
                horarios.find { it.dia.equals(dia, ignoreCase = true) && "${it.horaInicio.trim()} - ${it.horaFin.trim()}" == slot.trim() }
            }
            val asig = remember(asignaturas, h) {
                asignaturas.find { it.id == h?.asignaturaId }
            }
            ItemHorarioProfesor(slot, h, asig)
        }
    }
}

@Composable
fun ItemHorarioProfesor(slot: String, horario: Horario?, asignatura: Asignatura?) {
    val isReceso = slot.contains("RECREO") || slot.contains("11:10 - 11:35") || slot.contains("18:40 - 19:05")

    val colorFondo = when {
        isReceso -> surfaceDimColor.copy(alpha = 0.5f)
        asignatura != null -> {
            try {
                Color(asignatura.colorFondoHex.toColorInt())
            } catch (_: Exception) {
                surfaceDimColor.copy(alpha = 0.2f)
            }
        }
        else -> surfaceDimColor.copy(alpha = 0.1f)
    }

    val contentColor = Color.Black
    val subColor = surfaceDimColor

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (asignatura != null || isReceso) colorFondo else surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        isReceso -> stringResource(R.string.schedule_recess)
                        horario != null -> horario.asignaturaAcronimo.ifEmpty { stringResource(R.string.schedule_assigned_subject) }
                        else -> "---"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = contentColor
                )
                if (asignatura != null && !isReceso) {
                    Text(
                        text = asignatura.nombre,
                        fontSize = 12.sp,
                        color = subColor,
                        lineHeight = 14.sp
                    )
                }
                Text(
                    text = slot,
                    fontSize = 13.sp,
                    color = subColor
                )
            }
            
            if (horario != null && !isReceso) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${horario.cursoAcronimo} ${horario.turnoLetra}${horario.cicloNum}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = contentColor
                    )
                    if (horario.aula.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.label_classroom_format, horario.aula),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = subColor
                        )
                    }
                }
            }
        }
    }
}
