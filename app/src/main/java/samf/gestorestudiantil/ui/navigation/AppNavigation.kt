package samf.gestorestudiantil.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.screens.AuthScreen
import samf.gestorestudiantil.ui.screens.GoogleAcademicSetupScreen
import samf.gestorestudiantil.ui.screens.GooglePasswordSetupScreen
import samf.gestorestudiantil.ui.screens.HomeScreen
import samf.gestorestudiantil.ui.screens.PendingApprovalScreen
import samf.gestorestudiantil.ui.screens.ProfileScreen
import samf.gestorestudiantil.ui.screens.RegisterStep2Screen
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.viewmodels.AuthViewModel

@Composable
fun AppNavigation(
    targetAsignaturaId: String? = null,
    onNotificationHandled: () -> Unit = {},
    darkTheme: Boolean
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    var currentUser by remember { mutableStateOf<User?>(null) }
    var isCheckingSession by remember { mutableStateOf(true) }
    var needsGoogleSetup by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val backStack = remember { mutableStateListOf<Any>() }
    
    // 1. LISTENER DE SESIÓN Y DATOS DE USUARIO EN TIEMPO REAL
    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        var userListener: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            userListener?.remove()

            if (firebaseUser == null) {
                currentUser = null
                needsGoogleSetup = false
                isCheckingSession = false
            } else {
                userListener = db.collection("usuarios")
                    .document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null && snapshot.exists()) {
                            val user = snapshot.toObject(User::class.java)
                            if (user != null && user.rol.isNotBlank()) {
                                currentUser = user
                                needsGoogleSetup = false
                            } else {
                                needsGoogleSetup = true
                            }
                        } else {
                            needsGoogleSetup = true
                        }
                        isCheckingSession = false
                    }
            }
        }

        auth.addAuthStateListener(authListener)
        onDispose {
            auth.removeAuthStateListener(authListener)
            userListener?.remove()
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
        } else if (isCheckingSession) {
            // Do nothing while checking session
        } else {
            // Caso: Logueado -> Decidir destino
            if (needsGoogleSetup) {
                if (currentDestination !is Routes.GooglePasswordSetup && currentDestination !is Routes.GoogleAcademicSetup) {
                    backStack.clear()
                    backStack.add(Routes.GooglePasswordSetup)
                }
            } else if (currentUser != null) {
                val targetDestination = if (currentUser?.estado == "PENDIENTE") {
                    Routes.Pending
                } else {
                    Routes.Home
                }

                // Navegar solo si el destino es diferente (evita recargas innecesarias)
                if (currentDestination != targetDestination &&
                    currentDestination !is Routes.Profile) {

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
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            )
        },
        entryProvider = entryProvider {
            entry<Routes.Auth> {
                AuthScreen(
                    authViewModel = authViewModel,
                    darkTheme = darkTheme,
                    onAuthSuccess = { /* El listener maneja esto */ },
                    onRequireGoogleSetup = { needsGoogleSetup = true },
                    onNavigateToRegisterStep2 = { name, email, pass, fotoUrl ->
                        backStack.add(Routes.RegisterStep2(name, email, pass, fotoUrl))
                    },
                )
            }

            entry<Routes.RegisterStep2> { route ->
                RegisterStep2Screen(
                    route = route,
                    authViewModel = authViewModel,
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToHome = {
                        // The session listener in AppNavigation will handle the switch
                        // once authState.user is set and Firebase Auth state changes.
                    }
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

            entry<Routes.GooglePasswordSetup> {
                GooglePasswordSetupScreen(
                    authViewModel = authViewModel,
                    onBack = {
                        FirebaseAuth.getInstance().signOut()
                        backStack.clear()
                        backStack.add(Routes.Auth)
                    },
                    onNext = { password: String ->
                        backStack.add(Routes.GoogleAcademicSetup(password))
                    }
                )
            }

            entry<Routes.GoogleAcademicSetup> { route ->
                GoogleAcademicSetupScreen(
                    authViewModel = authViewModel,
                    passwordValue = route.password,
                    onBack = { backStack.removeLastOrNull() },
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
                        targetAsignaturaId = targetAsignaturaId,
                        onNotificationHandled = onNotificationHandled,
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()
                        },
                        onNavigateProfile = { backStack.add(Routes.Profile) }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            entry<Routes.Profile> {
                ProfileScreen(
                    usuario = currentUser,
                    onBack = { backStack.removeLastOrNull()},
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                    },
                    onProfileUpdated = {usuarioEditado ->
                        // Aquí actualizas el estado en el ViewModel que leen todas las pantallas
                        currentUser = usuarioEditado
                    }
                )
            }
        }
    )
}
