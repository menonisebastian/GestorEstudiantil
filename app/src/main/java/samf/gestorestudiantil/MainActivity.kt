package samf.gestorestudiantil

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import samf.gestorestudiantil.domain.FcmTokenManager
import samf.gestorestudiantil.domain.NotificationPermissionGate
import samf.gestorestudiantil.ui.dialogs.DialogOrchestrator
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.navigation.AppNavigation
import samf.gestorestudiantil.ui.theme.GestorEstudiantilTheme
import samf.gestorestudiantil.ui.viewmodels.AuthViewModel
import samf.gestorestudiantil.ui.viewmodels.SettingsViewModel


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var targetAsignaturaId by mutableStateOf<String?>(null)
    private val authViewModel: AuthViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        handleNotificationIntent(intent)
        enableEdgeToEdge()
        setContent {
            val themePreference by settingsViewModel.themePreference.collectAsState()
            val darkTheme = when (themePreference) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            GestorEstudiantilTheme(darkTheme = darkTheme) {
                val dialogStates = remember { mutableStateListOf<DialogState>() }
                
                NotificationPermissionGate(
                    onShowDialog = { dialogStates.add(it) }
                )
                
                DialogOrchestrator(
                    states = dialogStates,
                    onShowDialog = { dialogStates.add(it) },
                    onDismiss = { dialogStates.remove(it) }
                )

                FcmTokenManager(authViewModel)

                AppNavigation(
                    targetAsignaturaId = targetAsignaturaId,
                    onNotificationHandled = { targetAsignaturaId = null },
                    darkTheme = darkTheme
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val asignaturaId = intent?.getStringExtra("target_asignatura_id")
        if (asignaturaId != null) {
            targetAsignaturaId = asignaturaId
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview()
{
    /*

    app/src/main/java/samf/gestorestudiantil/
    ├── data/              # Modelos, Repositorios (Impl) y Servicios (Firebase/Supabase)
    ├── domain/            # Casos de Uso, Interfaces de Repositorios y Mappers
    ├── ui/
    │   ├── components/    # Librería de componentes UI reutilizables
    │   ├── dialogs/       # Orquestador de diálogos basado en estados
    │   ├── navigation/    # Configuración de Navigation 3 y HomeState
    │   ├── panels/        # Interfaces especializadas por rol (Admin/Profe/Estudiante)
    │   ├── screens/       # Pantallas de flujo principal (Auth, Home, Perfil)
    │   ├── theme/         # Sistema de diseño, colores y tipografía
    │   └── viewmodels/    # Lógica de presentación y gestión de estados (StateFlow)
    ├── di/                # Módulos de Hilt (FirebaseModule, RepositoryModule, NetworkModule)
    └── utils/             # Utilidades, ErrorMapper y formateadores

    * */

}
