package samf.gestorestudiantil.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.screens.AuthScreen
import samf.gestorestudiantil.ui.screens.GoogleSetupScreen
import samf.gestorestudiantil.ui.screens.HomeScreen
import samf.gestorestudiantil.ui.screens.PendingApprovalScreen
import samf.gestorestudiantil.ui.screens.ProfileScreen
import samf.gestorestudiantil.ui.screens.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var currentUser by remember { mutableStateOf<User?>(null) }

    NavHost(navController = navController, startDestination = "auth") {

        composable("auth") {
            AuthScreen(
                onAuthSuccess = { user ->
                    currentUser = user
                    // AQUÍ INTERCEPTAMOS EL ESTADO DEL USUARIO
                    val destination = if (user.estado == "PENDIENTE") "pending" else "home"

                    navController.navigate(destination) {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onRequireGoogleSetup = {
                    navController.navigate("google_setup") { popUpTo("auth") { inclusive = true } }
                }
            )
        }

        // NUEVA RUTA PARA USUARIOS PENDIENTES
        composable("pending") {
            if (currentUser != null) {
                PendingApprovalScreen(
                    usuario = currentUser!!,
                    onLogout = {
                        currentUser = null
                        navController.navigate("auth") { popUpTo(0) }
                    }
                )
            }
        }

        // 2. PANTALLA AUXILIAR: CONFIGURACIÓN DE GOOGLE
        composable("google_setup") {
            GoogleSetupScreen(
                onSetupComplete = { user ->
                    currentUser = user
                    navController.navigate("home") {
                        popUpTo("google_setup") { inclusive = true }
                    }
                }
            )
        }

        // 3. PANTALLA PRINCIPAL
        composable("home") {
            if (currentUser != null) {
                HomeScreen(
                    usuario = currentUser!!,
                    navController = navController,
                    onLogout = {
                        currentUser = null
                        navController.navigate("auth") {
                            popUpTo(0)
                        }
                    }
                )
            }
        }

        // 4. PANTALLAS SECUNDARIAS
        composable("profile") {
            ProfileScreen(usuario = currentUser, onBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}