package samf.gestorestudiantil.ui.panels.admin

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
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
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.AccImg
import samf.gestorestudiantil.ui.components.CustomSearchBar
import samf.gestorestudiantil.ui.dialogs.ConfirmDialog
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosAdminPanel(
    paddingValues: PaddingValues,
    usuarioActual: User,
    adminViewModel: AdminViewModel = viewModel()
) {
    val context = LocalContext.current
    var textoBusqueda by remember { mutableStateOf("") }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) } // 0: Pendientes, 1: Activos
    val tabs = listOf("Pendientes", "Activos")
    var confirmDialogVisible by remember { mutableStateOf(false) }

    // Observamos el estado del ViewModel
    val adminState by adminViewModel.adminState.collectAsState()

    // Cargamos los usuarios al iniciar la pantalla
    LaunchedEffect(Unit) {
        adminViewModel.cargarUsuariosPorCentro(usuarioActual.centroId)
    }

    // Mostramos errores si los hay
    LaunchedEffect(adminState.errorMessage) {
        if (adminState.errorMessage != null) {
            Toast.makeText(context, adminState.errorMessage, Toast.LENGTH_LONG).show()
            adminViewModel.clearError()
        }
    }

    // Filtrar la lista real proveniente de Firebase
    val usuariosFiltrados = remember(textoBusqueda, selectedTabIndex, adminState.usuarios) {
        val estadoFiltro = if (selectedTabIndex == 0) "PENDIENTE" else "ACTIVO"
        adminState.usuarios.filter {
            it.estado == estadoFiltro &&
                    (it.nombre.contains(textoBusqueda, ignoreCase = true) || it.email.contains(textoBusqueda, ignoreCase = true))
        }
    }

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Gestión de Usuarios",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
            )

            CustomSearchBar(textoBusqueda, onValueChange = { textoBusqueda = it })
        }

        // Pestañas modernas de Material 3
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = backgroundColor,
            contentColor = textColor
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) textColor else surfaceDimColor
                        )
                    }
                )
            }
        }

        // Contenido Principal
        if (adminState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (usuariosFiltrados.isEmpty()) {
                    item {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("No hay usuarios en esta lista", color = surfaceDimColor)
                        }
                    }
                } else {
                    items(usuariosFiltrados) { usuario ->
                        UsuarioCardAdmin(
                            usuario = usuario,
                            isPending = selectedTabIndex == 0,
                            // CONDICIÓN: Un admin no puede eliminarse a sí mismo
                            canDelete = usuario.id != usuarioActual.id,
                            onAprobar = { adminViewModel.aprobarUsuario(usuario.id) },
                            onRechazar = { confirmDialogVisible = true }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (confirmDialogVisible) {
        ConfirmDialog(
            title = "Eliminar Usuario",
            content = "Desea eliminar al usuario seleccionado? \nEsta accion no se puede deshacer.",
            onConfirm = {
                adminViewModel.rechazarOEliminarUsuario(usuarioActual.id)
                confirmDialogVisible = false
            },
            onDismissRequest = { confirmDialogVisible = false }
        )
    }
}

@Composable
fun UsuarioCardAdmin(
    usuario: User,
    isPending: Boolean,
    canDelete: Boolean, // <-- NUEVO PARÁMETRO
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    val softRed = Color(0xFFD74132)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Imagen de perfil
                AccImg(imgUrl = usuario.imgUrl)

                Spacer(modifier = Modifier.width(16.dp))

                // Info del Usuario
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = usuario.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                    Text(text = usuario.email, fontSize = 12.sp, color = surfaceDimColor)

                    Spacer(modifier = Modifier.height(4.dp))

                    // Chips de Rol y Curso
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RoleChip(rol = usuario.rol)
                        Text(text = "• ${usuario.cursoOArea}", fontSize = 11.sp, color = surfaceDimColor, modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }

                // Acciones para usuarios ACTIVOS (Editar / Eliminar)
                if (!isPending) {
                    Row {
                        IconButton(onClick = { /* TODO: Editar (Abre diálogo) */ }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Editar", tint = surfaceDimColor, modifier = Modifier.size(20.dp))
                        }
                        // MOSTRAR SOLO SI NO ES ÉL MISMO
                        if (canDelete) {
                            IconButton(onClick = onRechazar, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = softRed, modifier = Modifier.size(20.dp))
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFD32F2F)),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rechazar")
                    }

                    Button(
                        onClick = onAprobar,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Aprobar")
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