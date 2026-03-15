package samf.gestorestudiantil.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.credentials.CredentialManager
import kotlinx.coroutines.launch
import samf.gestorestudiantil.R
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.signInWithGoogle
import samf.gestorestudiantil.ui.components.BottomNavBar
import samf.gestorestudiantil.ui.components.CustomTextField
import samf.gestorestudiantil.ui.components.CustomPasswordTextField
import samf.gestorestudiantil.ui.components.ProfileImagePicker
import samf.gestorestudiantil.ui.components.SocialMediaButton
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AuthViewModel

val itemsAuth: Map<String, ImageVector> = mapOf(
    "Ingresar" to Icons.AutoMirrored.Filled.Login,
    "Registrarse" to Icons.Default.AppRegistration
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onAuthSuccess: (User) -> Unit,
    onRequireGoogleSetup: () -> Unit,
    onNavigateToRegisterStep2: (name: String, email: String, pass: String, fotoUrl: String) -> Unit
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
                                val googleToken = signInWithGoogle(
                                    context = context,
                                    credentialManager = credentialManager,
                                    serverClientId = token
                                )
                                if (googleToken != null) {
                                    authViewModel.loginWithGoogleToken(googleToken)
                                } else {
                                    Toast.makeText(context, "No se pudo iniciar sesión con Google", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    "Registrarse" -> RegistroPanelStep1(
                        paddingValues = paddingValues,
                        isLoading = authState.isLoading,
                        onNextClick = onNavigateToRegisterStep2
                    )
                }
            }

            if (authState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
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

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        val (headerRef, inputsRef, footerRef) = createRefs()

        Column(
            modifier = Modifier
                .constrainAs(inputsRef) {
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

        Column(
            modifier = Modifier.constrainAs(headerRef) {
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

        Column(
            modifier = Modifier
                .constrainAs(footerRef) {
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

            TextButton(
                onClick = { }
            ) {
                Text("Recordar contraseña")
            }

            HorizontalDivider()

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
fun RegistroPanelStep1(
    paddingValues: PaddingValues,
    isLoading: Boolean,
    onNextClick: (name: String, email: String, pass: String, fotoUrl: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val passwordState = rememberTextFieldState()
    val confirmPasswordState = rememberTextFieldState()
    var fotoUrl by remember { mutableStateOf("") }
    
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            val (headerRef, inputsRef, footerRef) = createRefs()

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

                ProfileImagePicker(
                    currentPhotoUrl = fotoUrl,
                    onPhotoUploaded = { urlSegura -> fotoUrl = urlSegura }
                )
            }

            Column(
                modifier = Modifier
                    .constrainAs(footerRef) {
                        top.linkTo(inputsRef.bottom, margin = 24.dp)
                        centerHorizontallyTo(parent)
                    }
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val password = passwordState.text.toString()
                        val confirmPassword = confirmPasswordState.text.toString()

                        if (name.isBlank() || email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Por favor rellena todos los campos", Toast.LENGTH_SHORT).show()
                        } else if (password != confirmPassword) {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                        } else if (password.length < 6) {
                            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                        } else {
                            onNextClick(name, email, password, fotoUrl)
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
        }
    }
}
