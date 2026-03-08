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
import androidx.navigation3.runtime.NavEntry
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
import samf.gestorestudiantil.ui.screens.SettingsScreen
import samf.gestorestudiantil.ui.theme.backgroundColor

@Composable
fun AppNavigation() {
    var currentUser by remember { mutableStateOf<User?>(null) }
    var isCheckingSession by remember { mutableStateOf(true) }
    var needsGoogleSetup by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val backStack = remember { mutableStateListOf<Any>() }

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
                            currentUser = doc.toObject(User::class.java)
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
        // Si estamos cargando, no hacemos nada aún
        if (isCheckingSession) return@LaunchedEffect

        val currentDestination = backStack.lastOrNull()

        if (currentUser == null) {
            // Caso: No logueado -> Ir a Auth
            if (currentDestination !is Routes.Auth) {
                backStack.clear()
                backStack.add(Routes.Auth)
            }
        } else {
            // Caso: Logueado -> Decidir destino
            val targetDestination = when {
                needsGoogleSetup -> Routes.GoogleSetup
                currentUser?.estado == "PENDIENTE" -> Routes.Pending
                else -> Routes.Home
            }

            // Navegar solo si el destino es diferente (evita recargas innecesarias)
            // y respetamos si el usuario navegó manualmente a Perfil o Ajustes
            if (currentDestination != targetDestination &&
                currentDestination !is Routes.Profile &&
                currentDestination !is Routes.Settings) {

                // Limpiamos el stack para que no pueda volver atrás al login/loading
                backStack.clear()
                backStack.add(targetDestination)
            }
        }
    }

    // 3. PANTALLA DE CARGA (CORREGIDO)
    // El problema era aquí: NavDisplay no debe ejecutarse si backStack está vacío.
    // Mantenemos el spinner si estamos verificando sesión O si el backstack aún no tiene la primera pantalla.
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
        entryProvider = { key ->
            when (key) {
                is Routes.Auth -> NavEntry(key) {
                    AuthScreen(
                        onAuthSuccess = { /* El listener maneja esto */ },
                        onRequireGoogleSetup = { needsGoogleSetup = true }
                    )
                }

                is Routes.Pending -> NavEntry(key) {
                    if (currentUser != null) {
                        PendingApprovalScreen(
                            usuario = currentUser!!,
                            onLogout = { FirebaseAuth.getInstance().signOut() }
                        )
                    }
                }

                is Routes.GoogleSetup -> NavEntry(key) {
                    GoogleSetupScreen(
                        onSetupComplete = { user ->
                            currentUser = user
                            needsGoogleSetup = false
                        }
                    )
                }

                is Routes.Home -> NavEntry(key) {
                    // Verificación de seguridad extra
                    if (currentUser != null) {
                        HomeScreen(
                            usuario = currentUser!!,
                            navController = null, // Ya no usas NavController clásico
                            onLogout = {
                                FirebaseAuth.getInstance().signOut()
                            },
                            onNavigateProfile = { backStack.add(Routes.Profile) },
                            onNavigateSettings = { backStack.add(Routes.Settings) }
                        )
                    } else {
                        // Fallback por si acaso (evita pantalla blanca si hay desincronización)
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is Routes.Profile -> NavEntry(key) {
                    ProfileScreen(usuario = currentUser, onBack = { backStack.removeLastOrNull() })
                }

                is Routes.Settings -> NavEntry(key) {
                    SettingsScreen(onBack = { backStack.removeLastOrNull() })
                }

                else -> NavEntry(key) { }
            }
        }
    )
}