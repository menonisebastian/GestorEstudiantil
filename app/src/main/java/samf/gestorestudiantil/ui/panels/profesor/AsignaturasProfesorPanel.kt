package samf.gestorestudiantil.ui.panels.profesor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Mis Asignaturas",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
            )

            CustomSearchBar(textoBusqueda, onValueChange = { textoBusqueda = it }, onFilterClick = {})
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(asignaturasFiltradas) { materia ->
                CardItem(
                    item = materia,
                    getIcono = { it.iconoName.toComposeIcon() },
                    getAcron = { it.acronimo },
                    getNombre = { it.nombre },
                    getColorFondo = { it.colorFondoHex.toComposeColor() },
                    getColorIcono = { it.colorIconoHex.toComposeColor() },
                    onClick = { onAsignaturaClick(materia) }
                )
            }
        }

        if (asignaturasFiltradas.isEmpty() && !state.isLoading) {
            MensajeVacio()
        }
    }
}
