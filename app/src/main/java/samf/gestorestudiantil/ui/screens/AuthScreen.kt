package samf.gestorestudiantil.ui.screens

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import samf.gestorestudiantil.R
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.authRouteToTab
import samf.gestorestudiantil.domain.authTabToRoute
import samf.gestorestudiantil.domain.signInWithGoogle
import samf.gestorestudiantil.ui.components.BottomNavBar
import samf.gestorestudiantil.ui.components.CustomTextField
import samf.gestorestudiantil.ui.components.CustomPasswordTextField
import samf.gestorestudiantil.ui.components.ProfileImagePicker
import samf.gestorestudiantil.ui.components.SocialMediaButton
import samf.gestorestudiantil.ui.components.TextDivider
import samf.gestorestudiantil.ui.components.TitleLogo
import samf.gestorestudiantil.ui.navigation.Routes
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.theme.whiteColor
import samf.gestorestudiantil.ui.viewmodels.AuthViewModel

val itemsAuth: Map<String, ImageVector> = mapOf(
    "Ingresar" to Icons.AutoMirrored.Filled.Login,
    "Registrarse" to Icons.Default.AppRegistration
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: (User) -> Unit,
    onRequireGoogleSetup: () -> Unit,
    onNavigateToRegisterStep2: (name: String, email: String, pass: String, fotoUrl: String) -> Unit
) {
    val tabs = remember { itemsAuth.keys.toList() }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val token = stringResource(R.string.id_token)
    val credentialManager = remember { CredentialManager.create(context) }

    // ✅ Back stack interno de AuthScreen
    val authBackStack = remember {
        mutableStateListOf<Any>(Routes.AuthRoutes.Login)
    }

    // ✅ Pager → authBackStack
    LaunchedEffect(pagerState.currentPage) {
        val newRoute = authTabToRoute(tabs[pagerState.currentPage])
        val currentTop = authBackStack.lastOrNull()
        if (currentTop == null || newRoute::class != currentTop::class) {
            authBackStack.clear()
            authBackStack.add(newRoute)
        }
    }

    // ✅ authBackStack → Pager
    LaunchedEffect(authBackStack.toList()) {
        val topRoute = authBackStack.lastOrNull() ?: return@LaunchedEffect
        val targetTab = authRouteToTab(topRoute)
        val index = tabs.indexOf(targetTab)
        if (index != -1 && index != pagerState.currentPage) {
            scope.launch { pagerState.animateScrollToPage(index) }
        }
    }

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
                title = {
                    Row (
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp).fillMaxWidth()) {
                        TitleLogo(150.dp)
                    }
                },
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

                val pageRoute = authTabToRoute(tabs.getOrNull(page) ?: "Ingresar")
                val pageBackStack = remember(pageRoute) {
                    mutableStateListOf<Any>(pageRoute)
                }

                NavDisplay(
                    backStack = pageBackStack,
                    onBack = { pageBackStack.removeLastOrNull() },
                    entryProvider = entryProvider {

                        entry<Routes.AuthRoutes.Login> {
                            LoginPanel(
                                paddingValues = paddingValues,
                                isLoading = authState.isLoading,
                                onLoginClick = { email, pass ->
                                    authViewModel.loginWithEmail(email, pass)
                                },
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
                                            Toast.makeText(
                                                context,
                                                "No se pudo iniciar sesión con Google",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                onForgotPassword = {
                                    pageBackStack.add(Routes.AuthRoutes.ForgotPassword)
                                }
                            )
                        }

                        entry<Routes.AuthRoutes.ForgotPassword> {
                            ForgotPasswordPanel(
                                paddingValues = paddingValues,
                                isLoading = authState.isLoading,
                                onResetClick = { email ->
                                    authViewModel.resetPassword(email) {
                                        pageBackStack.add(Routes.AuthRoutes.ForgotPasswordSuccess)
                                    }
                                },
                                onBack = { pageBackStack.removeLastOrNull() }
                            )
                        }

                        entry<Routes.AuthRoutes.ForgotPasswordSuccess> {
                            ForgotPasswordSuccessPanel(
                                paddingValues = paddingValues,
                                onBackToLogin = {
                                    pageBackStack.clear()
                                    pageBackStack.add(Routes.AuthRoutes.Login)
                                }
                            )
                        }

                        entry<Routes.AuthRoutes.Register> {
                            RegistroPanelStep1(
                                paddingValues = paddingValues,
                                isLoading = authState.isLoading,
                                onNextClick = onNavigateToRegisterStep2
                            )
                        }
                    }
                )
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
fun ForgotPasswordSuccessPanel(
    paddingValues: PaddingValues,
    onBackToLogin: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        val (iconRef, textRef, footerRef) = createRefs()

        androidx.compose.material3.Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color(0xFF4CAF50),
            modifier = Modifier
                .size(100.dp)
                .constrainAs(iconRef) {
                    centerHorizontallyTo(parent)
                    bottom.linkTo(textRef.top, margin = 24.dp)
                }
        )

        Column(
            modifier = Modifier
                .constrainAs(textRef) {
                    centerTo(parent)
                }
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "¡Correo enviado!",
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Revisa tu bandeja de entrada y sigue las instrucciones para restablecer tu contraseña.",
                color = surfaceDimColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Button(
            onClick = onBackToLogin,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .constrainAs(footerRef) {
                    top.linkTo(textRef.bottom, margin = 32.dp)
                    centerHorizontallyTo(parent)
                }
        ) {
            Text("Volver al inicio", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = whiteColor)
        }
    }
}

