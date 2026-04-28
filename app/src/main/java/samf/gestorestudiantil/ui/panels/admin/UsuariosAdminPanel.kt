package samf.gestorestudiantil.ui.panels.admin

import androidx.compose.animation.core.tween
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import samf.gestorestudiantil.R
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.AccImg
import samf.gestorestudiantil.ui.components.CustomSearchBar
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.errorColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AdminState
import samf.gestorestudiantil.ui.viewmodels.AdminViewModel
import samf.gestorestudiantil.ui.viewmodels.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosAdminPanel(
    paddingValues: PaddingValues,
    usuarioActual: User,
    adminViewModel: AdminViewModel = viewModel(),
    onOpenDialog: (DialogState) -> Unit,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var textoBusquedaRaw by rememberSaveable { mutableStateOf("") }
    var textoBusqueda by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(textoBusquedaRaw) {
        kotlinx.coroutines.delay(300)
        textoBusqueda = textoBusquedaRaw
    }

    var filtroRol by rememberSaveable { mutableStateOf("") }
    var filtroCurso by rememberSaveable { mutableStateOf("") }
    var filtroCiclo by rememberSaveable { mutableStateOf("") }
    var filtroTurno by rememberSaveable { mutableStateOf("") }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) } // 0: Activos, 1: Pendientes
    val tabs = listOf(stringResource(R.string.admin_tab_active), stringResource(R.string.admin_tab_pending))

    // Observamos el estado del ViewModel
    val adminState by adminViewModel.adminState.collectAsState()

    val countActivos by remember(adminState.usuarios) { derivedStateOf { adminState.usuarios.count { it.estado == "ACTIVO" } } }
    val countPendientes by remember(adminState.usuarios) { derivedStateOf { adminState.usuarios.count { it.estado == "PENDIENTE" } } }

    // Cargamos los usuarios y cursos al iniciar la pantalla
    LaunchedEffect(Unit) {
        adminViewModel.cargarUsuariosPorCentro(usuarioActual.centroId)
        adminViewModel.cargarCursosPorCentro(usuarioActual.centroId)
    }

    // Mostramos errores si los hay
    LaunchedEffect(adminState.errorMessage) {
        if (adminState.errorMessage != null) {
            Toast.makeText(context, adminState.errorMessage, Toast.LENGTH_LONG).show()
            adminViewModel.clearError()
        }
    }

    // Filtrar la lista real proveniente de Firebase
    val usuariosFiltrados by remember(textoBusqueda, filtroRol, filtroCurso, filtroCiclo, filtroTurno, selectedTabIndex, adminState.usuarios) {
        derivedStateOf {
            val estadoFiltro = if (selectedTabIndex == 1) "PENDIENTE" else "ACTIVO"
            val rolesSeleccionados = filtroRol.split(",").filter { it.isNotEmpty() }
            val cursosSeleccionados = filtroCurso.split(",").filter { it.isNotEmpty() }
            val ciclosSeleccionados = filtroCiclo.split(",").filter { it.isNotEmpty() }
            val turnosSeleccionados = filtroTurno.split(",").filter { it.isNotEmpty() }

            adminState.usuarios.filter {
                val coincideEstado = it.estado == estadoFiltro
                val coincideTexto = it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                                    it.email.contains(textoBusqueda, ignoreCase = true)
                val coincideRol = if (rolesSeleccionados.isEmpty()) true else rolesSeleccionados.any { rol -> it.rol.equals(rol, ignoreCase = true) }
                val coincideCurso = if (cursosSeleccionados.isEmpty()) true else {
                    when (it) {
                        is User.Estudiante -> cursosSeleccionados.any { curso -> it.curso.contains(curso, ignoreCase = true) }
                        is User.Profesor -> cursosSeleccionados.any { curso -> it.departamento.contains(curso, ignoreCase = true) }
                        else -> true
                    }
                }
                val coincideCiclo = if (ciclosSeleccionados.isEmpty()) true else {
                    when (it) {
                        is User.Estudiante -> ciclosSeleccionados.any { ciclo -> it.cicloNum.toString() == ciclo }
                        else -> true
                    }
                }
                val coincideTurno = if (turnosSeleccionados.isEmpty()) true else {
                    when (it) {
                        is User.Estudiante -> turnosSeleccionados.any { turno -> it.turno.equals(turno, ignoreCase = true) }
                        else -> true
                    }
                }

                coincideEstado && coincideTexto && coincideRol && coincideCurso && coincideCiclo && coincideTurno
            }
        }
    }

    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        // Contenido Principal (Pasa por debajo del cabezal)
        if (adminState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 220.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (usuariosFiltrados.isEmpty()) {
                    item {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.admin_no_users), color = surfaceDimColor)
                        }
                    }
                } else {
                    items(
                        items = usuariosFiltrados,
                        key = { it.id }
                    ) { usuario ->
                        UsuarioCardAdmin(
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(150),
                                fadeOutSpec = tween(150),
                                placementSpec = tween(150)
                            ),
                            usuario = usuario,
                            isPending = selectedTabIndex == 1,
                            canDelete = usuario.id != usuarioActual.id,
                            onAprobar = { adminViewModel.aprobarUsuario(usuario) },
                            onRechazar = {
                                onOpenDialog(
                                    DialogState.Confirmation(
                                        title = R.string.admin_delete_user_title.toString(),
                                        content = R.string.admin_delete_user_confirm.toString() + " ${usuario.nombre}?",
                                        onConfirm = {
                                            adminViewModel.rechazarOEliminarUsuario(usuario) {
                                                appViewModel.showSnackbar(
                                                    message = "Usuario eliminado",
                                                    actionLabel = "Deshacer",
                                                    onAction = {
                                                        adminViewModel.guardarUsuario(usuario)
                                                    }
                                                )
                                            }
                                        }
                                    )
                                )
                            },
                            onUserDialog = {
                                onOpenDialog(DialogState.UserProfile(usuario))
                            },
                            onOpenDialog = onOpenDialog,
                            adminState = adminState
                        )
                    }
                }
            }
        }

        // Cabezal Flotante (Título + Barra de Búsqueda + Tabs)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor.copy(alpha = 0.95f)) // Fondo para el área del TabRow
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.admin_management_users),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )

                    CustomSearchBar(
                        textoBusqueda = textoBusquedaRaw,
                        onValueChange = { textoBusquedaRaw = it },
                        onFilterClick = {
                            onOpenDialog(
                                DialogState.Filter(
                                    tipo = "Usuario",
                                    currentFilters = mapOf(
                                        "rol" to filtroRol,
                                        "curso" to filtroCurso,
                                        "ciclo" to filtroCiclo,
                                        "turno" to filtroTurno
                                    ),
                                    opcionesPersonalizadas = mapOf(
                                        "cursos" to adminState.cursos.map { it.acronimo }.distinct()
                                    ),
                                    onApply = { seleccion ->
                                        filtroRol = seleccion["rol"] ?: ""
                                        filtroCurso = seleccion["curso"] ?: ""
                                        filtroCiclo = seleccion["ciclo"] ?: ""
                                        filtroTurno = seleccion["turno"] ?: ""
                                    }
                                )
                            )
                        },
                        filters = mapOf(
                            "rol" to filtroRol,
                            "curso" to filtroCurso,
                            "ciclo" to filtroCiclo,
                            "turno" to filtroTurno
                        ),
                        onRemoveFilter = { keyPlusValue ->
                            val (key, newValue) = if (keyPlusValue.contains(":")) {
                                val parts = keyPlusValue.split(":")
                                parts[0] to parts[1]
                            } else {
                                keyPlusValue to ""
                            }
                            when(key) {
                                "rol" -> filtroRol = newValue
                                "curso" -> filtroCurso = newValue
                                "ciclo" -> filtroCiclo = newValue
                                "turno" -> filtroTurno = newValue
                            }
                        }
                    )
                }
            }

            // Pestañas (TabRow) - Se mantienen fuera de la Card pero dentro del área flotante
            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent, // Transparent para usar el fondo de la columna
                contentColor = textColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    val count = if (index == 0) countActivos else countPendientes
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) textColor else surfaceDimColor
                                )
                                if (count > 0) {
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 6.dp)
                                            .background(
                                                color = if (selectedTabIndex == index) primaryColor.copy(alpha = 0.15f) else surfaceDimColor.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedTabIndex == index) primaryColor else surfaceDimColor
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UsuarioCardAdmin(
    modifier: Modifier = Modifier,
    usuario: User,
    isPending: Boolean,
    canDelete: Boolean,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    onUserDialog: () -> Unit,
    onOpenDialog: (DialogState) -> Unit,
    adminViewModel: AdminViewModel = viewModel(),
    adminState: AdminState
) {
    val softRed = Color(0xFFD74132)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Imagen de perfil
                AccImg(userName = usuario.nombre, imgUrl = usuario.imgUrl, onClick = { onUserDialog() }, size = 48.dp)

                Spacer(modifier = Modifier.width(16.dp))

                // Info del Usuario
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = usuario.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                    Text(
                        text = usuario.email,
                        fontSize = 12.sp,
                        color = surfaceDimColor,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Chips de Rol y Curso
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RoleChip(rol = usuario.rol)
                        val infoExtra = when (usuario) {
                            is User.Estudiante -> usuario.curso
                            is User.Profesor -> usuario.departamento
                            else -> ""
                        }
                        if (infoExtra.isNotEmpty()) {
                            Text(
                                text = "• $infoExtra",
                                fontSize = 11.sp,
                                color = surfaceDimColor,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Acciones para usuarios ACTIVOS (Editar / Eliminar)
                if (!isPending) {
                    Row {
                        IconButton(
                            onClick = {
                                onOpenDialog(DialogState.EditUser(
                                    user = usuario,
                                    cursos = adminState.cursos,
                                    onSave = { updated ->
                                        adminViewModel.guardarUsuario(updated)
                                    }
                                ))
                            }, 
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.label_edit), tint = surfaceDimColor, modifier = Modifier.size(20.dp))
                        }
                        
                        // Botón adicional para Profesores (Asignar materias)
                        if (usuario.rol == "PROFESOR") {
                            IconButton(
                                onClick = {
                                    onOpenDialog(DialogState.AsignarAsignaturas(
                                        profesor = usuario,
                                        onAssign = { },
                                        onUnassign = { }
                                    ))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Outlined.School, contentDescription = stringResource(R.string.label_subjects), tint = surfaceDimColor, modifier = Modifier.size(20.dp))
                            }
                        }

                        // MOSTRAR SOLO SI NO ES ÉL MISMO
                        if (canDelete) {
                            IconButton(onClick = onRechazar, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.label_delete), tint = softRed, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // Acciones para usuarios PENDIENTES (Aprobar / Rechazar)
            if (isPending) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onRechazar,
                        colors = ButtonDefaults.buttonColors(containerColor = errorColor.copy(alpha = 0.1f), contentColor = errorColor),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.admin_reject))
                        }
                    }

                    Button(
                        onClick = onAprobar,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.1f), contentColor = primaryColor),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.admin_approve))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoleChip(rol: String) {
    val (bgColor, txtColor) = when (rol) {
        "PROFESOR" -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100)) // Naranja
        "ADMIN" -> Pair(Color(0xFFF3E5F5), Color(0xFF4A148C)) // Morado
        else -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0)) // Azul (Estudiante)
    }

    Box(
        modifier = Modifier
            .background(color = bgColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = rol, color = txtColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}
