package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import samf.gestorestudiantil.data.enums.tipoRecordatorio
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.data.models.Tarea
import samf.gestorestudiantil.domain.toComposeColor
import samf.gestorestudiantil.domain.toComposeIcon
import samf.gestorestudiantil.ui.components.CardItem
import samf.gestorestudiantil.ui.components.CustomNotificationCard
import samf.gestorestudiantil.ui.components.CustomSearchBar
import samf.gestorestudiantil.ui.components.MensajeVacio
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.textColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AsignaturasEstudiantePanel(
    asignaturas: List<Asignatura>,
    tareas: List<Tarea> = emptyList(),
    paddingValues: PaddingValues,
    onAsignaturaClick: (Asignatura) -> Unit,
    onTareaClick: (Tarea) -> Unit = {},
    onDownloadTarea: (Tarea) -> Unit = {}
) {
    var textoBusquedaRaw by rememberSaveable { mutableStateOf("") }
    var textoBusqueda by remember { mutableStateOf("") }

    LaunchedEffect(textoBusquedaRaw) {
        delay(150)
        textoBusqueda = textoBusquedaRaw
    }

    val asignaturasFiltradas by remember(textoBusqueda, asignaturas) {
        derivedStateOf {
            if (textoBusqueda.isBlank()) {
                asignaturas
            } else {
                asignaturas.filter { 
                    it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                    it.acronimo.contains(textoBusqueda, ignoreCase = true)
                }
            }
        }
    }

    val tareasUrgentes by remember(tareas) {
        derivedStateOf {
            val hoy = Calendar.getInstance()
            val en7Dias = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }
            
            tareas.filter { tarea ->
                val fechaTarea = Calendar.getInstance().apply { time = tarea.fechaLimiteEntrega.toDate() }
                !fechaTarea.before(hoy) && !fechaTarea.after(en7Dias)
            }.sortedBy { it.fechaLimiteEntrega }
        }
    }

    var dashboardExpandido by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        // BLOQUE 2: Contenido Principal
        if (asignaturasFiltradas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                MensajeVacio()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
                modifier = Modifier.fillMaxSize()
            )
            {
                // Item de espaciado para el cabezal flotante
                item(span = { GridItemSpan(3) }) {
                    Spacer(modifier = Modifier.height(140.dp))
                }

                if (tareasUrgentes.isNotEmpty() && textoBusquedaRaw.isBlank())
                {
                    item(span = { GridItemSpan(3) })
                    {
                        val rotationState by animateFloatAsState(targetValue = if (dashboardExpandido) 180f else 0f)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        )
                        {
                            Column(modifier = Modifier.padding(16.dp))
                            {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = if (dashboardExpandido) 12.dp else 0.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Próximas entregas",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(primaryColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = tareasUrgentes.size.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = primaryColor
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { dashboardExpandido = !dashboardExpandido },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExpandMore,
                                            contentDescription = if (dashboardExpandido) "Colapsar" else "Expandir",
                                            tint = textColor,
                                            modifier = Modifier.rotate(rotationState)
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = dashboardExpandido) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        tareasUrgentes.forEach { tarea ->
                                            val syntheticRec = Recordatorio(
                                                id = tarea.id,
                                                titulo = tarea.titulo,
                                                descripcion = tarea.descripcion,
                                                fecha = SimpleDateFormat(
                                                    "yyyy-MM-dd",
                                                    Locale.getDefault()
                                                ).format(tarea.fechaLimiteEntrega.toDate()),
                                                hora = SimpleDateFormat(
                                                    "HH:mm",
                                                    Locale.getDefault()
                                                ).format(tarea.fechaLimiteEntrega.toDate()),
                                                tipo = tipoRecordatorio.TAREA
                                            )
                                            CustomNotificationCard(
                                                isAsignaturaPanel = true,
                                                recordatorio = syntheticRec,
                                                onClick = { onTareaClick(tarea) },
                                                showDelete = false,
                                                onDownload = if (tarea.adjunto != null) {
                                                    { onDownloadTarea(tarea) }
                                                } else null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                }

                items(
                    items = asignaturasFiltradas,
                    key = { it.id.ifEmpty { it.idDocumento } }
                )
                {
                    materia ->
                    CardItem(
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(150),
                            fadeOutSpec = tween(150),
                            placementSpec = tween(150)
                        ),
                        item = materia,
                        getIcono = { it.iconoName.toComposeIcon() },
                        getAcron = { it.acronimo },
                        getNombre = { it.nombre },
                        getColorFondo = { it.colorFondoHex.toComposeColor() },
                        getColorIcono = { it.colorIconoHex.toComposeColor() },
                        notificaciones = materia.numNotificaciones,
                        onClick = { onAsignaturaClick(materia) }
                    )
                }
            }
        }

        // BLOQUE 1: Cabezal Flotante (al final para estar encima)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Mis Asignaturas",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )

                CustomSearchBar(
                    textoBusqueda = textoBusquedaRaw,
                    onValueChange = { textoBusquedaRaw = it },
                    onFilterClick = null
                )
            }
        }
    }
}
