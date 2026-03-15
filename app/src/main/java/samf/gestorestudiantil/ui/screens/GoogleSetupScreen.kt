package samf.gestorestudiantil.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.CustomOptionsTextField
import samf.gestorestudiantil.ui.components.CustomPasswordTextField
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSetupScreen(
    onSetupComplete: (User) -> Unit,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    // Estados para contraseña
    val passwordState = rememberTextFieldState()
    val confirmPasswordState = rememberTextFieldState()

    // --- NUEVOS ESTADOS PARA DATOS ACADÉMICOS ---
    var rolSeleccionado by remember { mutableStateOf("Seleccionar Rol...") }
    val roles = listOf("ESTUDIANTE", "PROFESOR")

    var centroNombre by remember { mutableStateOf("Seleccionar Instituto...") }
    var centroId by remember { mutableStateOf("") }

    var cursoNombre by remember { mutableStateOf("Seleccionar Curso...") }
    var cursoId by remember { mutableStateOf("") }

    var turno by remember { mutableStateOf("Seleccionar Turno...") }

    // Observamos los datos del ViewModel
    val centrosList by authViewModel.centros.collectAsState()
    val cursosList by authViewModel.cursos.collectAsState()
    
    val turnosDisponibles = remember(cursoId, cursosList) {
        cursosList.find { it.id == cursoId }?.turnos ?: emptyList()
    }
    // ---------------------------------------------

    LaunchedEffect(authState) {
        if (authState.isSuccess && authState.user != null) {
            onSetupComplete(authState.user!!)
        }
        if (authState.errorMessage != null) {
            Toast.makeText(context, authState.errorMessage, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Completar Registro", fontWeight = FontWeight.ExtraBold, color = textColor) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { paddingValues ->

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                // Añadimos scroll por si la pantalla es pequeña para tantos campos
                .verticalScroll(rememberScrollState())
        ) {
            val (headerRef, inputsRef, footerRef) = createRefs()

            // 1. HEADER
            Column(
                modifier = Modifier
                    .constrainAs(headerRef) {
                        top.linkTo(parent.top, margin = 16.dp) // Cambiado a top para dar espacio
                        centerHorizontallyTo(parent)
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = primaryColor
                )
                Text(
                    text = "Configuración Final",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "Establece tu contraseña y datos académicos.",
                    color = surfaceDimColor,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            // 2. INPUTS (Contraseñas + Selectores)
            Column(
                modifier = Modifier
                    .constrainAs(inputsRef) {
                        top.linkTo(headerRef.bottom, margin = 24.dp)
                        centerHorizontallyTo(parent)
                    }
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- SECCIÓN CONTRASEÑA ---
                CustomPasswordTextField(state = passwordState)
                CustomPasswordTextField(state = confirmPasswordState)

                // --- SECCIÓN ACADÉMICA (NUEVO) ---
                CustomOptionsTextField(
                    texto = rolSeleccionado,
                    onValueChange = { rolSeleccionado = it },
                    opciones = roles,
                    icon = Icons.Outlined.Person,
                    label = "¿Eres Estudiante o Profesor?"
                )

                CustomOptionsTextField(
                    texto = centroNombre,
                    onValueChange = { nombreSeleccionado ->
                        centroNombre = nombreSeleccionado
                        val centroSel = centrosList.find { it.nombre == nombreSeleccionado }
                        if (centroSel != null) {
                            centroId = centroSel.id
                            authViewModel.loadCursosPorCentro(centroSel.id)
                            // Resetear curso al cambiar centro
                            cursoNombre = "Seleccionar Curso..."
                            cursoId = ""
                            turno = "Seleccionar Turno..."
                        }
                    },
                    opciones = centrosList.map { it.nombre },
                    icon = Icons.Default.Business,
                    label = "Instituto"
                )

                // Mostrar curso solo si es estudiante y ya eligió centro
                if (rolSeleccionado == "ESTUDIANTE" && centroId.isNotEmpty()) {
                    CustomOptionsTextField(
                        texto = cursoNombre,
                        onValueChange = { nombreSeleccionado ->
                            cursoNombre = nombreSeleccionado
                            val cursoSel = cursosList.find { it.nombre == nombreSeleccionado }
                            if (cursoSel != null) {
                                cursoId = cursoSel.id
                                turno = "Seleccionar Turno..."
                            }
                        },
                        opciones = cursosList.map { it.nombre },
                        icon = Icons.Default.Class,
                        label = "Curso a matricular"
                    )
                    
                    if (cursoId.isNotEmpty() && turnosDisponibles.isNotEmpty()) {
                        CustomOptionsTextField(
                            texto = turno,
                            onValueChange = { turno = it },
                            opciones = turnosDisponibles,
                            icon = Icons.Default.Schedule,
                            label = "Turno"
                        )
                    }
                }
            }

            // 3. FOOTER (Botón)
            Column(
                modifier = Modifier
                    .constrainAs(footerRef) {
                        top.linkTo(inputsRef.bottom, margin = 32.dp)
                        bottom.linkTo(parent.bottom, margin = 16.dp) // Anclado abajo también
                        centerHorizontallyTo(parent)
                    }
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val password = passwordState.text.toString()
                        val confirmPassword = confirmPasswordState.text.toString()

                        // Validaciones
                        if (password != confirmPassword) {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                        } else if (password.length < 6) {
                            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                        } else if (rolSeleccionado == "Seleccionar Rol..." || centroId.isEmpty()) {
                            Toast.makeText(context, "Selecciona tu rol e instituto", Toast.LENGTH_SHORT).show()
                        } else if (rolSeleccionado == "ESTUDIANTE") {
                            if (cursoId.isEmpty()) {
                                Toast.makeText(context, "Debes seleccionar un curso", Toast.LENGTH_SHORT).show()
                            } else if (turnosDisponibles.isNotEmpty() && turno == "Seleccionar Turno...") {
                                Toast.makeText(context, "Debes seleccionar un turno", Toast.LENGTH_SHORT).show()
                            } else {
                                authViewModel.completeGoogleSetup(
                                    password = password,
                                    rolSeleccionado = rolSeleccionado,
                                    centroId = centroId,
                                    cursoId = cursoId,
                                    cursoNombre = cursoNombre,
                                    turno = turno
                                )
                            }
                        } else {
                            // Llamada actualizada al ViewModel
                            authViewModel.completeGoogleSetup(
                                password = password,
                                rolSeleccionado = rolSeleccionado,
                                centroId = centroId,
                                cursoId = cursoId,
                                cursoNombre = cursoNombre,
                                turno = turno
                            )
                        }
                    },
                    enabled = !authState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Finalizar Registro")
                }
            }

            if (authState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.constrainAs(createRef()) { centerTo(parent) }
                )
            }
        }
    }
}
