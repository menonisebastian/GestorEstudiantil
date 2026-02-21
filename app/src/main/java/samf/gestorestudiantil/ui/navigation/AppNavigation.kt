package samf.gestorestudiantil.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.screens.AuthScreen
import samf.gestorestudiantil.ui.screens.HomeScreen
import samf.gestorestudiantil.ui.screens.ProfileScreen
import samf.gestorestudiantil.ui.screens.SettingsScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Estado temporal para simular la sesión.
    // En el futuro, esto se conectará a FirebaseAuth.getInstance().currentUser
    var currentUser by remember { mutableStateOf<User?>(null) }

    NavHost(navController = navController, startDestination = "auth") {

        // 1. PANTALLA DE AUTENTICACIÓN
        composable("auth") {
            AuthScreen(
                onAuthSuccess = { user ->
                    currentUser = user
                    // Navegamos al Home y limpiamos la pila para que el botón "Atrás" no vuelva al Login
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        // 2. PANTALLA PRINCIPAL (Unificada por roles)
        composable("home") {
            if (currentUser != null) {
                HomeScreen(
                    usuario = currentUser!!,
                    navController = navController,
                    onLogout = {
                        currentUser = null
                        // Volvemos al Login y limpiamos toda la pila de navegación
                        navController.navigate("auth") {
                            popUpTo(0)
                        }
                    }
                )
            }
        }

        // 3. PANTALLA DE CUENTA
        composable("profile") {
            ProfileScreen(
                usuario = currentUser,
                onBack = { navController.popBackStack() }
            )
        }

        // 4. PANTALLA DE CONFIGURACIÓN
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}