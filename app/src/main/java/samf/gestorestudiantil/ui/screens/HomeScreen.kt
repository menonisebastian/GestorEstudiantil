package samf.gestorestudiantil.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Grading
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.BottomNavBar
import samf.gestorestudiantil.ui.components.CustomFAB
import samf.gestorestudiantil.ui.components.IconLogo
import samf.gestorestudiantil.ui.components.TitleLogo
import samf.gestorestudiantil.ui.dialogs.DialogOrchestrator
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.navigation.Routes
import samf.gestorestudiantil.ui.navigation.rememberHomeState
import samf.gestorestudiantil.ui.panels.admin.*
import samf.gestorestudiantil.ui.panels.estudiante.AsignaturasEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.CalificacionesAsignaturaPanel
import samf.gestorestudiantil.ui.panels.estudiante.CalificacionesEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.HorariosEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.MateriaDetalleEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.RecordatoriosEstudiantePanel
import samf.gestorestudiantil.ui.panels.profesor.AsignaturasProfesorPanel
import samf.gestorestudiantil.ui.panels.profesor.CalificacionesDetalleEstudiante
import samf.gestorestudiantil.ui.panels.profesor.CalificacionesProfesorPanel
import samf.gestorestudiantil.ui.panels.profesor.EstudiantesAsignaturaLista
import samf.gestorestudiantil.ui.panels.profesor.HorariosProfesorPanel
import samf.gestorestudiantil.ui.panels.profesor.MateriaDetalleProfesorPanel
import samf.gestorestudiantil.ui.panels.CalendarioPanel
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AppViewModel
import samf.gestorestudiantil.ui.viewmodels.CurrentUserUiState
import samf.gestorestudiantil.ui.viewmodels.EstudianteViewModel
import java.util.UUID

// 1. Definimos los mapas de navegación según tu requerimiento inicial
val itemsEstudiante: Map<String, ImageVector> = mapOf(
    "Asignaturas" to Icons.Outlined.Class,
    "Horarios" to Icons.Default.Schedule,
    "Calendario" to Icons.Default.CalendarMonth,
    "Perfil" to Icons.Outlined.Person
)

val itemsProfesor: Map<String, ImageVector> = mapOf(
    "Asignaturas" to Icons.Outlined.Class,
    "Horarios" to Icons.Default.Schedule,
    "Calendario" to Icons.Default.CalendarMonth,
    "Calificaciones" to Icons.AutoMirrored.Filled.Grading,
    "Perfil" to Icons.Outlined.Person
)

