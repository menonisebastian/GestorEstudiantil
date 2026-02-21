package samf.gestorestudiantil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
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
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import samf.gestorestudiantil.R
import samf.gestorestudiantil.data.models.User
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
fun AuthScreen(onAuthSuccess: (User) -> Unit)
{

    val tabs = remember { itemsAuth.keys.toList() }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val centro = "IES Comercio"

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(centro, fontWeight = FontWeight.ExtraBold, color = textColor)
                },
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
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize(),
            userScrollEnabled = true
        ) { page ->
            when (tabs[page]) {
                "Ingresar" -> LoginPanel(onAuthSuccess, onGoogleClick = {},paddingValues)
                "Registrarse" -> RegistroPanel(paddingValues)
            }
        }
    }
}

// Estos paneles luego los puedes extraer a sus propios archivos
@Composable
fun LoginPanel(onLoginClick: (User) -> Unit, onGoogleClick: () -> Unit, paddingValues: PaddingValues)
{
    val buttonSize = 60.dp
    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center)
    {
        Column(modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp))
        {
            Text("Panel de Iniciar Sesión", color = textColor, fontSize = 20.sp)

            Button(onClick = {
                // Mockeamos un inicio de sesión de Estudiante
                onLoginClick(User(id = "1", nombre = "Sebastian", rol = "ESTUDIANTE", cursoOArea = "DAMV2"))
            }) {
                Text("Entrar como Estudiante")
            }

            Button(onClick = {
                // Mockeamos un inicio de sesión de Profesor
                onLoginClick(User(id = "2", nombre = "Eduardo", rol = "PROFESOR", cursoOArea = "Dpto. Informática"))
            }) {
                Text("Entrar como Profesor")
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center)
            {
                Text("O", color = textColor, fontSize = 16.sp)
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
fun RegistroPanel(paddingValues: PaddingValues)
{
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val dateNow = LocalDate.now()

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            Text("Registro", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            CustomTextField(
                value = name,
                onValueChange = { name = it },
                icon = Icons.Outlined.Person,
                label = "Nombre",
                readOnly = false,
                isClickable = true
            )
            CustomTextField(
                value = email,
                onValueChange = { email = it },
                icon = Icons.Outlined.Email,
                label = "Email",
                readOnly = false,
                isClickable = true
            )
            CustomTextField(
                value = password,
                onValueChange = { password = it },
                icon = Icons.Outlined.Lock,
                label = "Contraseña",
                readOnly = false,
                isClickable = true
            )
            CustomTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                icon = Icons.Outlined.Lock,
                label = "Confirmar contraseña",
                readOnly = false,
                isClickable = true
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center)
            {
                Button(
                    onClick = { /* Lógica para registrar usuario */ }
                ){
                    Text("Registrarme")
                }
            }
        }
    }
}