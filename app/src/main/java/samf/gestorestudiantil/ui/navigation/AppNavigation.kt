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
import samf.gestorestudiantil.ui.screens.ProfileScreen
import samf.gestorestudiantil.ui.screens.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var currentUser by remember { mutableStateOf<User?>(null) }

    NavHost(navController = navController, startDestination = "auth") {

        // 1. PANTALLA DE AUTENTICACIÓN
        composable("auth") {
            AuthScreen(
                onAuthSuccess = { user ->
                    currentUser = user
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                // Atrapamos cuando el ViewModel nos dice que es un nuevo usuario de Google
                onRequireGoogleSetup = {
                    navController.navigate("google_setup") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
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