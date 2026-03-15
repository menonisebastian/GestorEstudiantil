package samf.gestorestudiantil.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.screens.AuthScreen
import samf.gestorestudiantil.ui.screens.GoogleSetupScreen
import samf.gestorestudiantil.ui.screens.HomeScreen
import samf.gestorestudiantil.ui.screens.PendingApprovalScreen
import samf.gestorestudiantil.ui.screens.ProfileScreen
import samf.gestorestudiantil.ui.screens.RegisterStep2Screen
import samf.gestorestudiantil.ui.screens.SettingsScreen
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.viewmodels.AuthViewModel

@Composable
fun AppNavigation() {
    var currentUser by remember { mutableStateOf<User?>(null) }
    var isCheckingSession by remember { mutableStateOf(true) }
    var needsGoogleSetup by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val backStack = remember { mutableStateListOf<Any>() }
    
    val authViewModel: AuthViewModel = viewModel()

    // 1. LISTENER DE SESIÓN
    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                currentUser = null
                isCheckingSession = false
            } else {
                scope.launch {
                    try {
                        val doc = FirebaseFirestore.getInstance()
                            .collection("usuarios")
                            .document(firebaseUser.uid)
                            .get()
                            .await()

                        if (doc.exists()) {
                            val user = doc.toObject(User::class.java)
                            // Validar que el usuario tenga los datos mínimos, sino enviarlo al setup
                            if (user != null && user.rol.isNotBlank()) {
                                currentUser = user
                            } else {
                                needsGoogleSetup = true
                            }
                        } else {
                            needsGoogleSetup = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    // Finalizamos la carga solo después de intentar obtener los datos
                    isCheckingSession = false
                }
            }
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    // 2. GESTIÓN DE NAVEGACIÓN AUTOMÁTICA
    LaunchedEffect(currentUser, isCheckingSession, needsGoogleSetup) {
        if (isCheckingSession) return@LaunchedEffect

        val currentDestination = backStack.lastOrNull()
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser == null) {
            // Caso: No logueado -> Ir a Auth
            if (currentDestination !is Routes.Auth && currentDestination !is Routes.RegisterStep2) {
                backStack.clear()
                backStack.add(Routes.Auth)
            }
        } else {
            // Caso: Logueado -> Decidir destino
            if (needsGoogleSetup) {
                if (currentDestination !is Routes.GoogleSetup) {
                    backStack.clear()
                    backStack.add(Routes.GoogleSetup)
                }
            } else if (currentUser != null) {
                val targetDestination = if (currentUser?.estado == "PENDIENTE") {
                    Routes.Pending
                } else {
                    Routes.Home
                }

                // Navegar solo si el destino es diferente (evita recargas innecesarias)
                if (currentDestination != targetDestination &&
                    currentDestination !is Routes.Profile &&
                    currentDestination !is Routes.Settings) {

                    backStack.clear()
                    backStack.add(targetDestination)
                }
            }
        }
    }

    // 3. PANTALLA DE CARGA
    if (isCheckingSession || backStack.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // 4. NAV DISPLAY
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Routes.Auth> {
                AuthScreen(
                    authViewModel = authViewModel,
                    onAuthSuccess = { /* El listener maneja esto */ },
                    onRequireGoogleSetup = { needsGoogleSetup = true },
                    onNavigateToRegisterStep2 = { name, email, pass, fotoUrl ->
                        backStack.add(Routes.RegisterStep2(name, email, pass, fotoUrl))
                    }
                )
            }

            entry<Routes.RegisterStep2> { route ->
                RegisterStep2Screen(
                    route = route,
                    authViewModel = authViewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<Routes.Pending> {
                if (currentUser != null) {
                    PendingApprovalScreen(
                        usuario = currentUser!!,
                        onLogout = { FirebaseAuth.getInstance().signOut() }
                    )
                }
            }

            entry<Routes.GoogleSetup> {
                GoogleSetupScreen(
                    authViewModel = authViewModel,
                    onSetupComplete = { user ->
                        currentUser = user
                        needsGoogleSetup = false
                    }
                )
            }

            entry<Routes.Home> {
                if (currentUser != null) {
                    HomeScreen(
                        usuario = currentUser!!,
                        navController = null,
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()
                        },
                        onNavigateProfile = { backStack.add(Routes.Profile) },
                        onNavigateSettings = { backStack.add(Routes.Settings) }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            entry<Routes.Profile> {
                ProfileScreen(usuario = currentUser, onBack = { backStack.removeLastOrNull() })
            }

            entry<Routes.Settings> {
                SettingsScreen(onBack = { backStack.removeLastOrNull() })
            }
        }
    )
}
