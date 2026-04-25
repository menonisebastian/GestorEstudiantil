package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.ui.components.CustomNotificationCard
import samf.gestorestudiantil.ui.components.CustomSearchBar
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun RecordatoriosEstudiantePanel(
    recordatorios: List<Recordatorio>,
    paddingValues: PaddingValues,
    onOpenDialog: (DialogState) -> Unit,
    onDelete: (Recordatorio) -> Unit,
    onUpdate: (Recordatorio) -> Unit
) {
    var textoBusquedaRaw by remember { mutableStateOf("") }
    var textoBusqueda by remember { mutableStateOf("") }

    LaunchedEffect(textoBusquedaRaw) {
        delay(300)
        textoBusqueda = textoBusquedaRaw
    }

    var filtroTipo by remember { mutableStateOf("") }

    val recordatoriosFiltrados by remember(textoBusqueda, filtroTipo, recordatorios) {
        derivedStateOf {
            val tiposSeleccionados = filtroTipo.split(",").filter { it.isNotEmpty() }
            recordatorios.filter {
                val coincideTexto = it.titulo.contains(textoBusqueda, ignoreCase = true) ||
                        it.descripcion.contains(textoBusqueda, ignoreCase = true)
                val coincideTipo = if (tiposSeleccionados.isEmpty()) true else {
                    tiposSeleccionados.any { tipo -> it.tipo.name.equals(tipo, ignoreCase = true) }
                }
                coincideTexto && coincideTipo
            }
        }
    }

    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        // BLOQUE 2: Contenido Principal
        if (recordatoriosFiltrados.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No hay recordatorios", color = surfaceDimColor, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 160.dp, bottom = 120.dp, start = 20.dp, end = 20.dp)
            ) {
                items(
                    items = recordatoriosFiltrados,
                    key = { it.id }
                ) { recordatorio ->
                    CustomNotificationCard(
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(150),
                            fadeOutSpec = tween(150),
                            placementSpec = tween(150)
                        ),
                        recordatorio = recordatorio,
                        onClick = {
                            onOpenDialog(
                                DialogState.EditRecordatorio(
                                    recordatorio = recordatorio,
                                    onSave = onUpdate
                                )
                            )
                        },
                        onDelete = {
                            onDelete(recordatorio)
                        }
                    )
                }
            }
        }

        // BLOQUE 1: Cabezal Flotante (Título + Barra de Búsqueda con Filtros)
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
                    text = "Mis Recordatorios",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )

                CustomSearchBar(
                    textoBusqueda = textoBusquedaRaw,
                    onValueChange = { textoBusquedaRaw = it },
                    onFilterClick = {
                        onOpenDialog(
                            DialogState.Filter(
                                tipo = "Recordatorio",
                                currentFilters = if (filtroTipo.isNotEmpty()) mapOf("tipo" to filtroTipo) else emptyMap(),
                                opcionesPersonalizadas = emptyMap(),
                                onApply = { seleccion -> filtroTipo = seleccion["tipo"] ?: "" }
                            )
                        )
                    },
                    filters = if (filtroTipo.isNotEmpty()) mapOf("tipo" to filtroTipo) else emptyMap(),
                    onRemoveFilter = { keyPlusValue ->
                        if (keyPlusValue.contains(":")) {
                            val (_, newValue) = keyPlusValue.split(":")
                            filtroTipo = newValue
                        } else {
                            filtroTipo = ""
                        }
                    }
                )
            }
        }
    }
}
