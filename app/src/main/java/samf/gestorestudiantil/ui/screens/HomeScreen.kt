package samf.gestorestudiantil.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Grading
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.data.models.listaRecordatorios
import samf.gestorestudiantil.ui.components.BottomNavBar
import samf.gestorestudiantil.ui.components.TopBarRow
import samf.gestorestudiantil.ui.dialogs.AddRecordatorioDialog
import samf.gestorestudiantil.ui.panels.admin.UsuariosAdminPanel
import samf.gestorestudiantil.ui.panels.estudiante.AsignaturasEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.CalificacionesAsignaturaPanel
import samf.gestorestudiantil.ui.panels.estudiante.CalificacionesEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.HorariosEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.RecordatoriosEstudiantePanel
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.textColor
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
    "Recordatorios" to Icons.Outlined.Notifications
)

val itemsAdmin: Map<String, ImageVector> = mapOf(
    "Usuarios" to Icons.Outlined.Person,
    "Centros" to Icons.Default.Business,
    "Cursos" to Icons.AutoMirrored.Filled.List,
    "Notificaciones" to Icons.Outlined.Notifications
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(usuario: User, navController: NavController, onLogout: () -> Unit) {

    // 2. Seleccionamos el mapa de navegación adecuado según el rol
    val currentNavItems = remember(usuario.rol) {
        when (usuario.rol) {
            "ADMIN" -> itemsAdmin
            "PROFESOR" -> itemsProfesor
            else -> itemsEstudiante
        }
    }

    // Convertimos las claves a lista para el Pager
    val tabs = remember(currentNavItems) { currentNavItems.keys.toList() }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    // Solo mostramos el FAB de añadir en la pestaña "Recordatorios" o "Notificaciones"
    val showFab = tabs[pagerState.currentPage] == "Recordatorios" || tabs[pagerState.currentPage] == "Notificaciones"
    var showRecordatorioDialog by remember { mutableStateOf(false) }

    // Estados para las vistas compartidas o específicas
    var asignaturaSeleccionada by remember { mutableStateOf<Asignatura?>(null) }
    var recordatorios by remember { mutableStateOf(listaRecordatorios) }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    // Reutilizamos tu TopBarRow pasándole los datos del usuario inyectado
                    TopBarRow(
                        usuario.nombre,
                        usuario.rol,
                        usuario.cursoOArea,
                        onNavigateProfile = { navController.navigate("profile") },
                        onNavigateSettings = { navController.navigate("settings") },
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
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                BottomNavBar(
                    items = currentNavItems,
                    selectedItem = tabs[pagerState.currentPage],
                    onItemSelected = { selectedKey ->
                        val index = tabs.indexOf(selectedKey)
                        if (index != -1) {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { showRecordatorioDialog = true },
                    containerColor = primaryColor
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Añadir Recordatorio",
                        tint = textColor,
                    )
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            userScrollEnabled = true
        ) { page ->

            // 3. Renderizamos el panel correspondiente al Tab seleccionado
            when (tabs[page]) {
                "Materias" -> {
                    // Aquí podrías bifurcar: if (usuario.rol == "PROFESOR") ProfesorMateriasPanel() else AsignaturasEstudiantePanel()
                    AsignaturasEstudiantePanel(PaddingValues(0.dp))
                }
                "Horarios" -> {
                    // Misma lógica de reutilización. El panel puede recibir el rol para mostrar diferentes cosas
                    HorariosEstudiantePanel(PaddingValues(0.dp))
                }
                "Recordatorios", "Notificaciones" -> {
                    RecordatoriosEstudiantePanel(PaddingValues(0.dp))
                }
                "Calificaciones" -> {
                    if (asignaturaSeleccionada != null) {
                        CalificacionesAsignaturaPanel(
                            asignatura = asignaturaSeleccionada!!,
                            paddingValues = PaddingValues(0.dp),
                            onBackClick = { asignaturaSeleccionada = null }
                        )
                        BackHandler { asignaturaSeleccionada = null }
                    } else {
                        CalificacionesEstudiantePanel(
                            paddingValues = PaddingValues(0.dp),
                            onAsignaturaClick = { asignatura ->
                                asignaturaSeleccionada = asignatura
                            }
                        )
                    }
                }
                // --- PLACEHOLDERS PARA PANELES DE ADMIN ---
                "Usuarios" -> UsuariosAdminPanel(
                    paddingValues = PaddingValues(0.dp),
                    usuarioActual = usuario
                )
                "Centros" -> PlaceholderPanel("Panel de Gestión de Centros")
                "Cursos" -> PlaceholderPanel("Panel de Gestión de Cursos")
            }
        }
    }

    if (showRecordatorioDialog) {
        AddRecordatorioDialog(
            onDismissRequest = { showRecordatorioDialog = false },
            onAddRecordatorio = { titulo, descripcion, fecha, hora, tipo ->
                // Actualizado para usar el nuevo modelo Recordatorio
                val nuevoRecordatorio = Recordatorio(
                    id = UUID.randomUUID().toString(), // ID temporal para la UI
                    usuarioId = usuario.id,
                    titulo = titulo,
                    descripcion = descripcion,
                    fecha = fecha,
                    hora = hora,
                    tipo = tipo
                )
                recordatorios = recordatorios + nuevoRecordatorio
                showRecordatorioDialog = false
            }
        )
    }
}

// Composable de relleno para las pantallas que aún no están creadas
@Composable
fun PlaceholderPanel(titulo: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = titulo, color = textColor)
    }
}