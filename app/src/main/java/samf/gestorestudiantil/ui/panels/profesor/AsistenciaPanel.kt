package samf.gestorestudiantil.ui.panels.profesor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.AsistenciaEstado
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.AccImg
import samf.gestorestudiantil.ui.components.CustomFAB
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AsistenciaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaPanel(
    asignatura: Asignatura,
    onOpenDialog: (DialogState) -> Unit,
    viewModel: AsistenciaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(asignatura.id) {
        viewModel.setFechaActual()
        viewModel.cargarDatosAsignatura(asignatura)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header con Fecha
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Fecha de Asistencia", fontSize = 12.sp, color = surfaceDimColor)
                        val fechaStr = remember(state.fechaSeleccionada) {
                            if (state.fechaSeleccionada == 0L) "" else
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(state.fechaSeleccionada))
                        }
                        Text(fechaStr, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                    }
                    IconButton(onClick = {
                        val currentFechaStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(if (state.fechaSeleccionada == 0L) System.currentTimeMillis() else state.fechaSeleccionada))
                        onOpenDialog(DialogState.DatePicker(
                            initialDate = currentFechaStr,
                            allowPastDates = true,
                            onDateSelected = { dateStr ->
                                val millis = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(dateStr)?.time ?: System.currentTimeMillis()
                                viewModel.cambiarFecha(millis)
                            }
                        ))
                    }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Cambiar fecha", tint = primaryColor)
                    }
                }
            }

            // Lista de Estudiantes
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.estudiantes) { estudiante ->
                    val asistencia = state.asistencias.find { it.estudianteId == estudiante.id }
                    
                    AsistenciaEstudianteItem(
                        estudiante = estudiante,
                        estado = asistencia?.estado,
                        onImageClick = {
                            onOpenDialog(DialogState.UserProfile(estudiante))
                        },
                        onClick = {
                            onOpenDialog(DialogState.SelectAsistencia(
                                estudianteNombre = estudiante.nombre,
                                estadoActual = asistencia?.estado,
                                onEstadoSelected = { nuevo ->
                                    viewModel.actualizarEstadoAsistencia(estudiante.id, nuevo, asignatura.id)
                                }
                            ))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AsistenciaEstudianteItem(
    estudiante: User,
    estado: AsistenciaEstado?,
    onImageClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccImg(
                userName = estudiante.nombre,
                imgUrl = estudiante.imgUrl,
                size = 40.dp,
                onClick = onImageClick
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = estudiante.nombre,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            
            if (estado != null) {
                val color = when (estado) {
                    AsistenciaEstado.PRESENTE -> Color(0xFF4CAF50)
                    AsistenciaEstado.AUSENTE -> Color(0xFFF44336)
                    AsistenciaEstado.TARDE -> Color(0xFFFF9800)
                    AsistenciaEstado.JUSTIFICADO -> Color(0xFF2196F3)
                }
                val label = when (estado) {
                    AsistenciaEstado.PRESENTE -> "PRESENTE"
                    AsistenciaEstado.AUSENTE -> "AUSENTE"
                    AsistenciaEstado.TARDE -> "TARDE"
                    AsistenciaEstado.JUSTIFICADO -> "JUSTIFICADO"
                }

                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = label,
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = surfaceDimColor.copy(alpha = 0.3f)
                )
            }
        }
    }
}
