package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun AsignaturasEstudiantePanel(
    asignaturas: List<Asignatura>,
    paddingValues: PaddingValues,
    onAsignaturaClick: (Asignatura) -> Unit
)
{
    var textoBusqueda by remember { mutableStateOf("") }

    val asignaturasFiltradas = remember(textoBusqueda, asignaturas) {
        if (textoBusqueda.isBlank()) asignaturas
        else asignaturas.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


        // BLOQUE 1: Contenido con márgenes (Agrupado)
        // Aquí metemos todo lo que SÍ necesita márgenes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp) // <--- Un solo padding para todo este bloque
        ) {


            // Título (ya no necesita padding individual)
            Text(
                text = "Mis Asignaturas",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
            )

            // Barra de Búsqueda (ya no necesita padding individual)

            CustomSearchBar(textoBusqueda, onValueChange = { textoBusqueda = it }, onFilterClick = {})
        }

        // BLOQUE 2: Contenido Borde a Borde (Fuera del bloque con padding)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(asignaturasFiltradas) { materia ->
                CardItem(item = materia,
                    getIcono = { it.iconoName.toComposeIcon() },
                    getAcron = {it.acronimo},
                    getNombre = {it.nombre},
                    getColorFondo = {it.colorFondoHex.toComposeColor()},
                    getColorIcono = {it.colorIconoHex.toComposeColor()},
                    notificaciones = materia.numNotificaciones,
                    onClick = { onAsignaturaClick(materia) }
                )
            }
        }

        // Mensaje vacío (reutilizando el padding del bloque 1 si quisiéramos,
        // o aplicándolo aquí si es un caso especial)
        if (asignaturasFiltradas.isEmpty()) {
            MensajeVacio()
        }
    }
}