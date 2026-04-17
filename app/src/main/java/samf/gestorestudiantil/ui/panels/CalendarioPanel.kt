package samf.gestorestudiantil.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.HorizontalDivider as Divider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import kotlinx.coroutines.launch
import samf.gestorestudiantil.data.enums.tipoRecordatorio
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.data.models.Tarea
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.formatearFechaParaMostrar
import samf.gestorestudiantil.ui.components.CustomFAB
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.tertiaryColor
import samf.gestorestudiantil.ui.theme.textColor
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

// 1. Modelo unificado para la UI
data class EventoCalendario(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val fechaIso: String, // Formato "yyyy-MM-dd"
    val hora: String?,
    val tipo: TipoEventoVisual,
    val subtipoRecordatorio: tipoRecordatorio? = null
)

enum class TipoEventoVisual {
    TAREA, RECORDATORIO
}

fun mapearEventos(tareas: List<Tarea>, recordatorios: List<Recordatorio>): List<EventoCalendario> {
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    val eventosTareas = tareas.map { tarea ->
        val date = tarea.fechaLimiteEntrega.toDate()
        EventoCalendario(
            id = tarea.id,
            titulo = tarea.titulo,
            descripcion = tarea.descripcion,
            fechaIso = dateFormatter.format(date),
            hora = timeFormatter.format(date),
            tipo = TipoEventoVisual.TAREA
        )
    }

    val eventosRecordatorios = recordatorios.map { rec ->
        EventoCalendario(
            id = rec.id,
            titulo = rec.titulo,
            descripcion = rec.descripcion,
            fechaIso = rec.fecha.replace("/", "-"),
            hora = rec.hora.takeIf { it.isNotBlank() },
            tipo = TipoEventoVisual.RECORDATORIO,
            subtipoRecordatorio = rec.tipo
        )
    }

    return (eventosTareas + eventosRecordatorios).sortedWith(compareBy({ it.fechaIso }, { it.hora }))
}

@Composable
fun CalendarioPanel(
    usuarioActual: User,
    tareas: List<Tarea>,
    recordatorios: List<Recordatorio>,
    paddingValues: PaddingValues,
    onAddRecordatorio: (String) -> Unit,
    onAddTarea: (String) -> Unit,
    onDeleteRecordatorio: (Recordatorio) -> Unit,
    onDeleteTarea: (Tarea) -> Unit
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }
    val daysOfWeek = remember { daysOfWeek() }
    
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )
    val coroutineScope = rememberCoroutineScope()

    val eventosTotales = remember(tareas, recordatorios) {
        mapearEventos(tareas, recordatorios)
    }

    val eventosDelDiaSeleccionado = remember(selectedDate, eventosTotales) {
        val dateStr = selectedDate?.toString() ?: ""
        eventosTotales.filter { it.fechaIso == dateStr }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Cabecera del Calendario
        MonthHeader(
            currentMonth = calendarState.firstVisibleMonth.yearMonth,
            onPreviousMonth = {
                coroutineScope.launch {
                    calendarState.animateScrollToMonth(calendarState.firstVisibleMonth.yearMonth.previousMonth)
                }
            },
            onNextMonth = {
                coroutineScope.launch {
                    calendarState.animateScrollToMonth(calendarState.firstVisibleMonth.yearMonth.nextMonth)
                }
            }
        )

        HorizontalCalendar(
            state = calendarState,
            dayContent = { day ->
                val isSelected = selectedDate == day.date
                val tieneEventos = eventosTotales.any { it.fechaIso == day.date.toString() }
                
                Day(
                    day = day,
                    isSelected = isSelected,
                    tieneEventos = tieneEventos,
                    onClick = { selectedDate = it.date }
                )
            },
            monthHeader = {
                DaysOfWeekTitle(daysOfWeek = daysOfWeek)
            }
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp), color = surfaceColor.copy(alpha = 0.2f))

        Text(
            text = selectedDate?.let { "Eventos para el ${formatearFechaParaMostrar(it.toString())}" } ?: "Selecciona un día",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Box(modifier = Modifier.weight(1f)) {
            if (eventosDelDiaSeleccionado.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(bottom = 140.dp), contentAlignment = Alignment.Center) {
                    Text(text = "No hay eventos para este día", color = textColor.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 160.dp)
                ) {
                    items(eventosDelDiaSeleccionado) { evento ->
                        val canDelete = if (evento.tipo == TipoEventoVisual.TAREA) {
                            usuarioActual.rol == "PROFESOR"
                        } else {
                            true // Recordatorios personales siempre se pueden borrar
                        }
                        
                        EventoItem(
                            evento = evento,
                            canDelete = canDelete,
                            onDelete = {
                                if (evento.tipo == TipoEventoVisual.RECORDATORIO) {
                                    val rec = recordatorios.find { it.id == evento.id }
                                    if (rec != null) onDeleteRecordatorio(rec)
                                } else {
                                    val tar = tareas.find { it.id == evento.id }
                                    if (tar != null) onDeleteTarea(tar)
                                }
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 100.dp)
            ) {
                CustomFAB(
                    onClick = { onAddRecordatorio(selectedDate?.toString() ?: LocalDate.now().toString()) },
                    text = "Añadir Recordatorio"
                )
            }
        }
    }
}

@Composable
fun MonthHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior", tint = textColor)
        }
        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es"))} ${currentMonth.year}".uppercase(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente", tint = textColor)
        }
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es")),
                color = textColor.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    tieneEventos: Boolean,
    onClick: (CalendarDay) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(if (isSelected) tertiaryColor else Color.Transparent)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = if (isSelected) Color.White else if (day.position == DayPosition.MonthDate) textColor else textColor.copy(alpha = 0.3f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (tieneEventos) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                )
            }
        }
    }
}

@Composable
fun EventoItem(
    evento: EventoCalendario,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = evento.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = if (evento.tipo == TipoEventoVisual.TAREA) Color(0xFFFF5722) else evento.subtipoRecordatorio?.color ?: Color(0xFF64B5F6),
                    shape = CircleShape
                ) {
                    val label = if (evento.tipo == TipoEventoVisual.TAREA) "TAREA" else evento.subtipoRecordatorio?.label?.uppercase() ?: "RECORDATORIO"
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = evento.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.8f)
                    )
                    evento.hora?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hora: $it",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                }
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = surfaceDimColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
