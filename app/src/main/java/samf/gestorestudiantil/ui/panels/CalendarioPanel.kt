package samf.gestorestudiantil.ui.panels

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider as Divider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import samf.gestorestudiantil.ui.components.CustomNotificationCard
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
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
    onUpdateRecordatorio: (Recordatorio) -> Unit,
    onDeleteRecordatorio: (Recordatorio) -> Unit,
    onDeleteTarea: (Tarea) -> Unit,
    onDownloadTarea: (Tarea) -> Unit,
    onTareaClick: (Tarea) -> Unit
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .background(surfaceColor.copy(alpha = 0.95f), RoundedCornerShape(16.dp))
            .padding(16.dp),
            contentAlignment = Alignment.Center){
            Text(text = "Mi Calendario", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
        }
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
                val isToday = day.date == LocalDate.now()
                
                Day(
                    day = day,
                    isToday = isToday,
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

        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = selectedDate,
                label = "EventosContentTransition",
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                modifier = Modifier.fillMaxSize()
            ) { targetDate ->
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = targetDate?.let { "Eventos para el ${formatearFechaParaMostrar(it.toString(), prettyDate = true)}" } ?: "Selecciona un día",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    val eventosDelDia by remember(targetDate, eventosTotales) {
                        derivedStateOf {
                            val dateStr = targetDate?.toString() ?: ""
                            eventosTotales.filter { it.fechaIso == dateStr }
                        }
                    }

                    if (eventosDelDia.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay eventos para este día",
                                color = textColor.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 160.dp, start = 16.dp, end = 16.dp)
                        ) {
                            items(eventosDelDia) { evento ->
                                if (evento.tipo == TipoEventoVisual.RECORDATORIO) {
                                    val rec = recordatorios.find { it.id == evento.id }
                                    if (rec != null) {
                                        CustomNotificationCard(
                                            recordatorio = rec,
                                            onClick = { onUpdateRecordatorio(rec) },
                                            onDelete = { onDeleteRecordatorio(rec) }
                                        )
                                    }
                                } else {
                                    val tar = tareas.find { it.id == evento.id }
                                    if (tar != null) {
                                        val canDelete = usuarioActual.rol == "PROFESOR"
                                        val syntheticRec = Recordatorio(
                                            id = tar.id,
                                            titulo = tar.titulo,
                                            descripcion = tar.descripcion,
                                            fecha = SimpleDateFormat(
                                                "yyyy-MM-dd",
                                                Locale.getDefault()
                                            ).format(tar.fechaLimiteEntrega.toDate()),
                                            hora = SimpleDateFormat(
                                                "HH:mm",
                                                Locale.getDefault()
                                            ).format(tar.fechaLimiteEntrega.toDate()),
                                            tipo = tipoRecordatorio.TAREA
                                        )
                                        CustomNotificationCard(
                                            recordatorio = syntheticRec,
                                            onClick = { onTareaClick(tar) },
                                            onDelete = { if (canDelete) onDeleteTarea(tar) },
                                            showDelete = canDelete,
                                            onDownload = if (tar.adjunto != null) {
                                                { onDownloadTarea(tar) }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 103.dp)
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
    isToday: Boolean = false,
    onClick: (CalendarDay) -> Unit
) {

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(if (isSelected) tertiaryColor else Color.Transparent)
            .border(width = 1.dp, color = if (isToday) tertiaryColor else Color.Transparent, shape = CircleShape)
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

