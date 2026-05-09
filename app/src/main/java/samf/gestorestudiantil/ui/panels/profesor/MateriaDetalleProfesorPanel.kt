package samf.gestorestudiantil.ui.panels.profesor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Tarea
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.utils.UiText
import samf.gestorestudiantil.ui.components.AccImg
import samf.gestorestudiantil.ui.components.CustomFAB
import samf.gestorestudiantil.ui.components.UnidadCard
import samf.gestorestudiantil.ui.dialogs.DialogState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AppViewModel
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel

@Composable
fun MateriaDetalleProfesorPanel(
    asignatura: Asignatura,
    profesor: User,
    onOpenDialog: (DialogState) -> Unit,
    appViewModel: AppViewModel = hiltViewModel(),
    viewModel: ProfesorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val showScrollToTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 300
        }
    }

    LaunchedEffect(asignatura.id) {
        viewModel.cargarContenidoAsignatura(asignatura.id)
        viewModel.marcarAsignaturaComoLeida(profesor.id, asignatura.id)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 160.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AccImg(
                            userName = profesor.nombre,
                            imgUrl = profesor.imgUrl,
                            onClick = {
                                onOpenDialog(DialogState.UserProfile(profesor))
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
                                text = profesor.nombre,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                maxLines = 1
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.widthIn(max = 150.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        val titulo = "${asignatura.acronimo} ${asignatura.turno.firstOrNull()?.uppercase() ?: ""}${asignatura.ciclo.take(1)}"
                        Text(
                            text = titulo,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = asignatura.nombre,
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Normal,
                            lineHeight = 14.sp,
                            textAlign = TextAlign.End,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (state.unidades.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text("No hay unidades creadas aún.", color = textColor.copy(alpha = 0.5f))
                    }
                }
            }

            items(state.unidades) { unidad ->
                val postsDeUnidad = state.posts.filter { it.unidadId == unidad.id }
                val tareasDeUnidad = state.tareas.filter { it.unidadId == unidad.id }
                UnidadCard(
                    unidad = unidad,
                    posts = postsDeUnidad,
                    tareas = tareasDeUnidad,
                    onAddPost = {
                        onOpenDialog(
                            DialogState.AddPost(
                                asignaturaId = asignatura.id,
                                unidadId = unidad.id,
                                onSave = { titulo, contenido, visible ->
                                    viewModel.crearPost(
                                        asignaturaId = asignatura.id,
                                        unidadId = unidad.id,
                                        titulo = titulo,
                                        contenido = contenido,
                                        autorId = profesor.id,
                                        autorNombre = profesor.nombre,
                                        visible = visible
                                    )
                                }
                            )
                        )
                    },
                    onAddTarea = {
                        onOpenDialog(
                            DialogState.AddTarea(
                                asignaturaId = asignatura.id,
                                unidadId = unidad.id,
                                acronimoAsignatura = asignatura.acronimo,
                                profesorId = profesor.id,
                                profesorNombre = profesor.nombre,
                                tareaExistente = Tarea(
                                    profesorId = profesor.id,
                                    centroId = asignatura.centroId
                                ),
                                onSave = { }
                            )
                        )
                    },
                    onEditUnidad = {
                        onOpenDialog(
                            DialogState.AddUnidad(
                                asignaturaId = asignatura.id,
                                unidadId = unidad.id,
                                nombreInicial = unidad.nombre,
                                descripcionInicial = unidad.descripcion,
                                visibleInicial = unidad.visible,
                                ordenInicial = unidad.orden,
                                onSave = { nombre, desc, visible, orden ->
                                    viewModel.editarUnidad(unidad.id, nombre, desc, visible, orden)
                                }
                            )
                        )
                    },
                    onDeleteUnidad = {
                        onOpenDialog(
                            DialogState.Confirmation(
                                title = UiText.DynamicString("Eliminar Unidad"),
                                content = UiText.DynamicString("¿Estás seguro de que deseas eliminar esta unidad y todo su contenido?"),
                                onConfirm = {
                                    viewModel.eliminarUnidad(unidad) {
                                        appViewModel.showSnackbar(
                                            message = "Unidad eliminada",
                                            actionLabel = "Deshacer",
                                            onAction = {
                                                viewModel.restaurarUnidad(unidad.id)
                                            }
                                        )
                                    }
                                }
                            )
                        )
                    },
                    onEditPost = { post ->
                        onOpenDialog(
                            DialogState.AddPost(
                                asignaturaId = asignatura.id,
                                unidadId = unidad.id,
                                postId = post.id,
                                tituloInicial = post.titulo,
                                contenidoInicial = post.contenido,
                                visibleInicial = post.visible,
                                onSave = { titulo, contenido, visible ->
                                    viewModel.editarPost(post.id, titulo, contenido, visible)
                                }
                            )
                        )
                    },
                    onDeletePost = { post ->
                        onOpenDialog(
                            DialogState.Confirmation(
                                title = UiText.DynamicString("Eliminar Publicación"),
                                content = UiText.DynamicString("¿Estás seguro de que deseas eliminar esta publicación?"),
                                onConfirm = {
                                    viewModel.eliminarPost(post) {
                                        appViewModel.showSnackbar(
                                            message = "Publicación eliminada",
                                            actionLabel = "Deshacer",
                                            onAction = {
                                                viewModel.restaurarPost(post.id)
                                            }
                                        )
                                    }
                                }
                            )
                        )
                    },
                    onEditTarea = { tarea ->
                        onOpenDialog(
                            DialogState.AddTarea(
                                asignaturaId = asignatura.id,
                                unidadId = unidad.id,
                                acronimoAsignatura = asignatura.acronimo,
                                profesorId = profesor.id,
                                profesorNombre = profesor.nombre,
                                tareaExistente = tarea,
                                onSave = { }
                            )
                        )
                    },
                    onDeleteTarea = { tarea ->
                        onOpenDialog(
                            DialogState.Confirmation(
                                title = UiText.DynamicString("Eliminar Tarea"),
                                content = UiText.DynamicString("¿Estás seguro de que deseas eliminar esta tarea y todas sus entregas?"),
                                onConfirm = {
                                    viewModel.eliminarTarea(tarea) {
                                        appViewModel.showSnackbar(
                                            message = "Tarea eliminada",
                                            actionLabel = "Deshacer",
                                            onAction = {
                                                viewModel.restaurarTarea(tarea.id)
                                            }
                                        )
                                    }
                                }
                            )
                        )
                    },
                    onTareaClick = { tarea ->
                        onOpenDialog(
                            DialogState.VerEntregasProfesor(
                                tarea = tarea,
                                onCalificar = { evaluacion ->
                                    viewModel.calificarEntrega(evaluacion.id, evaluacion.nota.toFloat(), evaluacion.comentario)
                                    viewModel.guardarEvaluacion(evaluacion)
                                }
                            )
                        )
                    },
                    onAttachmentClick = { path, name ->
                        onOpenDialog(DialogState.AttachmentOptions(
                            supabasePath = path,
                            fileName = name,
                            onOpen = { p, n -> viewModel.descargarArchivo(p, n, isDirectDownload = false) },
                            onDownload = { p, n -> viewModel.descargarArchivo(p, n, isDirectDownload = true) }
                        ))
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 103.dp)
        ) {
            Column {
                AnimatedVisibility(
                    visible = showScrollToTop,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                lazyListState.animateScrollToItem(0)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.ArrowDropUp, contentDescription = "Ir arriba")
                    }
                }
            }
        }

        CustomFAB(
            onClick = {
                onOpenDialog(
                    DialogState.AddUnidad(
                        asignaturaId = asignatura.id,
                        ordenInicial = (state.unidades.maxOfOrNull { it.orden } ?: 0) + 1,
                        onSave = { nombre, desc, visible, orden ->
                            viewModel.crearUnidad(asignatura.id, nombre, desc, visible, orden)
                        }
                    )
                )
            },
            text = "Añadir Unidad",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 16.dp)
        )
    }
}
