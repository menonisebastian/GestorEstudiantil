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

    var isCheckingSession by remember { mutableStateOf(true) }
    var needsGoogleSetup by remember { mutableStateOf(false) }

    // ====================================================================
    // VERIFICACIÓN INICIAL
    // ====================================================================
    LaunchedEffect(Unit) {
        val authUser = FirebaseAuth.getInstance().currentUser
        if (authUser != null) {
            try {
                val doc = FirebaseFirestore.getInstance().collection("usuarios").document(authUser.uid).get().await()
                if (doc.exists()) {
                    currentUser = doc.toObject(User::class.java)
                } else {
                    needsGoogleSetup = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        isCheckingSession = false
    }

    if (isCheckingSession) {
        Box(
            modifier = Modifier.fillMaxSize().background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // ====================================================================
    // DETERMINAMOS EL DESTINO INICIAL (Usando los Objetos)
    // ====================================================================
    // El tipo de startDestination ahora es Any (cualquier objeto serializable)
    val startDestination: Any = when {
        needsGoogleSetup -> Routes.GoogleSetup
        currentUser?.estado == "PENDIENTE" -> Routes.Pending
        currentUser != null -> Routes.Home
        else -> Routes.Auth
    }

    // ====================================================================
    // NAVHOST TYPE-SAFE
    // ====================================================================
    NavHost(navController = navController, startDestination = startDestination)
    {

        composable<Routes.Auth> {
            AuthScreen(
                onAuthSuccess = { user ->
                    currentUser = user
                    val destination = if (user.estado == "PENDIENTE") Routes.Pending else Routes.Home

                    navController.navigate(destination) {
                        // Para limpiar el backstack, usamos el tipo genérico también
                        popUpTo<Routes.Auth> { inclusive = true }
                    }
                },
                onRequireGoogleSetup = {
                    navController.navigate(Routes.GoogleSetup) {
                        popUpTo<Routes.Auth> { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Pending> {
            if (currentUser != null) {
                PendingApprovalScreen(
                    usuario = currentUser!!,
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        currentUser = null
                        navController.navigate(Routes.Auth) {
                            // popUpTo(0) no cambia, limpia todo el stack
                            popUpTo(0)
                        }
                    }
                )
            }
        }

        composable<Routes.GoogleSetup> {
            GoogleSetupScreen(
                onSetupComplete = { user ->
                    currentUser = user
                    navController.navigate(Routes.Home) {
                        popUpTo<Routes.GoogleSetup> { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Home> {
            if (currentUser != null) {
                HomeScreen(
                    usuario = currentUser!!,
                    navController = navController,
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        currentUser = null
                        navController.navigate(Routes.Auth) {
                            popUpTo(0)
                        }
                    }
                )
            }
        }

        composable<Routes.Profile> {
            ProfileScreen(usuario = currentUser, onBack = { navController.popBackStack() })
        }

        composable<Routes.Settings> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}