@Composable
fun ForgotPasswordPanel(
    paddingValues: PaddingValues,
    isLoading: Boolean,
    onResetClick: (String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        val (headerRef, inputsRef, footerRef) = createRefs()

        Column(
            modifier = Modifier.constrainAs(headerRef) {
                bottom.linkTo(inputsRef.top, margin = 32.dp)
                centerHorizontallyTo(parent)
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Recuperar contraseña",
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Introduce tu email para recibir un enlace de recuperación",
                color = surfaceDimColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

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
                label = "Email",
                icon = Icons.Outlined.Email,
                readOnly = isLoading,
                isClickable = !isLoading
            )

            Button(
                onClick = {
                    if (email.isBlank()) {
                        Toast.makeText(context, "Por favor, introduce tu email", Toast.LENGTH_SHORT).show()
                    } else {
                        onResetClick(email)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Enviar enlace", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = whiteColor)
            }
        }

        Column(
            modifier = Modifier
                .constrainAs(footerRef) {
                    top.linkTo(inputsRef.bottom, margin = 16.dp)
                    centerHorizontallyTo(parent)
                }
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextButton(onClick = onBack, enabled = !isLoading) {
                Text("Volver al inicio de sesión", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LoginPanel(
    paddingValues: PaddingValues,
    isLoading: Boolean,
    onLoginClick: (String, String) -> Unit,
    onGoogleClick: () -> Unit,
    onForgotPassword: () -> Unit
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
                label = "Email",
                icon = Icons.Outlined.Email,
                readOnly = isLoading,
                isClickable = !isLoading
            )

            CustomPasswordTextField(state = passwordState, isLast = true)

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
                Text("Ingresar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = whiteColor)
            }
        }

        Column(
            modifier = Modifier.constrainAs(headerRef) {
                bottom.linkTo(inputsRef.top, margin = 32.dp)
                centerHorizontallyTo(parent)
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido de vuelta!",
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Por favor, ingresa tus datos para continuar",
                color = surfaceDimColor,
                fontSize = 14.sp,
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
            TextButton(
                onClick = { onForgotPassword() },
            ) {
                Text(text = "Recordar contraseña", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            TextDivider(text = "O ingresa con")

            Spacer(modifier = Modifier.height(4.dp))

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
                CustomTextField(value = name, onValueChange = { name = it }, label = "Nombre completo", icon = Icons.Outlined.Person, readOnly = isLoading, isClickable = !isLoading)
                CustomTextField(value = email, onValueChange = { email = it }, label = "Email", icon = Icons.Outlined.Email, readOnly = isLoading, isClickable = !isLoading)
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
                Text("Crea tu cuenta ya!", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)

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
                    Text("Siguiente", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = whiteColor)
                }
            }
        }
    }
}
