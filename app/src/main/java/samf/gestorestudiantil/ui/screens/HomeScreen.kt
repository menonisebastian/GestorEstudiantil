package samf.gestorestudiantil.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Grading
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.data.models.listaRecordatorios
import samf.gestorestudiantil.domain.isDetailRoute
import samf.gestorestudiantil.domain.routeToTab
import samf.gestorestudiantil.domain.tabToRoute
import samf.gestorestudiantil.ui.components.BottomNavBar
import samf.gestorestudiantil.ui.components.TopBarRow
import samf.gestorestudiantil.ui.dialogs.DialogOrchestrator
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.navigation.Routes
import samf.gestorestudiantil.ui.panels.admin.CentrosAdminPanel
import samf.gestorestudiantil.ui.panels.admin.UsuariosAdminPanel
import samf.gestorestudiantil.ui.screens.admin.EditAsignaturaScreen
import samf.gestorestudiantil.ui.screens.admin.EditCentroScreen
import samf.gestorestudiantil.ui.screens.admin.EditCursoScreen
import samf.gestorestudiantil.ui.screens.admin.EditUserScreen
import samf.gestorestudiantil.ui.panels.estudiante.AsignaturasEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.CalificacionesAsignaturaPanel
import samf.gestorestudiantil.ui.panels.estudiante.CalificacionesEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.HorariosEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.MateriaDetalleEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.RecordatoriosEstudiantePanel
import samf.gestorestudiantil.ui.panels.profesor.AsignaturasProfesorPanel
import samf.gestorestudiantil.ui.panels.profesor.CalificacionesProfesorPanel
import samf.gestorestudiantil.ui.panels.profesor.HorariosProfesorPanel
import samf.gestorestudiantil.ui.panels.profesor.MateriaDetalleProfesorPanel
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AppViewModel
import samf.gestorestudiantil.ui.viewmodels.CurrentUserUiState
import samf.gestorestudiantil.ui.viewmodels.EstudianteViewModel
import java.util.UUID

// 1. Definimos los mapas de navegación según tu requerimiento inicial
val itemsEstudiante: Map<String, ImageVector> = mapOf(
    "Materias" to Icons.Outlined.Class,
    "Horarios" to Icons.Default.Schedule,
    "Calificaciones" to Icons.AutoMirrored.Filled.Grading,
    "Recordatorios" to Icons.Outlined.Notifications
)

val itemsProfesor: Map<String, ImageVector> = mapOf(
    "Materias" to Icons.Outlined.Class,
    "Horarios" to Icons.Default.Schedule,
    "Calificaciones" to Icons.AutoMirrored.Filled.Grading,
    "Recordatorios" to Icons.Outlined.Notifications
)

