package samf.gestorestudiantil.ui.screens

import android.widget.Toast
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import samf.gestorestudiantil.R
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.BottomNavBar
import samf.gestorestudiantil.ui.components.CustomOptionsTextField
import samf.gestorestudiantil.ui.components.CustomTextField
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
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                        onGoogleClick = { authViewModel.loginWithGoogleToken(idToken = token) }
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
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
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

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Iniciar Sesión", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            CustomTextField(
                value = email,
                onValueChange = { email = it },
                icon = Icons.Outlined.Email,
                label = "Email",
                readOnly = isLoading,
                isClickable = !isLoading
            )

            CustomTextField(
                value = password,
                onValueChange = { password = it },
                icon = Icons.Outlined.Lock,
                label = "Contraseña",
                readOnly = isLoading,
                isClickable = !isLoading
            )

            Button(
                onClick = { onLoginClick(email, password) },
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
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
    // Control de paso (Wizzard)
    var currentStep by remember { mutableIntStateOf(1) }

    // Paso 1: Datos Personales y Foto
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf("") } // <-- ESTADO PARA LA FOTO

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

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
        Crossfade(targetState = currentStep, label = "RegistroSteps") { step ->
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (step == 1) {
                    // ==========================================
                    // PASO 1: DATOS PERSONALES Y FOTO
                    // ==========================================
                    Text("Crea tu cuenta", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    // COMPONENTE PARA ELEGIR FOTO
                    ProfileImagePicker(
                        currentPhotoUrl = fotoUrl,
                        onPhotoUploaded = { urlSegura -> fotoUrl = urlSegura }
                    )

                    CustomTextField(value = name, onValueChange = { name = it }, icon = Icons.Outlined.Person, label = "Nombre completo", readOnly = isLoading, isClickable = !isLoading)
                    CustomTextField(value = email, onValueChange = { email = it }, icon = Icons.Outlined.Email, label = "Email", readOnly = isLoading, isClickable = !isLoading)
                    CustomTextField(value = password, onValueChange = { password = it }, icon = Icons.Outlined.Lock, label = "Contraseña", readOnly = isLoading, isClickable = !isLoading)
                    CustomTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, icon = Icons.Outlined.Lock, label = "Confirmar contraseña", readOnly = isLoading, isClickable = !isLoading)

                    Button(
                        onClick = {
                            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Por favor rellena todos los campos", Toast.LENGTH_SHORT).show()
                            } else if (password != confirmPassword) {
                                Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            } else if (password.length < 6) {
                                Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                            } else {
                                currentStep = 2 // Avanzar al siguiente paso
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Siguiente")
                    }
                } else {
                    // ==========================================
                    // PASO 2: DATOS ACADÉMICOS
                    // ==========================================
                    Text("Crea tu cuenta", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Paso 2 de 2: Datos académicos", color = surfaceDimColor, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(8.dp))

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

                    // Fila de Botones: Atrás y Registrarse
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentStep = 1 },
                            modifier = Modifier.weight(1f).height(50.dp),
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
                            modifier = Modifier.weight(1f).height(50.dp)
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSetupScreen(
    onSetupComplete: (User) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = primaryColor)
                Text("¡Casi terminamos!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)

                Text(
                    "Como es tu primera vez iniciando con Google, establece una contraseña para tu cuenta. Así podrás iniciar sesión de ambas formas en el futuro.",
                    color = surfaceDimColor,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(
                    value = password,
                    onValueChange = { password = it },
                    icon = Icons.Outlined.Lock,
                    label = "Contraseña",
                    readOnly = authState.isLoading,
                    isClickable = !authState.isLoading
                )

                CustomTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    icon = Icons.Outlined.Lock,
                    label = "Confirmar contraseña",
                    readOnly = authState.isLoading,
                    isClickable = !authState.isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (password != confirmPassword) {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                        } else if (password.length < 6) {
                            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                        } else {
                            authViewModel.completeGoogleSetup(password)
                        }
                    },
                    enabled = !authState.isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Guardar y Continuar")
                }
            }

            if (authState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}