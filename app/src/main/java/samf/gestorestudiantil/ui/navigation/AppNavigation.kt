package samf.gestorestudiantil.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.screens.AuthScreen
import samf.gestorestudiantil.ui.screens.GoogleSetupScreen
import samf.gestorestudiantil.ui.screens.HomeScreen
import samf.gestorestudiantil.ui.screens.PendingApprovalScreen
import samf.gestorestudiantil.ui.screens.ProfileScreen
import samf.gestorestudiantil.ui.screens.SettingsScreen
import samf.gestorestudiantil.ui.theme.backgroundColor

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var currentUser by remember { mutableStateOf<User?>(null) }

    // Estados para controlar la verificación inicial
    var isCheckingSession by remember { mutableStateOf(true) }
    var needsGoogleSetup by remember { mutableStateOf(false) }

    // ====================================================================
    // 1. VERIFICACIÓN INICIAL ANTES DE CARGAR LA INTERFAZ
    // ====================================================================
    LaunchedEffect(Unit) {
        val authUser = FirebaseAuth.getInstance().currentUser
        if (authUser != null) {
            try {
                // Si hay token, descargamos su perfil de Firestore
                val doc = FirebaseFirestore.getInstance().collection("usuarios").document(authUser.uid).get().await()
                if (doc.exists()) {
                    currentUser = doc.toObject(User::class.java)
                } else {
                    // Existe en Auth (Google) pero abandonó la app antes de poner contraseña
                    needsGoogleSetup = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Hemos terminado de revisar, quitamos la pantalla de carga
        isCheckingSession = false
    }

    // ====================================================================
    // 2. PANTALLA DE CARGA (SPLASH SCREEN INVISIBLE)
    // ====================================================================
    if (isCheckingSession) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return // <- CRUCIAL: Detenemos la ejecución aquí hasta que termine de cargar
    }

    // ====================================================================
    // 3. DETERMINAMOS EL DESTINO INICIAL SIN PARPADEOS
    // ====================================================================
    val startDestination = when {
        needsGoogleSetup -> "google_setup"
        currentUser?.estado == "PENDIENTE" -> "pending"
        currentUser != null -> "home"
        else -> "auth"
    }

    // ====================================================================
    // 4. NAVHOST (Ahora arranca en la pantalla exacta)
    // ====================================================================
    NavHost(navController = navController, startDestination = startDestination) {

        composable("auth") {
            AuthScreen(
                onAuthSuccess = { user ->
                    currentUser = user
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

        composable("pending") {
            if (currentUser != null) {
                PendingApprovalScreen(
                    usuario = currentUser!!,
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        currentUser = null
                        navController.navigate("auth") { popUpTo(0) }
                    }
                )
            }
        }

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

        composable("home") {
            if (currentUser != null) {
                HomeScreen(
                    usuario = currentUser!!,
                    navController = navController,
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        currentUser = null
                        navController.navigate("auth") {
                            popUpTo(0)
                        }
                    }
                )
            }
        }

        composable("profile") {
            ProfileScreen(usuario = currentUser, onBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}