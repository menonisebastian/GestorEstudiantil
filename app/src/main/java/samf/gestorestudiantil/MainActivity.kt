package samf.gestorestudiantil

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import samf.gestorestudiantil.domain.notifications.FcmTokenManager
import samf.gestorestudiantil.domain.notifications.NotificationPermissionGate
import samf.gestorestudiantil.ui.dialogs.DialogOrchestrator
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.navigation.AppNavigation
import samf.gestorestudiantil.ui.theme.GestorEstudiantilTheme
import samf.gestorestudiantil.ui.viewmodels.AppViewModel
import samf.gestorestudiantil.ui.viewmodels.AuthViewModel
import samf.gestorestudiantil.ui.viewmodels.SettingsViewModel


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        appViewModel.handleIntent(intent)
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

                AppNavigation(darkTheme = darkTheme)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        appViewModel.handleIntent(intent)
    }
}
