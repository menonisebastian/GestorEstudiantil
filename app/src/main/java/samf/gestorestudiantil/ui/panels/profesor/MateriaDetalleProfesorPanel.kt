package samf.gestorestudiantil.ui.panels.profesor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import samf.gestorestudiantil.ui.components.UnidadCard
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MateriaDetalleProfesorPanel(
    asignatura: Asignatura,
    profesor: User,
    onBackClick: () -> Unit,
    onOpenDialog: (DialogState) -> Unit
) {
    val viewModel: ProfesorViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(asignatura.idFirestore) {
        viewModel.cargarUnidadesYPosts(asignatura.idFirestore)
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = textColor
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onOpenDialog(
                        DialogState.AddUnidad(
                            asignaturaId = asignatura.idFirestore,
                            onSave = { nombre, desc, visible ->
                                viewModel.crearUnidad(asignatura.idFirestore, nombre, desc, visible)
                            }
                        )
                    )
                },
                containerColor = primaryColor
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Unidad", tint = textColor)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Text(
                    text = "Contenido de la asignatura",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            if (state.unidades.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text("No hay unidades creadas aún.", color = textColor.copy(alpha = 0.5f))
                    }
                }
            }

            items(state.unidades) { unidad ->
                val postsDeUnidad = state.posts.filter { it.unidadId == unidad.idFirestore }
                UnidadCard(
                    unidad = unidad,
                    posts = postsDeUnidad,
                    onAddPost = {
                        onOpenDialog(
                            DialogState.AddPost(
                                asignaturaId = asignatura.idFirestore,
                                unidadId = unidad.idFirestore,
                                onSave = { titulo, contenido, visible ->
                                    viewModel.crearPost(
                                        asignaturaId = asignatura.idFirestore,
                                        unidadId = unidad.idFirestore,
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
                    onEditUnidad = {
                        onOpenDialog(
                            DialogState.AddUnidad(
                                asignaturaId = asignatura.idFirestore,
                                unidadId = unidad.idFirestore,
                                nombreInicial = unidad.nombre,
                                descripcionInicial = unidad.descripcion,
                                visibleInicial = unidad.visible,
                                onSave = { nombre, desc, visible ->
                                    viewModel.editarUnidad(unidad.idFirestore, nombre, desc, visible)
                                }
                            )
                        )
                    },
                    onDeleteUnidad = {
                        onOpenDialog(
                            DialogState.Confirmation(
                                title = "Eliminar Unidad",
                                content = "¿Estás seguro de que deseas eliminar esta unidad y todo su contenido?",
                                onConfirm = { viewModel.eliminarUnidad(unidad.idFirestore) }
                            )
                        )
                    },
                    onEditPost = { post ->
                        onOpenDialog(
                            DialogState.AddPost(
                                asignaturaId = asignatura.idFirestore,
                                unidadId = unidad.idFirestore,
                                postId = post.idFirestore,
                                tituloInicial = post.titulo,
                                contenidoInicial = post.contenido,
                                visibleInicial = post.visible,
                                onSave = { titulo, contenido, visible ->
                                    viewModel.editarPost(post.idFirestore, titulo, contenido, visible)
                                }
                            )
                        )
                    },
                    onDeletePost = { post ->
                        onOpenDialog(
                            DialogState.Confirmation(
                                title = "Eliminar Publicación",
                                content = "¿Estás seguro de que deseas eliminar esta publicación?",
                                onConfirm = { viewModel.eliminarPost(post.idFirestore) }
                            )
                        )
                    }
                )
            }
        }
    }
}
