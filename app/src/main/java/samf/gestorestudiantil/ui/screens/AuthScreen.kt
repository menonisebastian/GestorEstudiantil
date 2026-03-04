package samf.gestorestudiantil.ui.screens

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import samf.gestorestudiantil.R
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.signInWithGoogle
import samf.gestorestudiantil.ui.components.BottomNavBar
import samf.gestorestudiantil.ui.components.CustomOptionsTextField
import samf.gestorestudiantil.ui.components.CustomTextField
import samf.gestorestudiantil.ui.components.CustomPasswordTextField
import samf.gestorestudiantil.ui.components.ProfileImagePicker
import samf.gestorestudiantil.ui.components.SocialMediaButton
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AuthViewModel

val itemsAuth: Map<String, ImageVector> = mapOf(
    "Ingresar" to Icons.AutoMirrored.Filled.Login,
    "Registrarse" to Icons.Default.AppRegistration
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: (User) -> Unit,
    onRequireGoogleSetup: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val tabs = remember { itemsAuth.keys.toList() }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val centro = "Gestor Estudiantil"
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val token = stringResource(R.string.id_token)

    val credentialManager = remember { CredentialManager.create(context) }

    LaunchedEffect(authState) {
        if (authState.isSuccess && authState.user != null) {
            onAuthSuccess(authState.user!!)
        }
        if (authState.requireGooglePasswordSetup) {
            onRequireGoogleSetup()
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
                title = { Text(centro, fontWeight = FontWeight.ExtraBold, color = textColor) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor
                )
            )
        },
        bottomBar = {

            BottomNavBar(
                items = itemsAuth,
                selectedItem = tabs[pagerState.currentPage],
                onItemSelected = { selectedKey ->
                    val index = tabs.indexOf(selectedKey)
                    if (index != -1) {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !authState.isLoading
            ) { page ->
                when (tabs[page]) {
                    "Ingresar" -> LoginPanel(
                        paddingValues = paddingValues,
                        isLoading = authState.isLoading,
                        onLoginClick = { email, pass -> authViewModel.loginWithEmail(email, pass) },
                        onGoogleClick = {
                            scope.launch {
                                // lógica compleja encapsulada
                                val googleToken = signInWithGoogle(
                                    context = context,
                                    credentialManager = credentialManager,
                                    serverClientId = token
                                )

                                // Si obtenemos token, el ViewModel hace el resto (backend/firebase)
                                if (googleToken != null) {
                                    authViewModel.loginWithGoogleToken(googleToken)
                                } else {
                                    Toast.makeText(context, "No se pudo iniciar sesión con Google", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    "Registrarse" -> RegistroPanel(
                        paddingValues = paddingValues,
                        isLoading = authState.isLoading,
                        authViewModel = authViewModel,
                        onRegisterClick = { email, pass, nombre, rol, centroId, cursoId, cursoNom, fotoUrl ->
                            authViewModel.registerWithEmail(email, pass, nombre, rol, centroId, cursoId, cursoNom, fotoUrl)
                        }
                    )
                }
            }

            if (authState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
        }
    }
}


@Composable
fun LoginPanel(
    paddingValues: PaddingValues,
    isLoading: Boolean,
    onLoginClick: (String, String) -> Unit,
    onGoogleClick: () -> Unit
) {
    val buttonSize = 60.dp
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val passwordState = rememberTextFieldState()

    // Usamos ConstraintLayout para posicionamiento relativo preciso
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp) // Margen lateral general
    ) {
        // Creamos las referencias para los 3 bloques principales
        val (headerRef, inputsRef, footerRef) = createRefs()

        // 1. BLOQUE DE INPUTS (El centro de todo)
        Column(
            modifier = Modifier
                .constrainAs(inputsRef) {
                    // Esto centra este bloque vertical y horizontalmente en la pantalla
                    centerTo(parent)
                }
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CustomTextField(
                value = email,
                onValueChange = { email = it },
                icon = Icons.Outlined.Email,
                label = "Email",
                readOnly = isLoading,
                isClickable = !isLoading
            )

            CustomPasswordTextField(state = passwordState, isLast = true)
        }

        // 2. HEADER (Título) - Se posiciona ENCIMA de los inputs
        Column(
            modifier = Modifier.constrainAs(headerRef) {
                // La parte inferior del título se ancla a la parte superior de los inputs
                bottom.linkTo(inputsRef.top, margin = 32.dp)
                centerHorizontallyTo(parent)
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Iniciar Sesión",
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 3. FOOTER (Botones) - Se posiciona DEBAJO de los inputs
        Column(
            modifier = Modifier
                .constrainAs(footerRef) {
                    // La parte superior de los botones se ancla a la parte inferior de los inputs
                    top.linkTo(inputsRef.bottom, margin = 32.dp)
                    centerHorizontallyTo(parent)
                }
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    password = passwordState.text.toString()
                    onLoginClick(email, password)
                },
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Entrar")
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("O entra con", color = textColor, fontSize = 16.sp)
            }

            SocialMediaButton(
                iconRes = R.drawable.google,
                size = buttonSize,
                onClick = onGoogleClick
            )
        }
    }
}


@Composable
fun RegistroPanel(
    paddingValues: PaddingValues,
    isLoading: Boolean,
    authViewModel: AuthViewModel,
    onRegisterClick: (email: String, pass: String, nombre: String, rol: String, centroId: String, cursoId: String, cursoNombre: String, fotoUrl: String) -> Unit
) {
    // Control de paso (Wizard)
    var currentStep by rememberSaveable { mutableIntStateOf(1) }

    // Paso 1: Datos Personales y Foto
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val passwordState = rememberTextFieldState()
    val confirmPasswordState = rememberTextFieldState()
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf("") }

    // Paso 2: Selectores Dinámicos
    var rolSeleccionado by remember { mutableStateOf("Seleccionar Rol...") }
    val roles = listOf("ESTUDIANTE", "PROFESOR")

    var centroNombre by remember { mutableStateOf("Seleccionar Instituto...") }
    var centroId by remember { mutableStateOf("") }

    var cursoNombre by remember { mutableStateOf("Seleccionar Curso...") }
    var cursoId by remember { mutableStateOf("") }

    val context = LocalContext.current

    val centrosList by authViewModel.centros.collectAsState()
    val cursosList by authViewModel.cursos.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Crossfade(targetState = currentStep, label = "RegistroSteps") { step ->

            // AQUÍ EMPIEZA EL CAMBIO: Usamos ConstraintLayout para cada paso
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                val (headerRef, inputsRef, footerRef) = createRefs()

                if (step == 1) {
                    // ==========================================
                    // PASO 1: DATOS PERSONALES Y FOTO
                    // ==========================================

                    // 1. INPUTS (Centro absoluto)
                    Column(
                        modifier = Modifier
                            .constrainAs(inputsRef) {
                                centerTo(parent)
                            }
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CustomTextField(value = name, onValueChange = { name = it }, icon = Icons.Outlined.Person, label = "Nombre completo", readOnly = isLoading, isClickable = !isLoading)
                        CustomTextField(value = email, onValueChange = { email = it }, icon = Icons.Outlined.Email, label = "Email", readOnly = isLoading, isClickable = !isLoading)
                        CustomPasswordTextField(state = passwordState)
                        CustomPasswordTextField(state = confirmPasswordState, isLast = true)
                    }

                    // 2. HEADER (Título y Foto) - Arriba de los inputs
                    Column(
                        modifier = Modifier
                            .constrainAs(headerRef) {
                                bottom.linkTo(inputsRef.top, margin = 24.dp)
                                centerHorizontallyTo(parent)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Crea tu cuenta", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                        // Ponemos la foto en el header para que los inputs de texto sean el centro matemático
                        ProfileImagePicker(
                            currentPhotoUrl = fotoUrl,
                            onPhotoUploaded = { urlSegura -> fotoUrl = urlSegura }
                        )
                    }

                    // 3. FOOTER (Botón Siguiente) - Debajo de los inputs
                    Column(
                        modifier = Modifier
                            .constrainAs(footerRef) {
                                top.linkTo(inputsRef.bottom, margin = 24.dp)
                                centerHorizontallyTo(parent)
                            }
                            .fillMaxWidth()
                    ) {
                        password = passwordState.text.toString()
                        confirmPassword = confirmPasswordState.text.toString()

                        Button(
                            onClick = {
                                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                                    Toast.makeText(context, "Por favor rellena todos los campos", Toast.LENGTH_SHORT).show()
                                } else if (password != confirmPassword) {
                                    Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                                } else if (password.length < 6) {
                                    Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                                } else {
                                    currentStep = 2
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Siguiente")
                        }
                    }

                } else {
                    // ==========================================
                    // PASO 2: DATOS ACADÉMICOS
                    // ==========================================

                    // 1. INPUTS (Selectores) - Centro absoluto
                    Column(
                        modifier = Modifier
                            .constrainAs(inputsRef) {
                                centerTo(parent)
                            }
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
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
                                    cursoNombre = "Seleccionar Curso..."
                                    cursoId = ""
                                }
                            },
                            opciones = centrosList.map { it.nombre },
                            icon = Icons.Default.Business,
                            label = "Instituto"
                        )

                        if (rolSeleccionado == "ESTUDIANTE" && centroId.isNotEmpty()) {
                            CustomOptionsTextField(
                                texto = cursoNombre,
                                onValueChange = { nombreSeleccionado ->
                                    cursoNombre = nombreSeleccionado
                                    val cursoSel = cursosList.find { it.nombre == nombreSeleccionado }
                                    if (cursoSel != null) cursoId = cursoSel.id
                                },
                                opciones = cursosList.map { it.nombre },
                                icon = Icons.Default.Class,
                                label = "Curso a matricular"
                            )
                        }
                    }

                    // 2. HEADER (Títulos) - Arriba de los inputs
                    Column(
                        modifier = Modifier
                            .constrainAs(headerRef) {
                                bottom.linkTo(inputsRef.top, margin = 32.dp)
                                centerHorizontallyTo(parent)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Crea tu cuenta", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Paso 2 de 2: Datos académicos", color = surfaceDimColor, fontSize = 14.sp)
                    }

                    // 3. FOOTER (Botones Acción) - Debajo de los inputs
                    Row(
                        modifier = Modifier
                            .constrainAs(footerRef) {
                                top.linkTo(inputsRef.bottom, margin = 32.dp)
                                centerHorizontallyTo(parent)
                            }
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentStep = 1 },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading
                        ) {
                            Text("Atrás", color = textColor)
                        }

                        Button(
                            onClick = {
                                if (rolSeleccionado == "Seleccionar Rol..." || centroId.isEmpty()) {
                                    Toast.makeText(context, "Por favor selecciona rol e instituto", Toast.LENGTH_SHORT).show()
                                } else if (rolSeleccionado == "ESTUDIANTE" && cursoId.isEmpty()) {
                                    Toast.makeText(context, "Los estudiantes deben seleccionar un curso", Toast.LENGTH_SHORT).show()
                                } else {
                                    onRegisterClick(email, password, name, rolSeleccionado, centroId, cursoId, cursoNombre, fotoUrl)
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(
                                if (rolSeleccionado == "ESTUDIANTE")
                                    "Matricularme"
                                else
                                    "Finalizar"
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================
// PANTALLA AUXILIAR: COMPLETAR PERFIL DE GOOGLE
// =========================================================
// En AuthScreen.kt

// TODO: El usuario puede entrar directamente sin aprobacion del admin, revisar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSetupScreen(
    onSetupComplete: (User) -> Unit,
    authViewModel: AuthViewModel = viewModel()
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

    // Observamos los datos del ViewModel
    val centrosList by authViewModel.centros.collectAsState()
    val cursosList by authViewModel.cursos.collectAsState()
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
                            if (cursoSel != null) cursoId = cursoSel.id
                        },
                        opciones = cursosList.map { it.nombre },
                        icon = Icons.Default.Class,
                        label = "Curso a matricular"
                    )
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
                        } else if (rolSeleccionado == "ESTUDIANTE" && cursoId.isEmpty()) {
                            Toast.makeText(context, "Debes seleccionar un curso", Toast.LENGTH_SHORT).show()
                        } else {
                            // Llamada actualizada al ViewModel
                            authViewModel.completeGoogleSetup(
                                password = password,
                                rolSeleccionado = rolSeleccionado,
                                centroId = centroId,
                                cursoId = cursoId,
                                cursoNombre = cursoNombre
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
