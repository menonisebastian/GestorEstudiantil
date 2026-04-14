package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.AccImg
import samf.gestorestudiantil.ui.components.UnidadCard
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.EstudianteViewModel
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel

@Composable
fun MateriaDetalleEstudiantePanel(
    asignatura: Asignatura,
    estudiante: User,
    onBackClick: () -> Unit,
    onOpenDialog: (DialogState) -> Unit
) {
    val viewModel: EstudianteViewModel = hiltViewModel()
    val profesorViewModel: ProfesorViewModel = hiltViewModel()
    val state by profesorViewModel.state.collectAsState()

    LaunchedEffect(asignatura.id) {
        profesorViewModel.cargarContenidoAsignatura(asignatura.id)
        profesorViewModel.cargarProfesor(asignatura.profesorId)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 160.dp)
    ) {
        item {
            val profesor by profesorViewModel.profesor.collectAsState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AccImg(
                        userName = asignatura.profesorNombre,
                        imgUrl = profesor?.imgUrl ?: "",
                        onClick = {
                            profesor?.let { onOpenDialog(DialogState.UserProfile(it)) }
                        },
                        size = 40.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Profesor/a",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                        Text(
                            text = asignatura.profesorNombre.ifEmpty { "Sin asignar" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val titulo = "${asignatura.acronimo} ${asignatura.turno.firstOrNull()?.uppercase() ?: ""}${asignatura.ciclo.take(1)}"
                    Text(
                        text = titulo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = asignatura.nombre,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Normal,
                        lineHeight = 14.sp
                    )
                }
            }
        }

            val unidadesVisibles = state.unidades.filter { it.visible }

            if (unidadesVisibles.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text("No hay contenido disponible todavía.", color = textColor.copy(alpha = 0.5f))
                    }
                }
            }

            items(unidadesVisibles) { unidad ->
                val postsVisibles = state.posts.filter { it.unidadId == unidad.id && it.visible }
                val tareasVisibles = state.tareas.filter { it.unidadId == unidad.id && it.visible }
                UnidadCard(
                    unidad = unidad,
                    posts = postsVisibles,
                    tareas = tareasVisibles,
                    onAddPost = null, // Estudiantes no pueden añadir posts
                    onTareaClick = { tarea ->
                        onOpenDialog(
                            DialogState.TareaDetalleEstudiante(
                                tarea = tarea,
                                estudianteId = estudiante.id,
                                estudianteNombre = estudiante.nombre,
                                onEntregar = { fileData, fileName, mimeType ->
                                    viewModel.realizarEntrega(
                                        samf.gestorestudiantil.data.models.Entrega(
                                            tareaId = tarea.id,
                                            estudianteId = estudiante.id,
                                            estudianteNombre = estudiante.nombre,
                                            profesorId = tarea.profesorId,
                                            asignaturaId = tarea.asignaturaId
                                        ),
                                        fileData,
                                        fileName,
                                        mimeType
                                    )
                                },
                                onEliminarEntrega = {
                                    viewModel.state.value.miEntrega?.let {
                                        onOpenDialog(DialogState.Confirmation(
                                            title = "Eliminar entrega",
                                            content = "¿Estás seguro de que deseas eliminar tu entrega?",
                                            onConfirm = { viewModel.eliminarEntrega(it) }
                                        ))
                                    }
                                }
                            )
                        )
                    }
                )
            }
        }
}
