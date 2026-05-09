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
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.utils.UiText
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.navigation.HomeState
import samf.gestorestudiantil.ui.navigation.Routes
import samf.gestorestudiantil.ui.panels.CalendarioPanel
import samf.gestorestudiantil.ui.panels.estudiante.RecordatoriosEstudiantePanel
import samf.gestorestudiantil.ui.panels.profesor.*
import samf.gestorestudiantil.ui.screens.ProfileScreen
import samf.gestorestudiantil.ui.viewmodels.*

@Composable
fun ProfesorNavContent(
    pageTab: String,
    backStack: List<Any>,
    usuario: User.Profesor,
    profesorState: ProfesorState,
    appState: AppState,
    homeState: HomeState,
    onOpenDialog: (DialogState) -> Unit,
    profesorViewModel: ProfesorViewModel,
    asistenciaViewModel: AsistenciaViewModel,
    appViewModel: AppViewModel,
    isLoading: Boolean,
    onLogout: () -> Unit,
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
                val onAsignaturaClickProfesor = remember(pageTab) {
                    { asignatura: Asignatura ->
                        homeState.navigate(pageTab, Routes.HomeRoutes.MateriaDetalle(asignatura))
                    }
                }
                AsignaturasProfesorPanel(
                    profesor = usuario,
                    paddingValues = PaddingValues(0.dp),
                    onAsignaturaClick = onAsignaturaClickProfesor,
                    onOpenDialog = onOpenDialog,
                    viewModel = profesorViewModel
                )
            }
            entry<Routes.HomeRoutes.MateriaDetalle> { route ->
                MateriaDetalleProfesorPanel(
                    asignatura = route.asignatura,
                    profesor = usuario,
                    onOpenDialog = onOpenDialog,
                    viewModel = profesorViewModel
                )
            }
            entry<Routes.HomeRoutes.Asistencia> { route ->
                AsistenciaPanel(
                    asignatura = route.asignatura,
                    onOpenDialog = onOpenDialog,
                    viewModel = asistenciaViewModel
                )
            }
            entry<Routes.HomeRoutes.Horarios> {
                HorariosProfesorPanel(
                    paddingValues = PaddingValues(0.dp),
                    horarios = profesorState.horarios,
                    asignaturas = profesorState.asignaturas,
                    turno = usuario.turno
                )
            }
            entry<Routes.HomeRoutes.Calendario> {
                CalendarioPanel(
                    usuarioActual = usuario,
                    tareas = profesorState.tareas,
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
                    onDeleteTarea = { tarea ->
                        onOpenDialog(DialogState.Confirmation(
                            title = UiText.DynamicString("Eliminar Tarea"),
                            content = UiText.DynamicString("¿Estás seguro de que deseas eliminar la tarea '${tarea.titulo}'? Esta acción es irreversible."),
                            onConfirm = {
                                profesorViewModel.eliminarTarea(tarea) {
                                    appViewModel.showSnackbar(
                                        message = "Tarea eliminada correctamente",
                                        actionLabel = "Deshacer",
                                        onAction = { profesorViewModel.crearTarea(tarea, null, null, null) }
                                    )
                                }
                            }
                        ))
                    },
                    onDownloadTarea = { tarea ->
                        tarea.adjunto?.let { adjunto ->
                            onOpenDialog(DialogState.AttachmentOptions(
                                supabasePath = adjunto.supabasePath,
                                fileName = adjunto.nombreArchivo,
                                onOpen = { path, name -> profesorViewModel.descargarArchivo(path, name, isDirectDownload = false) },
                                onDownload = { path, name -> profesorViewModel.descargarArchivo(path, name, isDirectDownload = true) }
                            ))
                        }
                    },
                    onTareaClick = { tarea ->
                        val asignatura = profesorViewModel.state.value.asignaturas.find { it.id == tarea.asignaturaId }
                        onOpenDialog(
                            DialogState.AddTarea(
                                asignaturaId = tarea.asignaturaId,
                                unidadId = tarea.unidadId,
                                centroId = tarea.centroId,
                                acronimoAsignatura = asignatura?.acronimo ?: "",
                                profesorId = usuario.id,
                                profesorNombre = usuario.nombre,
                                tareaExistente = tarea,
                                onSave = { }
                            )
                        )
                    }
                )
            }
            entry<Routes.HomeRoutes.Calificaciones> {
                CalificacionesProfesorPanel(
                    paddingValues = PaddingValues(0.dp),
                    onOpenDialog = onOpenDialog,
                    onAsignaturaClick = { asignatura ->
                        homeState.navigate(pageTab, Routes.HomeRoutes.EstudiantesAsignatura(asignatura))
                    },
                    onEstudianteClick = { estudiante, asignatura ->
                        homeState.navigate(pageTab, Routes.HomeRoutes.CalificacionesEstudianteDetalle(estudiante, asignatura))
                    },
                    viewModel = profesorViewModel
                )
            }
            entry<Routes.HomeRoutes.EstudiantesAsignatura> { route ->
                LaunchedEffect(route.asignatura.id) {
                    profesorViewModel.cargarEstudiantesPorAsignatura(route.asignatura)
                }
                val onEstudianteClick = remember(pageTab, route.asignatura) {
                    { estudiante: User ->
                        homeState.navigate(pageTab, Routes.HomeRoutes.CalificacionesEstudianteDetalle(estudiante, route.asignatura))
                    }
                }
                EstudiantesAsignaturaLista(
                    asignatura = route.asignatura,
                    estudiantes = profesorState.estudiantes,
                    onEstudianteClick = onEstudianteClick,
                    onOpenDialog = onOpenDialog,
                )
            }
            entry<Routes.HomeRoutes.CalificacionesEstudianteDetalle> { route ->
                CalificacionesDetalleEstudiante(
                    estudiante = route.estudiante,
                    asignatura = route.asignatura,
                    onOpenDialog = onOpenDialog,
                    viewModel = profesorViewModel
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
