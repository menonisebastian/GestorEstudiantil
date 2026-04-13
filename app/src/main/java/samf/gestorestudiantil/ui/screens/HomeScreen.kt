package samf.gestorestudiantil.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.Grading
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Snackbar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.HazeState
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
import samf.gestorestudiantil.domain.isDetailRoute
import samf.gestorestudiantil.domain.tabToRoute
import samf.gestorestudiantil.ui.components.BottomNavBar
import samf.gestorestudiantil.ui.components.IconLogo
import samf.gestorestudiantil.ui.dialogs.DialogOrchestrator
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.navigation.Routes
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
    "Calificaciones" to Icons.AutoMirrored.Filled.Grading,
    "Recordatorios" to Icons.Outlined.Notifications,
    "Perfil" to Icons.Outlined.Person
)

val itemsProfesor: Map<String, ImageVector> = mapOf(
    "Asignaturas" to Icons.Outlined.Class,
    "Horarios" to Icons.Default.Schedule,
    "Calificaciones" to Icons.AutoMirrored.Filled.Grading,
    "Recordatorios" to Icons.Outlined.Notifications,
    "Perfil" to Icons.Outlined.Person
)

val itemsAdmin: Map<String, ImageVector> = mapOf(
    "Usuarios" to Icons.Outlined.Person,
    "Centros" to Icons.Default.Business,
    "Recordatorios" to Icons.Outlined.Notifications,
    "Perfil" to Icons.Outlined.Person
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    usuario: User,
    targetAsignaturaId: String? = null,
    onNotificationHandled: () -> Unit = {},
    onLogout: () -> Unit,
    onNavigateProfile: () -> Unit = {}
) {
    val currentNavItems = remember(usuario.rol) {
        when (usuario.rol) {
            "ADMIN" -> itemsAdmin
            "PROFESOR" -> itemsProfesor
            else -> itemsEstudiante
        }
    }

    val tabs = remember(currentNavItems) { currentNavItems.keys.toList() }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

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
    LaunchedEffect(usuario.id, usuario.cursoId, usuario.turno, usuario.cicloNum) {
        // Cargar usuario en AppViewModel para disparar carga de recordatorios
        appViewModel.setCurrentUser(
            CurrentUserUiState(
                id = usuario.id,
                name = usuario.nombre,
                role = usuario.rol,
                photoUrl = usuario.imgUrl,
                curso = usuario.cursoOArea
            )
        )

        if (usuario.rol == "ESTUDIANTE") {
            if (usuario.cursoId.isNotEmpty() && usuario.turno.isNotEmpty()) {
                estudianteViewModel.cargarAsignaturas(usuario.cursoId, usuario.turno, usuario.cicloNum, usuario.ultimaVezAsignaturas)
                estudianteViewModel.cargarHorarios(usuario.cursoId, usuario.turno, usuario.cicloNum)
            }
        } else if (usuario.rol == "PROFESOR") {
            profesorViewModel.cargarAsignaturas(usuario.id, usuario.ultimaVezAsignaturas)
            profesorViewModel.cargarHorariosProfesor(usuario.id)
        }
    }

    // Sincronizar tiempos de lectura cuando el usuario cambie en tiempo real (vía AppNavigation listener)
    LaunchedEffect(usuario.ultimaVezAsignaturas) {
        if (usuario.rol == "ESTUDIANTE") {
            estudianteViewModel.actualizarTiemposLectura(usuario.ultimaVezAsignaturas)
        } else if (usuario.rol == "PROFESOR") {
            profesorViewModel.actualizarTiemposLectura(usuario.ultimaVezAsignaturas)
        }
    }

    // ✅ Mapa de backstacks locales por cada tab para persistir estado y manejar scroll/FAB
    val tabBackStacks = remember(tabs) {
        tabs.associateWith { tab ->
            mutableStateListOf<NavKey>(tabToRoute(tab, usuario.rol))
        }
    }

    // ✅ Back stack INTERNO de HomeScreen (independiente del de AppNavigation)
    val homeBackStack = remember {
        mutableStateListOf<NavKey>(tabToRoute(tabs.first(), usuario.rol))
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

    // ✅ Manejo de Redirección por Notificación
    LaunchedEffect(targetAsignaturaId, estudianteState.asignaturas, profesorState.asignaturas) {
        if (targetAsignaturaId != null) {
            val asignaturas = if (usuario.rol == "PROFESOR") profesorState.asignaturas else estudianteState.asignaturas
            val asignatura = asignaturas.find { it.id == targetAsignaturaId }
            
            if (asignatura != null) {
                // 1. Identificar la tab de Materias
                val materiasTab = "Asignaturas"
                val index = tabs.indexOf(materiasTab)
                
                if (index != -1) {
                    // 2. Cambiar a esa tab si no estamos en ella
                    if (pagerState.currentPage != index) {
                        pagerState.animateScrollToPage(index)
                    }
                    
                    // 3. Añadir el detalle al backstack de esa tab
                    val pageBackStack = tabBackStacks[materiasTab]
                    if (pageBackStack != null) {
                        // Evitar duplicados si ya estamos ahí
                        val currentTop = pageBackStack.lastOrNull()
                        if (currentTop !is Routes.HomeRoutes.MateriaDetalle || currentTop.asignatura.id != asignatura.id) {
                            pageBackStack.add(Routes.HomeRoutes.MateriaDetalle(asignatura))
                        }
                    }
                    
                    // 4. Notificar que ya se manejó
                    onNotificationHandled()
                }
            }
        }
    }

    val currentPageTab = tabs.getOrNull(pagerState.currentPage) ?: ""
    val currentPageBackStack = tabBackStacks[currentPageTab]
    val currentRoute = currentPageBackStack?.lastOrNull()

    val context = LocalContext.current
    // ✅ Manejo del botón Atrás global
    BackHandler {
        if (currentPageBackStack != null && currentPageBackStack.size > 1) {
            currentPageBackStack.removeLastOrNull()
        } else {
            val currentHomeRoute = homeBackStack.lastOrNull()
            val firstTabRoute = tabToRoute(tabs.first(), usuario.rol)
            if (currentHomeRoute != null && currentHomeRoute::class != firstTabRoute::class) {
                homeBackStack.clear()
                homeBackStack.add(firstTabRoute)
            } else {
                (context as? Activity)?.finish()
            }
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
            if (currentPageTab != "Perfil") {
                CenterAlignedTopAppBar(
                    title = {
                        IconLogo(width = 125.dp)
                    },
                    navigationIcon = {
                        if (currentPageBackStack != null && currentPageBackStack.size > 1) {
                            IconButton(
                                onClick = { currentPageBackStack.removeLastOrNull() },
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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = backgroundColor
                    )
                )
            }
        },
        bottomBar = {
            BottomNavBar(
                items = currentNavItems,
                selectedItem = currentPageTab,
                onItemSelected = { selectedKey ->
                    val index = tabs.indexOf(selectedKey)
                    if (index != -1) {
                        scope.launch { pagerState.animateScrollToPage(index) }
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
                currentPageTab == "Recordatorios" || currentPageTab == "Notificaciones" -> {
                    FloatingActionButton(
                        onClick = {
                            onOpenDialog(
                                DialogState.AddRecordatorio(
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
                                        appViewModel.añadirRecordatorio(nuevo)
                                    }
                                )
                            )
                        },
                        containerColor = primaryColor
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Añadir", tint = textColor)
                    }
                }
                usuario.rol == "ADMIN" && currentPageTab == "Centros" -> {
                    val canAdd = when (currentRoute) {
                        is Routes.HomeRoutes.Centros -> true
                        is Routes.HomeRoutes.AdminCursos -> true
                        is Routes.HomeRoutes.AdminAsignaturas -> true
                        else -> false
                    }
                    if (canAdd) {
                        FloatingActionButton(
                            onClick = {
                                when (currentRoute) {
                                    is Routes.HomeRoutes.Centros -> currentPageBackStack.add(Routes.HomeRoutes.EditCentro())
                                    is Routes.HomeRoutes.AdminCursos -> currentPageBackStack.add(Routes.HomeRoutes.EditCurso(centroId = currentRoute.centroId))
                                    is Routes.HomeRoutes.AdminAsignaturas -> currentPageBackStack.add(Routes.HomeRoutes.EditAsignatura(cursoId = currentRoute.curso.id, centroId = currentRoute.centroId))
                                    else -> {}
                                }
                            },
                            containerColor = primaryColor
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Añadir", tint = textColor)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = if (currentPageTab == "Perfil") 0.dp else paddingValues.calculateTopPadding()),
            userScrollEnabled = currentRoute?.let { !isDetailRoute(it) } ?: true
        ) { page ->
            val pageTab = tabs.getOrNull(page) ?: ""
            val pageBackStack = tabBackStacks[pageTab] ?: return@HorizontalPager

            NavDisplay(
                backStack = pageBackStack,
                onBack = {
                    if (pageBackStack.size > 1) {
                        pageBackStack.removeLastOrNull()
                    }
                },
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    )
                },
                popTransitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                entryProvider = entryProvider {
                    // Estudiante / Profesor
                    entry<Routes.HomeRoutes.Materias> {
                        if (usuario.rol == "PROFESOR") {
                            AsignaturasProfesorPanel(
                                profesor = usuario,
                                paddingValues = PaddingValues(0.dp),
                                onAsignaturaClick = { asignatura ->
                                    pageBackStack.add(Routes.HomeRoutes.MateriaDetalle(asignatura))
                                }
                            )
                        } else {
                            AsignaturasEstudiantePanel(
                                asignaturas = estudianteState.asignaturas,
                                paddingValues = PaddingValues(0.dp),
                                onAsignaturaClick = { asignatura ->
                                    if (usuario.rol == "ESTUDIANTE") {
                                        estudianteViewModel.marcarAsignaturaComoLeida(usuario.id, asignatura.id)
                                    }
                                    pageBackStack.add(Routes.HomeRoutes.MateriaDetalle(asignatura))
                                }
                            )
                        }
                    }
                    entry<Routes.HomeRoutes.MateriaDetalle> { route ->
                        if (usuario.rol == "PROFESOR") {
                            MateriaDetalleProfesorPanel(
                                asignatura = route.asignatura,
                                profesor = usuario,
                                onBackClick = { pageBackStack.removeLastOrNull() },
                                onOpenDialog = onOpenDialog
                            )
                        } else {
                            MateriaDetalleEstudiantePanel(
                                asignatura = route.asignatura,
                                estudiante = usuario,
                                onBackClick = { pageBackStack.removeLastOrNull() },
                                onOpenDialog = onOpenDialog
                            )
                        }
                    }
                    entry<Routes.HomeRoutes.Horarios> {
                        if (usuario.rol == "PROFESOR") {
                            HorariosProfesorPanel(
                                paddingValues = PaddingValues(0.dp),
                                horarios = profesorState.horarios,
                                asignaturas = profesorState.asignaturas,
                                turno = usuario.turno.ifEmpty { "matutino" }
                            )
                        } else {
                            HorariosEstudiantePanel(
                                paddingValues = PaddingValues(0.dp),
                                horarios = estudianteState.horarios,
                                asignaturas = estudianteState.asignaturas,
                                turno = usuario.turno,
                                isLoading = estudianteState.isLoading
                            )
                        }
                    }
                    // HomeScreen.kt — al navegar
                    entry<Routes.HomeRoutes.Calificaciones> {
                        if (usuario.rol == "PROFESOR") {
                            CalificacionesProfesorPanel(
                                profesor = usuario,
                                paddingValues = PaddingValues(0.dp),
                                onOpenDialog = onOpenDialog,
                                onAsignaturaClick = { asignatura ->
                                    pageBackStack.add(Routes.HomeRoutes.EstudiantesAsignatura(asignatura))
                                },
                                onEstudianteClick = { estudiante, asignatura ->
                                    pageBackStack.add(Routes.HomeRoutes.CalificacionesEstudianteDetalle(estudiante, asignatura))
                                }
                            )
                        } else {
                            CalificacionesEstudiantePanel(
                                asignaturas = estudianteState.asignaturas,
                                paddingValues = PaddingValues(0.dp),
                                onAsignaturaClick = { asignatura ->
                                    pageBackStack.add(Routes.HomeRoutes.CalificacionesDetalle(asignatura))
                                }
                            )
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
                                pageBackStack.add(Routes.HomeRoutes.CalificacionesEstudianteDetalle(estudiante, route.asignatura))
                            },
                            onOpenDialog = onOpenDialog,
                            onBack = { pageBackStack.removeLastOrNull() },
                            viewModel = profesorViewModel
                        )
                    }
                    entry<Routes.HomeRoutes.CalificacionesEstudianteDetalle> { route ->
                        val profesorViewModel: ProfesorViewModel = hiltViewModel()
                        CalificacionesDetalleEstudiante(
                            estudiante = route.estudiante,
                            asignatura = route.asignatura,
                            onOpenDialog = onOpenDialog,
                            onBack = { pageBackStack.removeLastOrNull() },
                            viewModel = profesorViewModel
                        )
                    }
                    entry<Routes.HomeRoutes.CalificacionesDetalle> { route ->
                        // Cargar evaluaciones de la asignatura seleccionada
                        LaunchedEffect(route.asignatura.id) {
                            estudianteViewModel.cargarEvaluaciones(route.asignatura.id)
                        }

                        CalificacionesAsignaturaPanel(
                            asignatura = route.asignatura,
                            evaluaciones = estudianteState.evaluaciones,
                            paddingValues = PaddingValues(0.dp),
                            onBackClick = { pageBackStack.removeLastOrNull() },
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
                                pageBackStack.add(Routes.HomeRoutes.AdminTiposCurso(centro))
                            },
                            onEditCentro = { centro: Centro -> pageBackStack.add(Routes.HomeRoutes.EditCentro(centro)) }
                        )
                    }
                    entry<Routes.HomeRoutes.AdminTiposCurso> { route ->
                        TiposCursoScreen(
                            centro = route.centro,
                            adminState = adminState,
                            onTipoClick = { tipo: String ->
                                pageBackStack.add(Routes.HomeRoutes.AdminCursos(route.centro.id, tipo))
                            },
                            onBack = { pageBackStack.removeLastOrNull() }
                        )
                    }
                    entry<Routes.HomeRoutes.AdminCursos> { route ->
                        CursosScreen(
                            tipo = route.tipo,
                            centroId = route.centroId,
                            adminState = adminState,
                            onCursoClick = { curso: Curso ->
                                pageBackStack.add(Routes.HomeRoutes.AdminTurnos(route.centroId, curso))
                            },
                            onEditCurso = { curso: Curso ->
                                pageBackStack.add(Routes.HomeRoutes.EditCurso(curso, route.centroId))
                            },
                            onBack = { pageBackStack.removeLastOrNull() }
                        )
                    }
                    entry<Routes.HomeRoutes.AdminTurnos> { route ->
                        TurnosScreen(
                            curso = route.curso,
                            onTurnoClick = { turno: String ->
                                adminViewModel.cargarAsignaturasPorCurso(route.curso.id, turno)
                                pageBackStack.add(Routes.HomeRoutes.AdminCiclos(route.centroId, route.curso, turno))
                            },
                            onBack = { pageBackStack.removeLastOrNull() }
                        )
                    }
                    entry<Routes.HomeRoutes.AdminCiclos> { route ->
                        CiclosScreen(
                            curso = route.curso,
                            turno = route.turno,
                            adminState = adminState,
                            onVerAsignaturas = { ciclo: String ->
                                pageBackStack.add(Routes.HomeRoutes.AdminAsignaturas(route.centroId, route.curso, route.turno, ciclo))
                            },
                            onVerHorario = { ciclo: String ->
                                val cicloNum = ciclo.trim().firstOrNull()?.digitToIntOrNull() ?: 1
                                adminViewModel.cargarHorariosPorCursoYCiclo(route.curso.id, cicloNum, route.turno)
                                pageBackStack.add(Routes.HomeRoutes.AdminHorarios(route.centroId, route.curso, route.turno, ciclo))
                            },
                            onBack = { pageBackStack.removeLastOrNull() }
                        )
                    }
                    entry<Routes.HomeRoutes.AdminAsignaturas> { route ->
                        AsignaturasScreen(
                            curso = route.curso,
                            ciclo = route.ciclo,
                            adminState = adminState,
                            onAsignaturaClick = { asignatura: Asignatura ->
                                onOpenDialog(DialogState.AsignarProfesor(asignatura))
                            },
                            onEditAsignatura = { asignatura: Asignatura ->
                                pageBackStack.add(Routes.HomeRoutes.EditAsignatura(asignatura, route.curso.id, route.centroId))
                            },
                            onBack = { pageBackStack.removeLastOrNull() }
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
                            },
                            onBack = { pageBackStack.removeLastOrNull() }
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
                            onBack = { pageBackStack.removeLastOrNull() }
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
                            onBack = { pageBackStack.removeLastOrNull() }
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
                            onBack = { pageBackStack.removeLastOrNull() }
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
                            onBack = { pageBackStack.removeLastOrNull() }
                        )
                    }
                    entry<Routes.HomeRoutes.Perfil> {
                        ProfileScreen(
                            usuario = usuario,
                            onBack = {
                                if (pageBackStack.size > 1) {
                                    pageBackStack.removeLastOrNull()
                                }
                            },
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
