package samf.gestorestudiantil.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ListAlt
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.Tarea
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.BottomNavBar
import samf.gestorestudiantil.ui.components.CustomFAB
import samf.gestorestudiantil.ui.components.IconLogo
import samf.gestorestudiantil.ui.components.TitleLogo
import samf.gestorestudiantil.ui.dialogs.DialogOrchestrator
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.navigation.HomeState
import samf.gestorestudiantil.ui.navigation.Routes
import samf.gestorestudiantil.ui.navigation.rememberHomeState
import samf.gestorestudiantil.ui.panels.admin.*
import samf.gestorestudiantil.ui.panels.estudiante.AsignaturasEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.CalificacionesAsignaturaPanel
import samf.gestorestudiantil.ui.panels.estudiante.CalificacionesEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.HorariosEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.MateriaDetalleEstudiantePanel
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Checklist
import samf.gestorestudiantil.ui.panels.estudiante.CalificacionesGlobalesPanel
import samf.gestorestudiantil.ui.panels.estudiante.AsistenciaGlobalEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.RecordatoriosEstudiantePanel
import samf.gestorestudiantil.ui.panels.profesor.AsignaturasProfesorPanel
import samf.gestorestudiantil.ui.panels.profesor.CalificacionesDetalleEstudiante
import samf.gestorestudiantil.ui.panels.profesor.CalificacionesProfesorPanel
import samf.gestorestudiantil.ui.panels.profesor.EstudiantesAsignaturaLista
import samf.gestorestudiantil.ui.panels.profesor.HorariosProfesorPanel
import samf.gestorestudiantil.ui.panels.profesor.MateriaDetalleProfesorPanel
import samf.gestorestudiantil.ui.panels.profesor.AsistenciaPanel
import samf.gestorestudiantil.ui.panels.CalendarioPanel
import samf.gestorestudiantil.ui.viewmodels.AsistenciaViewModel
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AdminState
import samf.gestorestudiantil.ui.viewmodels.AdminViewModel
import samf.gestorestudiantil.ui.viewmodels.AppState
import samf.gestorestudiantil.ui.viewmodels.AppViewModel
import samf.gestorestudiantil.ui.viewmodels.CurrentUserUiState
import samf.gestorestudiantil.ui.viewmodels.EstudianteState
import samf.gestorestudiantil.ui.viewmodels.EstudianteViewModel
import samf.gestorestudiantil.ui.viewmodels.ProfesorState


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
    "Calendario" to Icons.Default.CalendarMonth,
    "Perfil" to Icons.Outlined.Person,
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

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    val currentTab = tabs.getOrNull(pagerState.currentPage) ?: tabs.firstOrNull() ?: ""
    val currentStack = homeState.getStack(currentTab)
    val currentRoute = currentStack?.lastOrNull()

    val canScrollPager = currentStack?.size == 1

    val appViewModel: AppViewModel = hiltViewModel()
    val appState by appViewModel.state.collectAsState()

    val estudianteViewModel: EstudianteViewModel = hiltViewModel()
    val estudianteState by estudianteViewModel.state.collectAsState()

    val profesorViewModel: ProfesorViewModel = hiltViewModel()
    val profesorState by profesorViewModel.state.collectAsState()

    val adminViewModel: AdminViewModel = hiltViewModel()
    val adminState by adminViewModel.adminState.collectAsState()

    val asistenciaViewModel: AsistenciaViewModel = hiltViewModel()


    LaunchedEffect(usuario.id) {
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
    }

    LaunchedEffect(pagerState.currentPage, usuario.id) {
        val activeTab = tabs.getOrNull(pagerState.currentPage)
        when (usuario) {
            is User.Estudiante -> {
                if (usuario.cursoId.isNotEmpty() && usuario.turno.isNotEmpty()) {
                    when (activeTab) {
                        "Asignaturas" -> {
                            estudianteViewModel.cargarAsignaturas(
                                usuario.cursoId,
                                usuario.turno,
                                usuario.cicloNum,
                                usuario.ultimaVezAsignaturas
                            )
                        }
                        "Horarios" -> {
                            estudianteViewModel.cargarHorarios(usuario.cursoId, usuario.turno, usuario.cicloNum)
                        }
                    }
                }
            }
            is User.Profesor -> {
                when (activeTab) {
                    "Asignaturas" -> {
                        profesorViewModel.cargarAsignaturas(usuario.id, usuario.ultimaVezAsignaturas)
                    }
                    "Horarios" -> {
                        profesorViewModel.cargarHorariosProfesor(usuario.id)
                    }
                }
            }
            else -> {}
        }
    }

    LaunchedEffect(usuario.id, usuario.rol) {
        when (usuario) {
            is User.Estudiante -> {
                estudianteViewModel.actualizarTiemposLectura(usuario.ultimaVezAsignaturas)
            }
            is User.Profesor -> {
                profesorViewModel.actualizarTiemposLectura(usuario.ultimaVezAsignaturas)
            }
            else -> {}
        }
    }

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

    val onOpenDialog: (DialogState) -> Unit = remember(dialogStack) {
        { newState: DialogState ->

            val shouldStack = newState is DialogState.Recordatorio || 
                             newState is DialogState.Confirmation
            
            if (!shouldStack) {
                dialogStack.clear()
            }
            dialogStack.add(newState)
        }
    }

    LaunchedEffect(targetAsignaturaId, profesorState.asignaturas, estudianteState.asignaturas) {
        if (targetAsignaturaId == null) return@LaunchedEffect
        
        val asignaturas = if (usuario.rol == "PROFESOR") 
            profesorState.asignaturas else estudianteState.asignaturas
        val asignatura = asignaturas.find { it.id == targetAsignaturaId }

        if (asignatura != null) {
            val tabIndex = tabs.indexOf("Asignaturas")
            if (tabIndex != -1) {
                pagerState.animateScrollToPage(tabIndex)
                homeState.navigate("Asignaturas", Routes.HomeRoutes.MateriaDetalle(asignatura))
                onNotificationHandled()
            }
        }
    }

    val context = LocalContext.current
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
        containerColor = backgroundColor,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val durationMillis = 4000L
                val progress = remember { Animatable(1f) }

                LaunchedEffect(data) {
                    progress.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis.toInt(), easing = LinearEasing)
                    )
                }

                Snackbar(
                    modifier = Modifier.padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    containerColor = surfaceColor,
                    contentColor = textColor,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
                            Text(text = data.visuals.message)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress.value },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp),
                                color = primaryColor,
                                trackColor = primaryColor.copy(alpha = 0.2f),
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }

                        if (data.visuals.actionLabel != null) {
                            TextButton(onClick = { data.performAction() }) {
                                Text(data.visuals.actionLabel!!, color = primaryColor)
                            }
                        }
                        IconButton(onClick = { data.dismiss() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = surfaceDimColor
                            )
                        }
                    }
                }
            }
        },
        topBar = {
            AnimatedContent(
                targetState = currentTab == "Perfil",
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "TopBarAnimation"
            ) { isPerfil ->
                if (isPerfil) {
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
                            if (usuario.rol == "ESTUDIANTE" && currentTab == "Asignaturas" && currentRoute is Routes.HomeRoutes.Materias) {
                                IconButton(
                                    onClick = {
                                        homeState.navigate("Asignaturas", Routes.HomeRoutes.CalificacionesGlobales)
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = surfaceColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ListAlt,
                                        contentDescription = "Calificaciones Globales",
                                        tint = surfaceDimColor
                                    )
                                }
                            }

                            if (usuario.rol == "PROFESOR" && currentTab == "Asignaturas" && currentRoute is Routes.HomeRoutes.MateriaDetalle) {
                                IconButton(
                                    onClick = {
                                        homeState.navigate("Asignaturas", Routes.HomeRoutes.Asistencia(currentRoute.asignatura))
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = surfaceColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Checklist,
                                        contentDescription = "Asistencia",
                                        tint = surfaceDimColor
                                    )
                                }
                            }

                            if (currentTab == "Calendario" && currentRoute !is Routes.HomeRoutes.Recordatorios) {
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

                            if (usuario.rol == "ADMIN" && currentTab != "Calendario" && currentRoute !is Routes.HomeRoutes.Mantenimiento) {
                                IconButton(
                                    onClick = {
                                        homeState.navigate(currentTab, Routes.HomeRoutes.Mantenimiento)
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = surfaceColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = "Mantenimiento",
                                        tint = surfaceDimColor
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            if (usuario.rol == "ESTUDIANTE" && currentTab == "Asignaturas" && currentRoute is Routes.HomeRoutes.Materias) {
                                IconButton(
                                    onClick = {
                                        homeState.navigate("Asignaturas", Routes.HomeRoutes.AsistenciaGlobalEstudiante)
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = surfaceColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Checklist,
                                        contentDescription = "Mi Asistencia Global",
                                        tint = surfaceDimColor
                                    )
                                }
                            } else if (currentStack != null && currentStack.size > 1) {
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
                selectedItem = currentTab,
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
                userImgUrl = usuario.imgUrl,
                userName = usuario.nombre
            )
        },
        floatingActionButton = {
            when {
                currentRoute is Routes.HomeRoutes.Recordatorios -> {
                    CustomFAB(
                        onClick = {
                            onOpenDialog(
                                DialogState.Recordatorio(
                                    initialDate = "",
                                    onSave = { recordatorio ->
                                        appViewModel.agregarRecordatorio(recordatorio.copy(usuarioId = usuario.id))
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

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .consumeWindowInsets(paddingValues),
            userScrollEnabled = canScrollPager
        ) { page ->
            val pageTab = tabs.getOrNull(page) ?: return@HorizontalPager
            val pageBackStack = homeState.getStack(pageTab) ?: return@HorizontalPager

            when (usuario) {
                is User.Estudiante -> {
                    EstudianteNavContent(
                        pageTab = pageTab,
                        backStack = pageBackStack,
                        usuario = usuario,
                        estudianteState = estudianteState,
                        appState = appState,
                        homeState = homeState,
                        onOpenDialog = onOpenDialog,
                        estudianteViewModel = estudianteViewModel,
                        profesorViewModel = profesorViewModel,
                        asistenciaViewModel = asistenciaViewModel,
                        appViewModel = appViewModel,
                        onLogout = onLogout
                    )
                }
                is User.Profesor -> {
                    ProfesorNavContent(
                        pageTab = pageTab,
                        backStack = pageBackStack,
                        usuario = usuario,
                        profesorState = profesorState,
                        appState = appState,
                        homeState = homeState,
                        onOpenDialog = onOpenDialog,
                        profesorViewModel = profesorViewModel,
                        asistenciaViewModel = asistenciaViewModel,
                        appViewModel = appViewModel,
                        onLogout = onLogout
                    )
                }
                is User.Admin -> {
                    AdminNavContent(
                        pageTab = pageTab,
                        backStack = pageBackStack,
                        usuario = usuario,
                        adminState = adminState,
                        appState = appState,
                        homeState = homeState,
                        onOpenDialog = onOpenDialog,
                        adminViewModel = adminViewModel,
                        appViewModel = appViewModel,
                        onLogout = onLogout
                    )
                }
                else -> PlaceholderPanel("Rol no reconocido")
            }
        }
    }

    DialogOrchestrator(
        states = dialogStack.toList(),
        onShowDialog = { dialogStack.add(it) },
        onDismiss = { dialogStack.remove(it) }
    )
}

@Composable
private fun EstudianteNavContent(
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
                                        samf.gestorestudiantil.data.models.Entrega(
                                            tareaId = tarea.id,
                                            estudianteId = usuario.id,
                                            estudianteNombre = usuario.nombre,
                                            profesorId = tarea.profesorId,
                                            asignaturaId = tarea.asignaturaId
                                        ),
                                        data,
                                        name,
                                        mime,
                                        tarea.titulo
                                    )
                                },
                                onEliminarEntrega = {
                                    estudianteViewModel.state.value.miEntrega?.let { entrega ->
                                        onOpenDialog(DialogState.Confirmation(
                                            title = "Eliminar entrega",
                                            content = "¿Estás seguro de que deseas eliminar tu entrega?",
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
                                        samf.gestorestudiantil.data.models.Entrega(
                                            tareaId = tarea.id,
                                            estudianteId = usuario.id,
                                            estudianteNombre = usuario.nombre,
                                            profesorId = tarea.profesorId,
                                            asignaturaId = tarea.asignaturaId
                                        ),
                                        data,
                                        name,
                                        mime,
                                        tarea.titulo
                                    )
                                },
                                onEliminarEntrega = {
                                    estudianteViewModel.state.value.miEntrega?.let { entrega ->
                                        onOpenDialog(DialogState.Confirmation(
                                            title = "Eliminar entrega",
                                            content = "¿Estás seguro de que deseas eliminar tu entrega?",
                                            onConfirm = {
                                                estudianteViewModel.eliminarEntrega(entrega) {
                                                    appViewModel.showSnackbar(
                                                        message = "Entrega eliminada",
                                                        actionLabel = "Deshacer",
                                                        onAction = { 
                                                            // Nota: Reinstaurar una entrega con su archivo requiere el ByteArray original.
                                                            // Si no lo tenemos, al menos informamos o permitimos restaurar metadatos.
                                                            // Para simplificar según el plan, mostramos el Snackbar.
                                                        }
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
                    onLogout = onLogout,
                    onProfileUpdated = { }
                )
            }
        }
    )
}

@Composable
private fun ProfesorNavContent(
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
                            title = "Eliminar Tarea",
                            content = "¿Estás seguro de que deseas eliminar la tarea '${tarea.titulo}'? Esta acción es irreversible.",
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
                        onOpenDialog(
                            DialogState.AddTarea(
                                asignaturaId = tarea.asignaturaId,
                                unidadId = tarea.unidadId,
                                tareaExistente = tarea,
                                onSave = { tareaEditada, fileData, fileName, mimeType ->
                                    profesorViewModel.editarTarea(tareaEditada, fileData, fileName, mimeType)
                                }
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
                    onLogout = onLogout,
                    onProfileUpdated = { }
                )
            }
        }
    )
}

@Composable
private fun AdminNavContent(
    pageTab: String,
    backStack: List<Any>,
    usuario: User.Admin,
    adminState: AdminState,
    appState: AppState,
    homeState: HomeState,
    onOpenDialog: (DialogState) -> Unit,
    adminViewModel: AdminViewModel,
    appViewModel: AppViewModel,
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
                    tareas = emptyList(), // Admin no suele ver tareas académicas
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
                    onLogout = onLogout,
                    onProfileUpdated = { }
                )
            }
        }
    )
}

@Composable
fun PlaceholderPanel(titulo: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = titulo, color = textColor)
    }
}
