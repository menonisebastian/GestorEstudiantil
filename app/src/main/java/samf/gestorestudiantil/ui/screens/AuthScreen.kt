package samf.gestorestudiantil.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import samf.gestorestudiantil.R
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.AuthViewModel
import samf.gestorestudiantil.ui.components.BottomNavBar
import samf.gestorestudiantil.ui.components.CustomTextField
import samf.gestorestudiantil.ui.components.SocialMediaButton
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.textColor
import java.time.LocalDate

val itemsAuth: Map<String, ImageVector> = mapOf(
    "Ingresar" to Icons.AutoMirrored.Filled.Login,
    "Registrarse" to Icons.Default.AppRegistration
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: (User) -> Unit,
    authViewModel: AuthViewModel = viewModel() // Instanciamos el ViewModel
) {
    val tabs = remember { itemsAuth.keys.toList() }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val centro = "IES Comercio"
    val context = LocalContext.current

    // Observamos el estado del ViewModel
    val authState by authViewModel.authState.collectAsState()

    // Escuchamos si hay éxito o errores
    LaunchedEffect(authState.isSuccess, authState.errorMessage) {
        if (authState.isSuccess && authState.user != null) {
            onAuthSuccess(authState.user!!)
        }
        if (authState.errorMessage != null) {
            Toast.makeText(context, authState.errorMessage, Toast.LENGTH_LONG).show()
            authViewModel.clearError() // Limpiamos el error después de mostrarlo
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
                userScrollEnabled = !authState.isLoading // Bloqueamos scroll si está cargando
            ) { page ->
                when (tabs[page]) {
                    "Ingresar" -> LoginPanel(
                        paddingValues = paddingValues,
                        isLoading = authState.isLoading,
                        onLoginClick = { email, pass -> authViewModel.loginWithEmail(email, pass) },
                        onGoogleClick = { authViewModel.loginWithGoogleToken(R.string.id_token.toString()) }
                    )
                    "Registrarse" -> RegistroPanel(
                        paddingValues = paddingValues,
                        isLoading = authState.isLoading,
                        onRegisterClick = { email, pass, nombre -> authViewModel.registerWithEmail(email, pass, nombre) }
                    )
                }
            }

            // Overlay de Carga (Spinner)
            if (authState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
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
    onRegisterClick: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Registro", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            CustomTextField(
                value = name,
                onValueChange = { name = it },
                icon = Icons.Outlined.Person,
                label = "Nombre",
                readOnly = isLoading,
                isClickable = !isLoading
            )
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
            CustomTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                icon = Icons.Outlined.Lock,
                label = "Confirmar contraseña",
                readOnly = isLoading,
                isClickable = !isLoading
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = {
                        if (password != confirmPassword) {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                        } else {
                            onRegisterClick(email, password, name)
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Registrarme")
                }
            }
        }
    }
}