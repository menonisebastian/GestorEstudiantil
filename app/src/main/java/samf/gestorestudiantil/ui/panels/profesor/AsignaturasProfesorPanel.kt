package samf.gestorestudiantil.ui.panels.profesor

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.toComposeColor
import samf.gestorestudiantil.domain.toComposeIcon
import samf.gestorestudiantil.ui.components.CardItem
import samf.gestorestudiantil.ui.components.CustomSearchBar
import samf.gestorestudiantil.ui.components.MensajeVacio
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel

@Composable
fun AsignaturasProfesorPanel(
    profesor: User,
    paddingValues: PaddingValues,
    onAsignaturaClick: (Asignatura) -> Unit
) {
    val viewModel: ProfesorViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    var textoBusqueda by remember { mutableStateOf("") }

    LaunchedEffect(profesor.id) {
        viewModel.cargarAsignaturas(profesor.id)
    }

    val asignaturasFiltradas = remember(textoBusqueda, state.asignaturas) {
        if (textoBusqueda.isBlank()) state.asignaturas
        else state.asignaturas.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        // BLOQUE 2: Contenido Principal
        if (asignaturasFiltradas.isEmpty() && !state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    val cursoAcron = materia.cursoId.split("_").lastOrNull()?.uppercase() ?: ""
                    val turnoLetra = if (materia.turno.contains("matutino", ignoreCase = true)) "M" else "V"
                    val badge = "$cursoAcron$turnoLetra${materia.cicloNum}"

                    CardItem(
                        item = materia,
                        getIcono = { it.iconoName.toComposeIcon() },
                        getAcron = { it.acronimo },
                        getNombre = { it.nombre },
                        getColorFondo = { it.colorFondoHex.toComposeColor() },
                        getColorIcono = { it.colorIconoHex.toComposeColor() },
                        notificaciones = materia.numNotificaciones,
                        badgeText = badge,
                        onClick = { onAsignaturaClick(materia) }
                    )
                }
            }
        }

        // BLOQUE 1: Cabezal Flotante
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
