package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

    val asignaturasFiltradas = remember(textoBusqueda, asignaturas) {
        if (textoBusqueda.isBlank()) asignaturas
        else asignaturas.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
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
                items(asignaturasFiltradas) { materia ->
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
                    onFilterClick = {}
                )
            }
        }
    }
}
