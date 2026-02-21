package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import samf.gestorestudiantil.data.models.listaAsignaturas
import samf.gestorestudiantil.domain.toComposeColor
import samf.gestorestudiantil.domain.toComposeIcon
import samf.gestorestudiantil.ui.components.CardItem
import samf.gestorestudiantil.ui.components.CustomSearchBar
import samf.gestorestudiantil.ui.components.MensajeVacio
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun AsignaturasEstudiantePanel(paddingValues: PaddingValues)
{
    var textoBusqueda by remember { mutableStateOf("") }

    val asignaturasFiltradas = remember(textoBusqueda) {
        if (textoBusqueda.isBlank()) listaAsignaturas
        else listaAsignaturas.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
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

            CustomSearchBar(textoBusqueda, onValueChange = { textoBusqueda = it })
        }

        // BLOQUE 2: Contenido Borde a Borde (Fuera del bloque con padding)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp), // Mantiene el alineamiento visual
            modifier = Modifier.fillMaxWidth()
        ) {
            items(asignaturasFiltradas) { materia ->
                CardItem(item = materia,
                    getIcono = { it.iconoName.toComposeIcon() },
                    getNombre = {it.nombre},
                    getHoras = {it.horas},
                    getColorFondo = {it.colorFondoHex.toComposeColor()},
                    getColorIcono = {it.colorIconoHex.toComposeColor()}
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