package samf.gestorestudiantil.ui.panels.profesor

import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.toComposeColor
import samf.gestorestudiantil.domain.toComposeIcon
import samf.gestorestudiantil.ui.components.CardItem
import samf.gestorestudiantil.ui.components.CustomSearchBar
import samf.gestorestudiantil.ui.components.MensajeVacio
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel

@Composable
fun AsignaturasProfesorPanel(
    profesor: User,
    paddingValues: PaddingValues,
    onAsignaturaClick: (Asignatura) -> Unit,
    onOpenDialog: (DialogState) -> Unit = {},
    viewModel: ProfesorViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var textoBusquedaRaw by rememberSaveable { mutableStateOf("") }
    var textoBusqueda by remember { mutableStateOf("") }
    var currentFilters by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(textoBusquedaRaw) {
        delay(300)
        textoBusqueda = textoBusquedaRaw
    }

    LaunchedEffect(profesor.id) {
        viewModel.cargarAsignaturas(profesor.id)
    }

    val asignaturasFiltradas by remember(textoBusqueda, currentFilters, state.asignaturas) {
        derivedStateOf {
            state.asignaturas.filter { materia ->
                val matchTexto = textoBusqueda.isBlank() || 
                                materia.nombre.contains(textoBusqueda, ignoreCase = true) ||
                                materia.acronimo.contains(textoBusqueda, ignoreCase = true)

                val filterCiclo = currentFilters["ciclo"]?.split(",")?.filter { it.isNotEmpty() }
                val matchCiclo = filterCiclo == null || filterCiclo.contains(materia.cicloNum.toString())

                val filterTurno = currentFilters["turno"]?.split(",")?.filter { it.isNotEmpty() }
                val matchTurno = filterTurno == null || filterTurno.any { it.equals(materia.turno, ignoreCase = true) }

                val filterCurso = currentFilters["curso"]?.split(",")?.filter { it.isNotEmpty() }
                val matchCurso = filterCurso == null || filterCurso.any { it.equals(materia.cursoId.split("_").lastOrNull(), ignoreCase = true) }

                matchTexto && matchCiclo && matchTurno && matchCurso
            }
        }
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
                items(
                    items = asignaturasFiltradas,
                    key = { it.id.ifEmpty { it.idDocumento } }
                ) { materia ->
                    val cursoAcron = materia.cursoId.split("_").lastOrNull()?.uppercase() ?: ""
                    val turnoLetra = if (materia.turno.contains("matutino", ignoreCase = true)) "M" else "V"
                    val badge = "$cursoAcron$turnoLetra${materia.cicloNum}"

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
                    textoBusqueda = textoBusquedaRaw,
                    onValueChange = { textoBusquedaRaw = it },
                    onFilterClick = {
                        val cursosDisponibles = state.asignaturas.map { it.cursoId.split("_").last().uppercase() }.distinct()
                        onOpenDialog(
                            DialogState.Filter(
                                tipo = "Asignatura",
                                currentFilters = currentFilters,
                                opcionesPersonalizadas = mapOf("cursos" to cursosDisponibles),
                                onApply = { currentFilters = it }
                            )
                        )
                    },
                    filters = currentFilters,
                    onRemoveFilter = { key ->
                        if (key.contains(":")) {
                            val (k, v) = key.split(":")
                            currentFilters = currentFilters + (k to v)
                        } else {
                            currentFilters = currentFilters - key
                        }
                    }
                )
            }
        }
    }
}
