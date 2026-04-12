package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.AccImg
import samf.gestorestudiantil.ui.components.UnidadCard
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.EstudianteViewModel
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MateriaDetalleEstudiantePanel(
    asignatura: Asignatura,
    estudiante: User,
    onBackClick: () -> Unit,
    onOpenDialog: (DialogState) -> Unit
) {
    val viewModel: EstudianteViewModel = viewModel()
    val profesorViewModel: ProfesorViewModel = viewModel()
    val state by profesorViewModel.state.collectAsState()

    LaunchedEffect(asignatura.id) {
        profesorViewModel.cargarContenidoAsignatura(asignatura.id)
        profesorViewModel.cargarProfesor(asignatura.profesorId)
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                modifier = Modifier.height(56.dp),
                title = {
                    Box(modifier = Modifier.fillMaxWidth().padding(end = 16.dp)) {
                        Column(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            horizontalAlignment = Alignment.End
                        ) {
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
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Volver",
                            tint = textColor
                        )
                    }
                },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                val profesor by profesorViewModel.profesor.collectAsState()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    AccImg(
                        imgUrl = profesor?.imgUrl ?: "",
                        onClick = {
                            profesor?.let { onOpenDialog(DialogState.UserProfile(it)) }
                        }
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
}
