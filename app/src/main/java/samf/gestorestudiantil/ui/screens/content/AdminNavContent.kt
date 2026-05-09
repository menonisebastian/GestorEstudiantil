package samf.gestorestudiantil.ui.screens.content

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.navigation.HomeState
import samf.gestorestudiantil.ui.navigation.Routes
import samf.gestorestudiantil.ui.panels.CalendarioPanel
import samf.gestorestudiantil.ui.panels.admin.*
import samf.gestorestudiantil.ui.panels.estudiante.RecordatoriosEstudiantePanel
import samf.gestorestudiantil.ui.screens.ProfileScreen
import samf.gestorestudiantil.ui.viewmodels.AdminState
import samf.gestorestudiantil.ui.viewmodels.AdminViewModel
import samf.gestorestudiantil.ui.viewmodels.AppState
import samf.gestorestudiantil.ui.viewmodels.AppViewModel

@Composable
fun AdminNavContent(
    pageTab: String,
    backStack: List<Any>,
    usuario: User.Admin,
    adminState: AdminState,
    appState: AppState,
    homeState: HomeState,
    onOpenDialog: (DialogState) -> Unit,
    adminViewModel: AdminViewModel,
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
            entry<Routes.HomeRoutes.Usuarios> {
                UsuariosAdminPanel(
                    paddingValues = PaddingValues(0.dp),
                    usuarioActual = usuario,
                    onOpenDialog = onOpenDialog
                )
            }
            entry<Routes.HomeRoutes.Centros> {
                LaunchedEffect(Unit) {
                    adminViewModel.cargarCentros()
                }
                CentrosListScreen(
                    adminState = adminState,
                    adminViewModel = adminViewModel,
                    onCentroClick = { centro: Centro ->
                        adminViewModel.cargarCursosPorCentro(centro.id)
                        homeState.navigate(pageTab, Routes.HomeRoutes.AdminTiposCurso(centro))
                    },
                    onEditCentro = { centro: Centro -> homeState.navigate(pageTab, Routes.HomeRoutes.EditCentro(centro)) }
                )
            }
            entry<Routes.HomeRoutes.AdminTiposCurso> { route ->
                TiposCursoScreen(
                    centro = route.centro,
                    adminState = adminState,
                    onTipoClick = { tipo: String ->
                        homeState.navigate(pageTab, Routes.HomeRoutes.AdminCursos(route.centro.id, tipo))
                    }
                )
            }
            entry<Routes.HomeRoutes.AdminCursos> { route ->
                CursosScreen(
                    tipo = route.tipo,
                    adminState = adminState,
                    onCursoClick = { curso: Curso ->
                        homeState.navigate(pageTab, Routes.HomeRoutes.AdminTurnos(route.centroId, curso))
                    },
                    onEditCurso = { curso: Curso ->
                        homeState.navigate(pageTab, Routes.HomeRoutes.EditCurso(curso, route.centroId))
                    },
                )
            }
            entry<Routes.HomeRoutes.AdminTurnos> { route ->
                TurnosScreen(
                    curso = route.curso,
                    onTurnoClick = { turno: String ->
                        adminViewModel.cargarAsignaturasPorCurso(route.curso.id, turno)
                        homeState.navigate(pageTab, Routes.HomeRoutes.AdminCiclos(route.centroId, route.curso, turno))
                    },
                )
            }
            entry<Routes.HomeRoutes.AdminCiclos> { route ->
                CiclosScreen(
                    curso = route.curso,
                    turno = route.turno,
                    adminState = adminState,
                    onVerHorario = { ciclo: String ->
                        val cicloNum = ciclo.trim().firstOrNull()?.digitToIntOrNull() ?: 1
                        adminViewModel.cargarHorariosPorCursoYCiclo(route.curso.id, cicloNum, route.turno)
                        homeState.navigate(pageTab, Routes.HomeRoutes.AdminHorarios(route.centroId, route.curso, route.turno, ciclo))
                    },
                    onEditAsignatura = { asignatura: Asignatura ->
                        homeState.navigate(pageTab, Routes.HomeRoutes.EditAsignatura(asignatura, route.curso.id, route.centroId))
                    },
                    onAsignaturaClick = { asignatura: Asignatura ->
                        onOpenDialog(DialogState.AsignarProfesor(asignatura))
                    },
                    onAsignarTutor = { claseId, centroId ->
                        onOpenDialog(DialogState.AsignarTutor(claseId, centroId))
                    },
                    onUserClick = { user ->
                        onOpenDialog(DialogState.UserProfile(user))
                    },
                )
            }
            entry<Routes.HomeRoutes.AdminAsignaturas> { route ->
                AsignaturasScreen(
                    ciclo = route.ciclo,
                    adminState = adminState,
                    onAsignaturaClick = { asignatura: Asignatura ->
                        onOpenDialog(DialogState.AsignarProfesor(asignatura))
                    },
                    onEditAsignatura = { asignatura: Asignatura ->
                        homeState.navigate(pageTab, Routes.HomeRoutes.EditAsignatura(asignatura, route.curso.id, route.centroId))
                    },
                )
            }
            entry<Routes.HomeRoutes.AdminHorarios> { route ->
                HorariosAdminScreen(
                    curso = route.curso,
                    ciclo = route.ciclo,
                    turno = route.turno,
                    adminState = adminState,
                    onEditHorario = { horario: Horario ->
                        onOpenDialog(
                            DialogState.EditHorario(
                                horario = horario,
                                asignaturasDisponibles = adminState.asignaturas.filter { it.ciclo == route.ciclo },
                                onSave = { adminViewModel.guardarHorario(it) },
                                onDelete = { h ->
                                    onOpenDialog(DialogState.Confirmation(
                                        title = "Eliminar Horario",
                                        content = "¿Estás seguro de que deseas eliminar este horario?",
                                            onConfirm = {
                                                adminViewModel.eliminarHorario(h) {
                                                    appViewModel.showSnackbar(
                                                        message = "Horario eliminado",
                                                        actionLabel = "Deshacer",
                                                        onAction = { adminViewModel.guardarHorario(h) }
                                                    )
                                                }
                                            }
                                    ))
                                }
                            )
                        )
                    }
                )
            }
            entry<Routes.HomeRoutes.EditCentro> { route ->
                EditCentroPanel(
                    state = DialogState.EditCentro(
                        centro = route.centro,
                        onSave = { adminViewModel.guardarCentro(it) },
                        onDelete = { centro ->
                            onOpenDialog(DialogState.Confirmation(
                                title = "Eliminar Centro",
                                content = "¿Estás seguro de que deseas eliminar el centro '${centro.nombre}'? Esta acción eliminará también sus cursos y asignaturas.",
                                onConfirm = {
                                    adminViewModel.eliminarCentro(centro) {
                                        appViewModel.showSnackbar(
                                            message = "Centro eliminado",
                                            actionLabel = "Deshacer",
                                            onAction = { adminViewModel.guardarCentro(centro) }
                                        )
                                    }
                                }
                            ))
                        }
                    ),
                    onBack = { homeState.pop(pageTab) }
                )
            }
            entry<Routes.HomeRoutes.EditCurso> { route ->
                EditCursoPanel(
                    state = DialogState.EditCurso(
                        curso = route.curso,
                        centroId = route.centroId,
                        onSave = { adminViewModel.guardarCurso(it) },
                        onDelete = { curso ->
                            onOpenDialog(DialogState.Confirmation(
                                title = "Eliminar Curso",
                                content = "¿Estás seguro de que deseas eliminar el curso '${curso.acronimo}'? Se eliminarán todas sus asignaturas y horarios.",
                                onConfirm = {
                                    adminViewModel.eliminarCurso(curso) {
                                        appViewModel.showSnackbar(
                                            message = "Curso eliminado",
                                            actionLabel = "Deshacer",
                                            onAction = { adminViewModel.guardarCurso(curso) }
                                        )
                                    }
                                }
                            ))
                        }
                    ),
                    onBack = { homeState.pop(pageTab) }
                )
            }
            entry<Routes.HomeRoutes.EditAsignatura> { route ->
                EditAsignaturaPanel(
                    state = DialogState.EditAsignatura(
                        asignatura = route.asignatura,
                        cursoId = route.cursoId,
                        centroId = route.centroId,
                        onSave = { adminViewModel.guardarAsignatura(it) },
                        onDelete = { asignatura ->
                            onOpenDialog(DialogState.Confirmation(
                                title = "Eliminar Asignatura",
                                content = "¿Estás seguro de que deseas eliminar la asignatura '${asignatura.acronimo}'?",
                                onConfirm = {
                                    adminViewModel.eliminarAsignatura(asignatura) {
                                        appViewModel.showSnackbar(
                                            message = "Asignatura eliminada",
                                            actionLabel = "Deshacer",
                                            onAction = { adminViewModel.guardarAsignatura(asignatura) }
                                        )
                                    }
                                }
                            ))
                        }
                    ),
                    onBack = { homeState.pop(pageTab) }
                )
            }
            entry<Routes.HomeRoutes.EditUser> { route ->
                LaunchedEffect(route.user.centroId) {
                    if (adminState.cursos.isEmpty()) {
                        adminViewModel.cargarCursosPorCentro(route.user.centroId)
                    }
                }
                EditUserPanel(
                    state = DialogState.EditUser(
                        user = route.user,
                        cursos = adminState.cursos,
                        onSave = { adminViewModel.guardarUsuario(it) }
                    ),
                    onBack = { homeState.pop(pageTab) }
                )
            }
            entry<Routes.HomeRoutes.Mantenimiento> {
                MantenimientoAdminPanel(adminViewModel = adminViewModel)
            }
            entry<Routes.HomeRoutes.Calendario> {
                CalendarioPanel(
                    usuarioActual = usuario,
                    tareas = emptyList(),
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
                    onDeleteTarea = { },
                    onDownloadTarea = { },
                    onTareaClick = { }
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