val itemsAdmin: Map<String, ImageVector> = mapOf(
    "Usuarios" to Icons.Outlined.Person,
    "Centros" to Icons.Default.Business,
    "Recordatorios" to Icons.Outlined.Notifications
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    usuario: User,

    onLogout: () -> Unit,
    onNavigateProfile: () -> Unit = {},
    onNavigateSettings: () -> Unit = {}
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

    val appViewModel: AppViewModel = viewModel()
    val appState by appViewModel.state.collectAsState()

    val estudianteViewModel: EstudianteViewModel = viewModel()
    val estudianteState by estudianteViewModel.state.collectAsState()

    val profesorViewModel: ProfesorViewModel = viewModel()
    val profesorState by profesorViewModel.state.collectAsState()

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
            profesorViewModel.cargarAsignaturas(usuario.id)
            profesorViewModel.cargarHorariosProfesor(usuario.id)
        }
    }

    // Sincronizar tiempos de lectura cuando el usuario cambie en tiempo real (vía AppNavigation listener)
    LaunchedEffect(usuario.ultimaVezAsignaturas) {
        if (usuario.rol == "ESTUDIANTE") {
            estudianteViewModel.actualizarTiemposLectura(usuario.ultimaVezAsignaturas)
        }
    }

    // ✅ Back stack INTERNO de HomeScreen (independiente del de AppNavigation)
    val homeBackStack = remember {
        mutableStateListOf<Any>(tabToRoute(tabs.first(), usuario.rol))
    }

    // ✅ Sincronizar Pager → homeBackStack (cuando el usuario desliza)
    LaunchedEffect(pagerState.currentPage) {
        val newRoute = tabToRoute(tabs[pagerState.currentPage], usuario.rol)
        val currentTop = homeBackStack.lastOrNull()
        // Solo cambiar si el top del stack es una ruta de tab (no un detalle)
        if (currentTop is Routes.HomeRoutes && newRoute::class != currentTop::class &&
            !isDetailRoute(currentTop)) {
            homeBackStack.clear()
            homeBackStack.add(newRoute)
        }
    }

    // ✅ Sincronizar homeBackStack → Pager (cuando la navegación cambia el stack)
    LaunchedEffect(homeBackStack.toList()) {
        val topRoute = homeBackStack.lastOrNull()
        if (topRoute != null && !isDetailRoute(topRoute)) {
            val targetTab = routeToTab(topRoute, usuario.rol)
            val index = tabs.indexOf(targetTab)
            if (index != -1 && index != pagerState.currentPage) {
                pagerState.animateScrollToPage(index)
            }
        }
    }

    val showFab = tabs.getOrNull(pagerState.currentPage)
        .let { it == "Recordatorios" || it == "Notificaciones" }

    var dialogState by remember { mutableStateOf<DialogState>(DialogState.None) }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    TopBarRow(
                        name = usuario.nombre,
                        role = usuario.rol,
                        curso = usuario.cursoOArea,
                        imgUrl = usuario.imgUrl,
                        onNavigateProfile = onNavigateProfile,   // directo, sin if/else
                        onNavigateSettings = onNavigateSettings,
                        onLogout = onLogout
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                items = currentNavItems,
                selectedItem = tabs.getOrNull(pagerState.currentPage) ?: "",
                onItemSelected = { selectedKey ->
                    val index = tabs.indexOf(selectedKey)
                    if (index != -1) {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    }
                }
            )
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        dialogState = DialogState.AddRecordatorio(
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
                    },
                    containerColor = primaryColor
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir", tint = textColor)
                }
            }
        }
    ) { paddingValues ->

        // ✅ HorizontalPager + NavDisplay anidado
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            userScrollEnabled = true
        ) { page ->
            // Cada página tiene su propio NavDisplay para sub-navegación
            val pageRoute = tabToRoute(tabs.getOrNull(page) ?: "", usuario.rol)

            // Back stack local por página (para Calificaciones -> Detalle, etc.)
            val pageBackStack = remember(pageRoute) {
                mutableStateListOf<Any>(pageRoute)
            }

            NavDisplay(
                backStack = pageBackStack,
                onBack = {
                    if (pageBackStack.size > 1) {
                        pageBackStack.removeLastOrNull()
                    }
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
                                        estudianteViewModel.marcarAsignaturaComoLeida(usuario.id, asignatura.idFirestore)
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
                                onOpenDialog = { newState -> dialogState = newState }
                            )
                        } else {
                            MateriaDetalleEstudiantePanel(
                                asignatura = route.asignatura,
                                onBackClick = { pageBackStack.removeLastOrNull() }
                            )
                        }
                    }
                    entry<Routes.HomeRoutes.Horarios> {
                        if (usuario.rol == "PROFESOR") {
                            HorariosProfesorPanel(
                                paddingValues = PaddingValues(0.dp),
                                horarios = profesorState.horarios,
                                asignaturas = profesorState.asignaturas
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
                                onOpenDialog = { newState -> dialogState = newState }
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
                    entry<Routes.HomeRoutes.CalificacionesDetalle> { route ->
                        // Cargar evaluaciones de la asignatura seleccionada
                        LaunchedEffect(route.asignatura.idFirestore) {
                            estudianteViewModel.cargarEvaluaciones(route.asignatura.idFirestore)
                        }

                        CalificacionesAsignaturaPanel(
                            asignatura = route.asignatura,
                            evaluaciones = estudianteState.evaluaciones,
                            paddingValues = PaddingValues(0.dp),
                            onBackClick = { pageBackStack.removeLastOrNull() }
                        )
                    }
                    //TODO cambiar a Firebase
//                    entry<Routes.HomeRoutes.CalificacionesDetalle> { route ->
//                        val asignatura = viewModel.asignaturas
//                            .collectAsState()
//                            .value
//                            .find { it.id == route.asignaturaId }
//
//                        if (asignatura != null) {
//                            CalificacionesAsignaturaPanel(
//                                asignatura = asignatura,
//                                paddingValues = PaddingValues(0.dp),
//                                onBackClick = { pageBackStack.removeLastOrNull() }
//                            )
//                        }
//                    }
                    entry<Routes.HomeRoutes.Recordatorios> {
                        RecordatoriosEstudiantePanel(
                            recordatorios = appState.recordatorios,
                            paddingValues = PaddingValues(0.dp),
                            onOpenDialog = { newState -> dialogState = newState }
                        )
                    }
                    // Admin
                    entry<Routes.HomeRoutes.Usuarios> {
                        UsuariosAdminPanel(
                            paddingValues = PaddingValues(0.dp),
                            usuarioActual = usuario,
                            onOpenDialog = { newState -> dialogState = newState }
                        )
                    }
                    entry<Routes.HomeRoutes.Centros> {
                        CentrosAdminPanel(
                            paddingValues = PaddingValues(0.dp),
                            onOpenDialog = { newState -> dialogState = newState },
                            onNavigate = { route -> pageBackStack.add(route) }
                        )
                    }
                    entry<Routes.HomeRoutes.EditCentro> { route ->
                        val adminViewModel: samf.gestorestudiantil.ui.viewmodels.AdminViewModel = viewModel()
                        EditCentroScreen(
                            state = DialogState.EditCentro(
                                centro = route.centro,
                                onSave = { adminViewModel.guardarCentro(it) }
                            ),
                            onBack = { pageBackStack.removeLastOrNull() }
                        )
                    }
                    entry<Routes.HomeRoutes.EditCurso> { route ->
                        val adminViewModel: samf.gestorestudiantil.ui.viewmodels.AdminViewModel = viewModel()
                        EditCursoScreen(
                            state = DialogState.EditCurso(
                                curso = route.curso,
                                centroId = route.centroId,
                                onSave = { adminViewModel.guardarCurso(it) }
                            ),
                            onBack = { pageBackStack.removeLastOrNull() }
                        )
                    }
                    entry<Routes.HomeRoutes.EditAsignatura> { route ->
                        val adminViewModel: samf.gestorestudiantil.ui.viewmodels.AdminViewModel = viewModel()
                        EditAsignaturaScreen(
                            state = DialogState.EditAsignatura(
                                asignatura = route.asignatura,
                                cursoId = route.cursoId,
                                centroId = route.centroId,
                                onSave = { adminViewModel.guardarAsignatura(it) }
                            ),
                            onBack = { pageBackStack.removeLastOrNull() }
                        )
                    }
                    entry<Routes.HomeRoutes.EditUser> { route ->
                        val adminViewModel: samf.gestorestudiantil.ui.viewmodels.AdminViewModel = viewModel()
                        EditUserScreen(
                            state = DialogState.EditUser(
                                user = route.user,
                                onSave = { adminViewModel.guardarUsuario(it) }
                            ),
                            onBack = { pageBackStack.removeLastOrNull() }
                        )
                    }
                }
            )
        }
    }

    DialogOrchestrator(
        state = dialogState,
        onDismiss = { dialogState = DialogState.None }
    )
}

// Composable de relleno para las pantallas que aún no están creadas
@Composable
fun PlaceholderPanel(titulo: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = titulo, color = textColor)
    }
}