val itemsAdmin: Map<String, ImageVector> = mapOf(
    "Usuarios" to Icons.Outlined.Person,
    "Centros" to Icons.Default.Business,
    "Perfil" to Icons.Outlined.Person,
    "Recordatorios" to Icons.Outlined.Notifications
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    usuario: User,
    targetAsignaturaId: String? = null,
    onNotificationHandled: () -> Unit = {},
    onLogout: () -> Unit
) {
    val currentNavItems = remember(usuario.rol) {
        when (usuario.rol) {
            "ADMIN" -> itemsAdmin
            "PROFESOR" -> itemsProfesor
            else -> itemsEstudiante
        }
    }

    val tabs = remember(currentNavItems) { currentNavItems.keys.toList() }
    val homeState = rememberHomeState(tabs = tabs, rol = usuario.rol)

    // 1. Agregamos el estado del Pager
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    // 2. Calculamos la pestaña actual basándonos en el Pager
    val currentTab = tabs.getOrNull(pagerState.currentPage) ?: tabs.firstOrNull() ?: ""
    val currentStack = homeState.getStack(currentTab)
    val currentRoute = currentStack?.lastOrNull()

    // 3. LA MAGIA: Solo permitimos swipe si estamos en la raíz de la pestaña
    val canScrollPager = currentStack?.size == 1

    val appViewModel: AppViewModel = hiltViewModel()
    val appState by appViewModel.state.collectAsState()

    val estudianteViewModel: EstudianteViewModel = hiltViewModel()
    val estudianteState by estudianteViewModel.state.collectAsState()

    val profesorViewModel: ProfesorViewModel = hiltViewModel()
    val profesorState by profesorViewModel.state.collectAsState()

    val adminViewModel: samf.gestorestudiantil.ui.viewmodels.AdminViewModel = hiltViewModel()
    val adminState by adminViewModel.adminState.collectAsState()

    val hazeState = rememberHazeState()

    // Cargar datos al entrar o cuando cambien los datos clave del usuario
    LaunchedEffect(usuario) {
        // Cargar usuario en AppViewModel para disparar carga de recordatorios
        appViewModel.setCurrentUser(
            CurrentUserUiState(
                id = usuario.id,
                name = usuario.nombre,
                role = usuario.rol,
                photoUrl = usuario.imgUrl,
                curso = when (usuario) {
                    is User.Estudiante -> usuario.curso
                    else -> ""
                }
            )
        )

        when (usuario) {
            is User.Estudiante -> {
                if (usuario.cursoId.isNotEmpty() && usuario.turno.isNotEmpty()) {
                    estudianteViewModel.cargarAsignaturas(
                        usuario.cursoId,
                        usuario.turno,
                        usuario.cicloNum,
                        emptyMap() // Se sincroniza en el siguiente effect
                    )
                    estudianteViewModel.cargarHorarios(usuario.cursoId, usuario.turno, usuario.cicloNum)
                }
            }
            is User.Profesor -> {
                profesorViewModel.cargarAsignaturas(usuario.id, usuario.ultimaVezAsignaturas)
                profesorViewModel.cargarHorariosProfesor(usuario.id)
            }
            else -> {}
        }
    }

    // Sincronizar tiempos de lectura cuando el usuario cambie en tiempo real (vía AppNavigation listener)
    LaunchedEffect(usuario) {
        when (usuario) {
            is User.Estudiante -> {
                estudianteViewModel.actualizarTiemposLectura(emptyMap()) // Ajustar si es necesario
            }
            is User.Profesor -> {
                profesorViewModel.actualizarTiemposLectura(usuario.ultimaVezAsignaturas)
            }
            else -> {}
        }
    }

    // Gestión de diálogos (Pila para permitir pickers sobre otros diálogos)
    val dialogStack = remember { mutableStateListOf<DialogState>() }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        appViewModel.snackbarEvents.collectLatest { event ->
            val result = snackbarHostState.showSnackbar(
                message = event.message,
                actionLabel = event.actionLabel,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                event.onAction?.invoke()
            }
        }
    }

    val onOpenDialog: (DialogState) -> Unit = { newState ->
        dialogStack.clear()
        dialogStack.add(newState)
    }

    // ✅ Manejo de Redirección por Notificación Actualizado
    LaunchedEffect(targetAsignaturaId, estudianteState.asignaturas, profesorState.asignaturas) {
        if (targetAsignaturaId != null) {
            val asignaturas = if (usuario.rol == "PROFESOR") profesorState.asignaturas else estudianteState.asignaturas
            val asignatura = asignaturas.find { it.id == targetAsignaturaId }

            if (asignatura != null) {
                val tabIndex = tabs.indexOf("Asignaturas")
                if (tabIndex != -1) {
                    // Nos movemos al tab y agregamos la ruta
                    pagerState.animateScrollToPage(tabIndex)
                    homeState.navigate("Asignaturas", Routes.HomeRoutes.MateriaDetalle(asignatura))
                    onNotificationHandled()
                }
            }
        }
    }

    val context = LocalContext.current
    // ✅ Manejo del botón Atrás global Actualizado
    BackHandler {
        if (currentStack != null && currentStack.size > 1) {
            homeState.pop(currentTab)
        } else if (pagerState.currentPage != 0) {
            scope.launch { pagerState.animateScrollToPage(0) }
        } else {
            (context as? ComponentActivity)?.moveTaskToBack(true)
        }
    }

    Scaffold(
        modifier = Modifier.hazeSource(hazeState),
        containerColor = backgroundColor,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = surfaceColor,
                    contentColor = textColor,
                    actionColor = primaryColor
                )
            }
        },
        topBar = {
            AnimatedContent(
                targetState = currentTab, // <-- Usamos currentTab derivado del pager
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                },
                label = "TopBarAnimation"
            ) { targetTab ->
                if (targetTab == "Perfil") {
                    CenterAlignedTopAppBar(
                        title = {
                            TitleLogo(125.dp)
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = backgroundColor,
                        )
                    )
                } else {
                    CenterAlignedTopAppBar(
                        title = {
                            IconLogo(width = 125.dp)
                        },
                        actions = {
                            if (usuario.rol != "ADMIN" && targetTab == "Calendario" && currentRoute !is Routes.HomeRoutes.Recordatorios) {
                                IconButton(
                                    onClick = {
                                        homeState.navigate("Calendario", Routes.HomeRoutes.Recordatorios)
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = surfaceColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Recordatorios",
                                        tint = surfaceDimColor
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            if (currentStack != null && currentStack.size > 1) {
                                IconButton(
                                    onClick = { homeState.pop(currentTab) },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = surfaceColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBackIosNew,
                                        contentDescription = "Regresar",
                                        tint = surfaceDimColor
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = backgroundColor,
                        )
                    )
                }
            }
        },
        bottomBar = {
            BottomNavBar(
                items = currentNavItems,
                selectedItem = currentTab, // <-- Usamos currentTab derivado del pager
                onItemSelected = { selectedKey ->
                    val index = tabs.indexOf(selectedKey)
                    if (index != -1) {
                        scope.launch {
                            if (pagerState.currentPage == index) {
                                // Si ya estamos en esta pestaña, volvemos a su raíz
                                homeState.popToRoot(selectedKey)
                            } else {
                                // Si es una pestaña nueva, deslizamos hacia ella
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    }
                },
                hazeState = hazeState,
                userImgUrl = usuario.imgUrl,
                userName = usuario.nombre
            )
        },
        floatingActionButton = {
            // ✅ FAB Centralizado y Contextual
            when {
                (currentRoute is Routes.HomeRoutes.Recordatorios || (currentTab == "Recordatorios" && usuario.rol == "ADMIN")) -> {
                    CustomFAB(
                        onClick = {
                            onOpenDialog(
                                DialogState.AddRecordatorio(
                                    initialDate = "",
                                    onSave = { titulo, descripcion, fecha, hora, tipo ->
                                        val nuevo = Recordatorio(
                                            id = UUID.randomUUID().toString(),
                                            usuarioId = usuario.id,
                                            titulo = titulo,
                                            descripcion = descripcion,
                                            fecha = fecha,
                                            hora = hora,
                                            tipo = tipo
                                        )
                                        appViewModel.agregarRecordatorio(nuevo)
                                    }
                                )
                            )
                        },
                        text = "Añadir Recordatorio"
                    )
                }
                usuario.rol == "ADMIN" && currentTab == "Centros" -> {
                    val canAdd = when (currentRoute) {
                        is Routes.HomeRoutes.Centros -> true
                        is Routes.HomeRoutes.AdminCursos -> true
                        is Routes.HomeRoutes.AdminAsignaturas -> true
                        else -> false
                    }
                    if (canAdd) {
                        CustomFAB(
                            onClick = {
                                when (currentRoute) {
                                    is Routes.HomeRoutes.Centros -> homeState.navigate(currentTab, Routes.HomeRoutes.EditCentro())
                                    is Routes.HomeRoutes.AdminCursos -> homeState.navigate(currentTab, Routes.HomeRoutes.EditCurso(centroId = currentRoute.centroId))
                                    is Routes.HomeRoutes.AdminAsignaturas -> homeState.navigate(currentTab, Routes.HomeRoutes.EditAsignatura(cursoId = currentRoute.curso.id, centroId = currentRoute.centroId))
                                    else -> {}
                                }
                            },
                            text = "Añadir"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        // REEMPLAZAMOS CROSSFADE POR HORIZONTAL PAGER
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .consumeWindowInsets(paddingValues),
            userScrollEnabled = canScrollPager // <-- Se desactiva si hay una pantalla hija abierta
        ) { page ->
            val pageTab = tabs.getOrNull(page) ?: return@HorizontalPager
            val pageBackStack = homeState.getStack(pageTab) ?: return@HorizontalPager

            NavDisplay(
                backStack = pageBackStack,
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
                    // Estudiante / Profesor
                    entry<Routes.HomeRoutes.Materias> {
                        when (usuario) {
                            is User.Profesor -> {
                                AsignaturasProfesorPanel(
                                    profesor = usuario,
                                    paddingValues = PaddingValues(0.dp),
                                    onAsignaturaClick = { asignatura ->
                                        homeState.navigate(pageTab, Routes.HomeRoutes.MateriaDetalle(asignatura))
                                    }
                                )
                            }
                            is User.Estudiante -> {
                                AsignaturasEstudiantePanel(
                                    asignaturas = estudianteState.asignaturas,
                                    paddingValues = PaddingValues(0.dp),
                                    onAsignaturaClick = { asignatura ->
                                        estudianteViewModel.marcarAsignaturaComoLeida(usuario.id, asignatura.id)
                                        homeState.navigate(pageTab, Routes.HomeRoutes.MateriaDetalle(asignatura))
                                    }
                                )
                            }
                            else -> {
                                // Admin o Incompleto no suelen ver esta pestaña o ven un placeholder
                                PlaceholderPanel("No disponible para este rol")
                            }
                        }
                    }
                    entry<Routes.HomeRoutes.MateriaDetalle> { route ->
                        when (usuario) {
                            is User.Profesor -> {
                                MateriaDetalleProfesorPanel(
                                    asignatura = route.asignatura,
                                    profesor = usuario,
                                    onOpenDialog = onOpenDialog
                                )
                            }
                            is User.Estudiante -> {
                                MateriaDetalleEstudiantePanel(
                                    asignatura = route.asignatura,
                                    estudiante = usuario,
                                    onBackClick = { homeState.pop(pageTab) },
                                    onOpenDialog = onOpenDialog,
                                    onVerCalificaciones = { asignatura ->
                                        homeState.navigate(pageTab, Routes.HomeRoutes.CalificacionesDetalle(asignatura))
                                    }
                                )
                            }
                            else -> homeState.pop(pageTab)
                        }
                    }
                    entry<Routes.HomeRoutes.Horarios> {
                        when (usuario) {
                            is User.Profesor -> {
                                HorariosProfesorPanel(
                                    paddingValues = PaddingValues(0.dp),
                                    horarios = profesorState.horarios,
                                    asignaturas = profesorState.asignaturas,
                                    turno = usuario.turno // Ahora usa el turno real del profesor
                                )
                            }
                            is User.Estudiante -> {
                                HorariosEstudiantePanel(
                                    paddingValues = PaddingValues(0.dp),
                                    horarios = estudianteState.horarios,
                                    asignaturas = estudianteState.asignaturas,
                                    turno = usuario.turno,
                                    isLoading = estudianteState.isLoading
                                )
                            }
                            else -> PlaceholderPanel("Horarios no disponibles")
                        }
                    }
                    entry<Routes.HomeRoutes.Calendario> {
                        val tareas = if (usuario.rol == "PROFESOR") {
                            profesorState.tareas
                        } else {
                            estudianteState.tareas
                        }

                        CalendarioPanel(
                            usuarioActual = usuario,
                            tareas = tareas,
                            recordatorios = appState.recordatorios,
                            paddingValues = PaddingValues(0.dp),
                            onAddRecordatorio = { fechaSeleccionada ->
                                onOpenDialog(
                                    DialogState.AddRecordatorio(
                                        initialDate = fechaSeleccionada,
                                        onSave = { titulo, descripcion, fecha, hora, tipo ->
                                            val nuevo = Recordatorio(
                                                id = UUID.randomUUID().toString(),
                                                usuarioId = usuario.id,
                                                titulo = titulo,
                                                descripcion = descripcion,
                                                fecha = fecha,
                                                hora = hora,
                                                tipo = tipo
                                            )
                                            appViewModel.agregarRecordatorio(nuevo)
                                        }
                                    )
                                )
                            },
                            onDeleteRecordatorio = { appViewModel.eliminarRecordatorio(it) },
                            onDeleteTarea = { tarea ->
                                if (usuario.rol == "PROFESOR") {
                                    onOpenDialog(DialogState.Confirmation(
                                        title = "Eliminar Tarea",
                                        content = "¿Estás seguro de que deseas eliminar la tarea '${tarea.titulo}'? Esta acción es irreversible.",
                                        onConfirm = { profesorViewModel.eliminarTarea(tarea) }
                                    ))
                                } else {
                                    appViewModel.showSnackbar("Solo los profesores pueden eliminar tareas académicas.")
                                }
                            }
                        )
                    }
                    // HomeScreen.kt — al navegar
                    entry<Routes.HomeRoutes.Calificaciones> {
                        when (usuario) {
                            is User.Profesor -> {
                                CalificacionesProfesorPanel(
                                    profesor = usuario,
                                    paddingValues = PaddingValues(0.dp),
                                    onOpenDialog = onOpenDialog,
                                    onAsignaturaClick = { asignatura ->
                                        homeState.navigate(pageTab, Routes.HomeRoutes.EstudiantesAsignatura(asignatura))
                                    },
                                    onEstudianteClick = { estudiante, asignatura ->
                                        homeState.navigate(pageTab, Routes.HomeRoutes.CalificacionesEstudianteDetalle(estudiante, asignatura))
                                    }
                                )
                            }
                            is User.Estudiante -> {
                                CalificacionesEstudiantePanel(
                                    asignaturas = estudianteState.asignaturas,
                                    paddingValues = PaddingValues(0.dp),
                                    onAsignaturaClick = { asignatura ->
                                        homeState.navigate(pageTab, Routes.HomeRoutes.CalificacionesDetalle(asignatura))
                                    }
                                )
                            }
                            else -> PlaceholderPanel("Calificaciones no disponibles")
                        }
                    }
                    entry<Routes.HomeRoutes.EstudiantesAsignatura> { route ->
                        val profesorViewModel: ProfesorViewModel = hiltViewModel()
                        val pState by profesorViewModel.state.collectAsState()

                        LaunchedEffect(route.asignatura.id) {
                            profesorViewModel.cargarEstudiantesPorAsignatura(route.asignatura)
                        }

                        EstudiantesAsignaturaLista(
                            asignatura = route.asignatura,
                            estudiantes = pState.estudiantes,
                            onEstudianteClick = { estudiante ->
                                homeState.navigate(pageTab, Routes.HomeRoutes.CalificacionesEstudianteDetalle(estudiante, route.asignatura))
                            },
                            onOpenDialog = onOpenDialog,
                        )
                    }
                    entry<Routes.HomeRoutes.CalificacionesEstudianteDetalle> { route ->
                        val profesorViewModel: ProfesorViewModel = hiltViewModel()
                        CalificacionesDetalleEstudiante(
                            estudiante = route.estudiante,
                            asignatura = route.asignatura,
                            onOpenDialog = onOpenDialog,
                            viewModel = profesorViewModel
                        )
                    }
                    entry<Routes.HomeRoutes.CalificacionesDetalle> { route ->
                        // Cargar evaluaciones de la asignatura seleccionada para el estudiante actual
                        LaunchedEffect(route.asignatura.id) {
                            estudianteViewModel.cargarEvaluaciones(route.asignatura.id, usuario.id)
                        }

                        CalificacionesAsignaturaPanel(
                            asignatura = route.asignatura,
                            evaluaciones = estudianteState.evaluaciones,
                            paddingValues = PaddingValues(0.dp),
                            onBackClick = { homeState.pop(pageTab) },
                            onOpenDialog = onOpenDialog
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
                    // Admin
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
                                                    adminViewModel.eliminarHorario(h.id)
                                                    appViewModel.showSnackbar(
                                                        message = "Horario eliminado",
                                                        actionLabel = "Deshacer",
                                                        onAction = { adminViewModel.guardarHorario(h) }
                                                    )
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
                                            adminViewModel.eliminarCentro(centro)
                                            appViewModel.showSnackbar(
                                                message = "Centro eliminado",
                                                actionLabel = "Deshacer",
                                                onAction = { adminViewModel.guardarCentro(centro) }
                                            )
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
                                            adminViewModel.eliminarCurso(curso)
                                            appViewModel.showSnackbar(
                                                message = "Curso eliminado",
                                                actionLabel = "Deshacer",
                                                onAction = { adminViewModel.guardarCurso(curso) }
                                            )
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
                                            adminViewModel.eliminarAsignatura(asignatura)
                                            appViewModel.showSnackbar(
                                                message = "Asignatura eliminada",
                                                actionLabel = "Deshacer",
                                                onAction = { adminViewModel.guardarAsignatura(asignatura) }
                                            )
                                        }
                                    ))
                                }
                            ),
                            onBack = { homeState.pop(pageTab) }
                        )
                    }
                    entry<Routes.HomeRoutes.EditUser> { route ->
                        val adminState by adminViewModel.adminState.collectAsState()

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
                    entry<Routes.HomeRoutes.Perfil> {
                        ProfileScreen(
                            usuario = usuario,
                            onLogout = onLogout,
                            onProfileUpdated = { }
                        )
                    }
                }
            )
        }
    }

    DialogOrchestrator(
        states = dialogStack,
        onShowDialog = { dialogStack.add(it) },
        onDismiss = { dialogStack.remove(it) }
    )
}

// Composable de relleno para las pantallas que aún no están creadas
@Composable
fun PlaceholderPanel(titulo: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = titulo, color = textColor)
    }
}
