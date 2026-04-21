package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.domain.toComposeColor
import samf.gestorestudiantil.domain.toComposeIcon
import samf.gestorestudiantil.ui.components.CardItem
import samf.gestorestudiantil.ui.components.CustomSearchBar
import samf.gestorestudiantil.ui.components.MensajeVacio
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun AsignaturasEstudiantePanel(
    asignaturas: List<Asignatura>,
    paddingValues: PaddingValues,
    onAsignaturaClick: (Asignatura) -> Unit
) {
    var textoBusqueda by remember { mutableStateOf("") }

    val asignaturasFiltradas by remember(textoBusqueda, asignaturas) {
        derivedStateOf {
            if (textoBusqueda.isBlank()) asignaturas
            else asignaturas.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
        }
    }

    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        // BLOQUE 2: Contenido Principal
        if (asignaturasFiltradas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                MensajeVacio()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 160.dp, bottom = 120.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val totalItems = asignaturasFiltradas.size
                val fullRows = totalItems / 3
                val remainingItems = totalItems % 3

                // Items que forman filas completas de 3
                items(asignaturasFiltradas.take(fullRows * 3)) { materia ->
                    CardItem(
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

                // Manejo de la última fila si tiene 1 o 2 elementos
                if (remainingItems > 0) {
                    val lastRowItems = asignaturasFiltradas.takeLast(remainingItems)

                    if (remainingItems == 1) {
                        // Centrar 1 elemento: Espacio vacío (span 1) + Item (span 1) + Espacio vacío (span 1)
                        item(span = { GridItemSpan(1) }) { Box(Modifier) }
                        item(span = { GridItemSpan(1) }) {
                            CardItem(
                                item = lastRowItems[0],
                                getIcono = { it.iconoName.toComposeIcon() },
                                getAcron = { it.acronimo },
                                getNombre = { it.nombre },
                                getColorFondo = { it.colorFondoHex.toComposeColor() },
                                getColorIcono = { it.colorIconoHex.toComposeColor() },
                                notificaciones = lastRowItems[0].numNotificaciones,
                                onClick = { onAsignaturaClick(lastRowItems[0]) }
                            )
                        }
                    } else {
                        // Centrar 2 elementos usando pesos para mantener el tamaño relativo (remainingItems es 2)
                        item(span = { GridItemSpan(3) }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Espacio flexible a la izquierda (1/6 del total para centrar 2 en 3 columnas)
                                Spacer(modifier = Modifier.weight(0.5f))
                                
                                lastRowItems.forEachIndexed { index, materia ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        CardItem(
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
                                    if (index == 0) Spacer(Modifier.padding(horizontal = 6.dp))
                                }

                                // Espacio flexible a la derecha
                                Spacer(modifier = Modifier.weight(0.5f))
                            }
                        }
                    }
                }
            }
        }

        // BLOQUE 1: Cabezal Flotante (Título + Barra de Búsqueda)
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
                    textoBusqueda = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    onFilterClick = null
                )
            }
        }
    }
}
