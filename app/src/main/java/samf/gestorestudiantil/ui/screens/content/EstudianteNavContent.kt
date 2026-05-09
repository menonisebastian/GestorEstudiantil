package samf.gestorestudiantil.ui.screens.content

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Entrega
import samf.gestorestudiantil.data.models.Tarea
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.utils.UiText
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.navigation.HomeState
import samf.gestorestudiantil.ui.navigation.Routes
import samf.gestorestudiantil.ui.panels.CalendarioPanel
import samf.gestorestudiantil.ui.panels.estudiante.*
import samf.gestorestudiantil.ui.screens.ProfileScreen
import samf.gestorestudiantil.ui.viewmodels.*

@Composable
fun EstudianteNavContent(
    pageTab: String,
    backStack: List<Any>,
    usuario: User.Estudiante,
    estudianteState: EstudianteState,
    appState: AppState,
    homeState: HomeState,
    onOpenDialog: (DialogState) -> Unit,
    estudianteViewModel: EstudianteViewModel,
    profesorViewModel: ProfesorViewModel,
    asistenciaViewModel: AsistenciaViewModel,
    appViewModel: AppViewModel,
    isLoading: Boolean,
    onLogout: () -> Unit
) {
    NavDisplay(
        backStack = backStack,
        onBack = { homeState.pop(pageTab) },
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(150)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(150)
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(150)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(150)
            )
        },
        entryProvider = entryProvider {
            entry<Routes.HomeRoutes.Materias> {
                val onAsignaturaClickEstudiante = remember(pageTab, usuario.id) {
                    { asignatura: Asignatura ->
                        estudianteViewModel.marcarAsignaturaComoLeida(usuario.id, asignatura.id)
                        homeState.navigate(pageTab, Routes.HomeRoutes.MateriaDetalle(asignatura))
                    }
                }
                val onTareaClickEstudiante = remember(usuario.id) {
                    { tarea: Tarea ->
                        estudianteViewModel.cargarMiEntrega(tarea.id, usuario.id)
                        onOpenDialog(
                            DialogState.TareaDetalleEstudiante(
                                tarea = tarea,
                                estudianteId = usuario.id,
                                estudianteNombre = usuario.nombre,
                                onEntregar = { data, name, mime ->
                                    estudianteViewModel.realizarEntrega(
                                        Entrega(
                                            tareaId = tarea.id,
                                            estudianteId = usuario.id,
                                            estudianteNombre = usuario.nombre,
                                            profesorId = tarea.profesorId,
                                            asignaturaId = tarea.asignaturaId
                                        ),
                                        data,
                                        name,
                                        mime,
                                        ""
                                    )
                                },
                                onEliminarEntrega = {
                                    estudianteViewModel.state.value.miEntrega?.let { entrega ->
                                        onOpenDialog(DialogState.Confirmation(
                                            title = UiText.DynamicString("Eliminar entrega"),
                                            content = UiText.DynamicString("¿Estás seguro de que deseas eliminar tu entrega?"),
                                            onConfirm = {
                                                estudianteViewModel.eliminarEntrega(entrega) {
                                                    appViewModel.showSnackbar(
                                                        message = "Entrega eliminada",
                                                        actionLabel = "Deshacer",
                                                        onAction = { }
                                                    )
                                                }
                                            }
                                        ))
                                    }
                                },
                                onAttachmentClick = { path, name ->
                                    onOpenDialog(DialogState.AttachmentOptions(
                                        supabasePath = path,
                                        fileName = name,
                                        onOpen = { p, n -> estudianteViewModel.descargarArchivo(p, n, isDirectDownload = false) },
                                        onDownload = { p, n -> estudianteViewModel.descargarArchivo(p, n, isDirectDownload = true) }
                                    ))
                                }
                            )
                        )
                    }
                }
                AsignaturasEstudiantePanel(
                    asignaturas = estudianteState.asignaturas,
                    tareas = estudianteState.tareas,
                    paddingValues = PaddingValues(0.dp),
                    onAsignaturaClick = onAsignaturaClickEstudiante,
                    onTareaClick = onTareaClickEstudiante,
                    onDownloadTarea = { tarea ->
                        tarea.adjunto?.let { adjunto ->
                            onOpenDialog(DialogState.AttachmentOptions(
                                supabasePath = adjunto.supabasePath,
                                fileName = adjunto.nombreArchivo,
                                onOpen = { path, name -> estudianteViewModel.descargarArchivo(path, name, isDirectDownload = false) },
                                onDownload = { path, name -> estudianteViewModel.descargarArchivo(path, name, isDirectDownload = true) }
                            ))
                        }
                    }
                )
            }
            entry<Routes.HomeRoutes.MateriaDetalle> { route ->
                MateriaDetalleEstudiantePanel(
                    asignatura = route.asignatura,
                    estudiante = usuario,
                    onOpenDialog = onOpenDialog,
                    onVerCalificaciones = { asignatura ->
                        homeState.navigate(pageTab, Routes.HomeRoutes.CalificacionesDetalle(asignatura))
                    },
                    viewModel = estudianteViewModel,
                    profesorViewModel = profesorViewModel
                )
            }
            entry<Routes.HomeRoutes.Horarios> {
                HorariosEstudiantePanel(
                    paddingValues = PaddingValues(0.dp),
                    horarios = estudianteState.horarios,
                    asignaturas = estudianteState.asignaturas,
                    turno = usuario.turno,
                    isLoading = estudianteState.isLoading
                )
            }
            entry<Routes.HomeRoutes.Calendario> {
                CalendarioPanel(
                    usuarioActual = usuario,
                    tareas = estudianteState.tareas,
                    recordatorios = appState.recordatorios,
                    paddingValues = PaddingValues(0.dp),
                    onAddRecordatorio = { fechaSeleccionada ->
                        onOpenDialog(
                            DialogState.Recordatorio(
                                initialDate = fechaSeleccionada,
                                onSave = { recordatorio ->
                                    appViewModel.agregarRecordatorio(recordatorio.copy(usuarioId = usuario.id))
                                }
                            )
                        )
                    },
                    onUpdateRecordatorio = { recordatorio ->
                        onOpenDialog(
                            DialogState.Recordatorio(
                                recordatorioExistente = recordatorio,
                                onSave = { appViewModel.actualizarRecordatorio(it) }
                            )
                        )
                    },
                    onDeleteRecordatorio = { appViewModel.eliminarRecordatorio(it) },
                    onDeleteTarea = { _ ->
                        appViewModel.showSnackbar("Solo los profesores pueden eliminar tareas académicas.")
                    },
                    onDownloadTarea = { tarea ->
                        tarea.adjunto?.let { adjunto ->
                            onOpenDialog(DialogState.AttachmentOptions(
                                supabasePath = adjunto.supabasePath,
                                fileName = adjunto.nombreArchivo,
                                onOpen = { path, name -> estudianteViewModel.descargarArchivo(path, name, isDirectDownload = false) },
                                onDownload = { path, name -> estudianteViewModel.descargarArchivo(path, name, isDirectDownload = true) }
                            ))
                        }
                    },
                    onTareaClick = { tarea ->
                        estudianteViewModel.cargarMiEntrega(tarea.id, usuario.id)
                        onOpenDialog(
                            DialogState.TareaDetalleEstudiante(
                                tarea = tarea,
                                estudianteId = usuario.id,
                                estudianteNombre = usuario.nombre,
                                onEntregar = { data, name, mime ->
                                    estudianteViewModel.realizarEntrega(
                                        Entrega(
                                            tareaId = tarea.id,
                                            estudianteId = usuario.id,
                                            estudianteNombre = usuario.nombre,
                                            profesorId = tarea.profesorId,
                                            asignaturaId = tarea.asignaturaId
                                        ),
                                        data,
                                        name,
                                        mime,
                                        ""
                                    )
                                },
                                onEliminarEntrega = {
                                    estudianteViewModel.state.value.miEntrega?.let { entrega ->
                                        onOpenDialog(DialogState.Confirmation(
                                            title = UiText.DynamicString("Eliminar entrega"),
                                            content = UiText.DynamicString("¿Estás seguro de que deseas eliminar tu entrega?"),
                                            onConfirm = {
                                                estudianteViewModel.eliminarEntrega(entrega) {
                                                    appViewModel.showSnackbar(
                                                        message = "Entrega eliminada",
                                                        actionLabel = "Deshacer",
                                                        onAction = { }
                                                    )
                                                }
                                            }
                                        ))
                                    }
                                },
                                onAttachmentClick = { path, name ->
                                    onOpenDialog(DialogState.AttachmentOptions(
                                        supabasePath = path,
                                        fileName = name,
                                        onOpen = { p, n -> estudianteViewModel.descargarArchivo(p, n, isDirectDownload = false) },
                                        onDownload = { p, n -> estudianteViewModel.descargarArchivo(p, n, isDirectDownload = true) }
                                    ))
                                }
                            )
                        )
                    }
                )
            }
            entry<Routes.HomeRoutes.Calificaciones> {
                CalificacionesEstudiantePanel(
                    asignaturas = estudianteState.asignaturas,
                    paddingValues = PaddingValues(0.dp),
                    onAsignaturaClick = { asignatura ->
                        homeState.navigate(pageTab, Routes.HomeRoutes.CalificacionesDetalle(asignatura))
                    }
                )
            }
            entry<Routes.HomeRoutes.CalificacionesDetalle> { route ->
                LaunchedEffect(route.asignatura.id) {
                    estudianteViewModel.cargarEvaluaciones(route.asignatura.id, usuario.id)
                }
                CalificacionesAsignaturaPanel(
                    asignatura = route.asignatura,
                    evaluaciones = estudianteState.evaluaciones,
                    onOpenDialog = onOpenDialog
                )
            }
            entry<Routes.HomeRoutes.CalificacionesGlobales> {
                LaunchedEffect(Unit) {
                    val ids = estudianteState.asignaturas.map { it.id }
                    estudianteViewModel.cargarTodasLasEvaluaciones(usuario.id, ids)
                }
                CalificacionesGlobalesPanel(
                    asignaturas = estudianteState.asignaturas,
                    evaluaciones = estudianteState.evaluacionesGlobales,
                    paddingValues = PaddingValues(0.dp),
                    onAsignaturaClick = { asignatura ->
                        homeState.navigate("Asignaturas", Routes.HomeRoutes.CalificacionesDetalle(asignatura))
                    }
                )
            }
            entry<Routes.HomeRoutes.AsistenciaGlobalEstudiante> {
                AsistenciaGlobalEstudiantePanel(
                    estudianteId = usuario.id,
                    asignaturas = estudianteState.asignaturas,
                    onOpenDialog = onOpenDialog,
                    paddingValues = PaddingValues(0.dp),
                    viewModel = asistenciaViewModel
                )
            }
            entry<Routes.HomeRoutes.Recordatorios> {
                RecordatoriosEstudiantePanel(
                    recordatorios = appState.recordatorios,
                    paddingValues = PaddingValues(0.dp),
                    onOpenDialog = onOpenDialog,
                    onDelete = { appViewModel.eliminarRecordatorio(it) },
                    onUpdate = { appViewModel.actualizarRecordatorio(it) }
                )
            }
            entry<Routes.HomeRoutes.Perfil> {
                ProfileScreen(
                    usuario = usuario,
                    isLoading = isLoading,
                    onLogout = onLogout,
                    onProfileUpdated = { }
                )
            }
        }
    )
}
