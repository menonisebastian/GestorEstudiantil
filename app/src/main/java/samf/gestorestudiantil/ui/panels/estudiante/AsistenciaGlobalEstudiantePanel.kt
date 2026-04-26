package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Asistencia
import samf.gestorestudiantil.data.models.AsistenciaEstado
import samf.gestorestudiantil.ui.components.CustomSearchBar
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AsistenciaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AsistenciaGlobalEstudiantePanel(
    estudianteId: String,
    asignaturas: List<Asignatura>,
    onOpenDialog: (DialogState) -> Unit,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    viewModel: AsistenciaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    var filtroEstado by remember { mutableStateOf("") }
    var filtroAsignatura by remember { mutableStateOf("") }

    LaunchedEffect(estudianteId) {
        viewModel.cargarAsistenciasEstudiante(estudianteId)
    }

    val asignaturasMap = remember(asignaturas) { asignaturas.associateBy { it.id } }
    val asignaturasOpciones = remember(asignaturas) { asignaturas.map { it.acronimo }.distinct().sorted() }
    val estadosOpciones = listOf("PRESENTE", "AUSENTE", "TARDE", "JUSTIFICADO")

    val asistenciasFiltradas by remember(state.asistencias, filtroEstado, filtroAsignatura) {
        derivedStateOf {
            state.asistencias.filter { asis ->
                val coincideEstado = if (filtroEstado.isEmpty()) true else {
                    filtroEstado.split(",").contains(asis.estado.name)
                }
                val coincideAsignatura = if (filtroAsignatura.isEmpty()) true else {
                    val asig = asignaturasMap[asis.asignaturaId]
                    filtroAsignatura.split(",").contains(asig?.acronimo)
                }
                coincideEstado && coincideAsignatura
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor).padding(paddingValues)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header con Filtros
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Mi Asistencia Global",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )

                    CustomSearchBar(
                        textoBusqueda = "",
                        onValueChange = {},
                        onFilterClick = {
                            onOpenDialog(DialogState.Filter(
                                tipo = "Asistencia",
                                currentFilters = buildMap {
                                    if (filtroEstado.isNotEmpty()) put("estado", filtroEstado)
                                    if (filtroAsignatura.isNotEmpty()) put("asignatura", filtroAsignatura)
                                },
                                opcionesPersonalizadas = mapOf(
                                    "estados" to estadosOpciones,
                                    "asignaturas" to asignaturasOpciones
                                ),
                                onApply = { seleccion ->
                                    filtroEstado = seleccion["estado"] ?: ""
                                    filtroAsignatura = seleccion["asignatura"] ?: ""
                                }
                            ))
                        },
                        filters = buildMap {
                            if (filtroEstado.isNotEmpty()) put("estado", filtroEstado)
                            if (filtroAsignatura.isNotEmpty()) put("asignatura", filtroAsignatura)
                        },
                        onRemoveFilter = { keyPlusValue ->
                            val (key, newValue) = if (keyPlusValue.contains(":")) {
                                val parts = keyPlusValue.split(":")
                                parts[0] to parts[1]
                            } else {
                                keyPlusValue to ""
                            }
                            if (key == "estado") filtroEstado = newValue
                            if (key == "asignatura") filtroAsignatura = newValue
                        }
                    )
                }
            }

            if (asistenciasFiltradas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (state.asistencias.isEmpty()) "No tienes registros de asistencia" else "No hay resultados para los filtros seleccionados",
                        color = surfaceDimColor
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(asistenciasFiltradas) { asistencia ->
                        val asignatura = asignaturasMap[asistencia.asignaturaId]
                        ItemAsistenciaEstudiante(
                            asistencia = asistencia,
                            asignaturaAcronimo = asignatura?.acronimo ?: "S/A",
                            asignaturaNombre = asignatura?.nombre ?: "Asignatura desconocida",
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(150),
                                fadeOutSpec = tween(150),
                                placementSpec = tween(150)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemAsistenciaEstudiante(
    asistencia: Asistencia,
    asignaturaAcronimo: String,
    asignaturaNombre: String,
    modifier: Modifier = Modifier
) {
    val fechaStr = remember(asistencia.fecha) {
        SimpleDateFormat("EEEE, dd 'de' MMMM", Locale.getDefault()).format(Date(asistencia.fecha))
            .replaceFirstChar { it.uppercase() }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asignaturaAcronimo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = asignaturaNombre,
                    fontSize = 12.sp,
                    color = surfaceDimColor,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fechaStr,
                    fontSize = 11.sp,
                    color = primaryColor,
                    fontWeight = FontWeight.Medium
                )
            }

            val (color, label) = when (asistencia.estado) {
                AsistenciaEstado.PRESENTE -> Color(0xFF4CAF50) to "PRESENTE"
                AsistenciaEstado.AUSENTE -> Color(0xFFF44336) to "AUSENTE"
                AsistenciaEstado.TARDE -> Color(0xFFFF9800) to "TARDE"
                AsistenciaEstado.JUSTIFICADO -> Color(0xFF2196F3) to "JUSTIFICADO"
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
        }
    }
}
