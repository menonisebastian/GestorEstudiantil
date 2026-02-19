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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.models.listaMaterias
import samf.gestorestudiantil.ui.components.CardItem
import samf.gestorestudiantil.ui.components.MensajeVacio
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun MateriasEstudiantePanel(paddingValues: PaddingValues)
{
    var textoBusqueda by remember { mutableStateOf("") }

    val materiasFiltrados = remember(textoBusqueda) {
        if (textoBusqueda.isBlank()) listaMaterias
        else listaMaterias.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // BLOQUE 1: Contenido con márgenes (Agrupado)
        // Aquí metemos todo lo que SÍ necesita márgenes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp) // <--- Un solo padding para todo este bloque
        ) {
            // Barra de Búsqueda (ya no necesita padding individual)
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = { Text("Buscar", color = surfaceDimColor) },
                leadingIcon = { Icon(Icons.Default.Search, "Buscar", tint = Color.Gray) },
                trailingIcon = { Icon(Icons.Outlined.FilterList, "Filtrar", tint = Color.Gray) },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = surfaceColor,
                    unfocusedContainerColor = surfaceColor,
                    disabledContainerColor = surfaceColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Título (ya no necesita padding individual)
            Text(
                text = "Mis Materias",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BLOQUE 2: Contenido Borde a Borde (Fuera del bloque con padding)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp), // Mantiene el alineamiento visual
            modifier = Modifier.fillMaxWidth()
        ) {
            items(materiasFiltrados) { materia ->
                CardItem(item = materia,
                    getIcono = { it.icono },
                    getNombre = {it.nombre},
                    getHoras = {it.horas},
                    getColorFondo = {it.colorFondo},
                    getColorIcono = {it.colorIcono}
                )
            }
        }

        // Mensaje vacío (reutilizando el padding del bloque 1 si quisiéramos,
        // o aplicándolo aquí si es un caso especial)
        if (materiasFiltrados.isEmpty()) {
            MensajeVacio()
        }
    }
}