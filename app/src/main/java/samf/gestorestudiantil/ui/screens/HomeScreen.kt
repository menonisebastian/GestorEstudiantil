package samf.gestorestudiantil.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.BottomNavBar
import samf.gestorestudiantil.ui.components.CustomFAB
import samf.gestorestudiantil.ui.components.IconLogo
import samf.gestorestudiantil.ui.components.TitleLogo
import samf.gestorestudiantil.ui.dialogs.DialogOrchestrator
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.navigation.Routes
import samf.gestorestudiantil.ui.navigation.rememberHomeState
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Checklist
import samf.gestorestudiantil.ui.viewmodels.AsistenciaViewModel
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.screens.content.*
import samf.gestorestudiantil.ui.viewmodels.AdminViewModel
import samf.gestorestudiantil.ui.viewmodels.AppViewModel
import samf.gestorestudiantil.ui.viewmodels.CurrentUserUiState
import samf.gestorestudiantil.ui.viewmodels.EstudianteViewModel
import samf.gestorestudiantil.ui.navigation.itemsProfesor
import samf.gestorestudiantil.ui.navigation.itemsAdmin
import samf.gestorestudiantil.ui.navigation.itemsEstudiante

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    usuario: User,
    isLoading: Boolean = false,
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

    LaunchedEffect(usuario) {
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

    LaunchedEffect(appState.pendingNotificationData, profesorState.asignaturas, estudianteState.asignaturas) {
        val data = appState.pendingNotificationData ?: return@LaunchedEffect
        
        val type = data["type"]
        val asignaturaId = data["target_asignatura_id"]

        if (type == "nuevo_registro" && usuario.rol == "ADMIN") {
            val tabIndex = tabs.indexOf("Usuarios")
            if (tabIndex != -1) {
                pagerState.animateScrollToPage(tabIndex)
                appViewModel.clearNotificationData()
            }
        } else if (asignaturaId != null) {
            val asignaturas = if (usuario.rol == "PROFESOR") 
                profesorState.asignaturas else estudianteState.asignaturas
            val asignatura = asignaturas.find { it.id == asignaturaId }

            if (asignatura != null) {
                val tabIndex = tabs.indexOf("Asignaturas")
                if (tabIndex != -1) {
                    pagerState.animateScrollToPage(tabIndex)
                    homeState.navigate("Asignaturas", Routes.HomeRoutes.MateriaDetalle(asignatura))
                    appViewModel.clearNotificationData()
                }
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

                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = surfaceColor,
                    shadowElevation = 6.dp
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 44.dp, top = 12.dp, bottom = 12.dp)
                            ) {
                                Text(
                                    text = data.visuals.message,
                                    modifier = Modifier.weight(1f),
                                    color = textColor
                                )
                                if (data.visuals.actionLabel != null) {
                                    TextButton(onClick = { data.performAction() }) {
                                        Text(data.visuals.actionLabel!!, color = primaryColor)
                                    }
                                }
                            }
                            LinearProgressIndicator(
                                progress = { progress.value },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp),
                                color = primaryColor,
                                trackColor = primaryColor.copy(alpha = 0.1f),
                                strokeCap = StrokeCap.Butt
                            )
                        }
                        IconButton(
                            onClick = { data.dismiss() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = surfaceDimColor,
                                modifier = Modifier.size(16.dp)
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
                        actions = {
                            IconButton(
                                onClick = { onOpenDialog(DialogState.Help) },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = surfaceColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = "Ayuda",
                                    tint = surfaceDimColor
                                )
                            }
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
                                homeState.popToRoot(selectedKey)
                            } else {
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
                        isLoading = isLoading,
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
                        isLoading = isLoading,
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
                        isLoading = isLoading,
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
