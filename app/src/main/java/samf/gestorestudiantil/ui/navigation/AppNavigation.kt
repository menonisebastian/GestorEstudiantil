package samf.gestorestudiantil.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.google.firebase.auth.FirebaseAuth
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
    val authState by authViewModel.authState.collectAsState()
    
    val currentUser = authState.user
    val isCheckingSession = authState.isCheckingSession
    val needsGoogleSetup = authState.requireGooglePasswordSetup

    val backStack = remember { mutableStateListOf<Any>() }
    
    // 2. GESTIÓN DE NAVEGACIÓN AUTOMÁTICA
    LaunchedEffect(currentUser, isCheckingSession, needsGoogleSetup) {
        if (isCheckingSession) return@LaunchedEffect

        val currentDestination = backStack.lastOrNull()
        
        if (currentUser == null && !needsGoogleSetup) {
            // Caso: No logueado -> Ir a Auth
            if (currentDestination !is Routes.Auth && currentDestination !is Routes.RegisterStep2) {
                backStack.clear()
                backStack.add(Routes.Auth)
            }
        } else {
            // Caso: Logueado -> Decidir destino
            if (needsGoogleSetup) {
                if (currentDestination !is Routes.GooglePasswordSetup && currentDestination !is Routes.GoogleAcademicSetup) {
                    backStack.clear()
                    backStack.add(Routes.GooglePasswordSetup)
                }
            } else if (currentUser != null) {
                val targetDestination = if (currentUser.estado == "PENDIENTE") {
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
                    onRequireGoogleSetup = { /* El listener maneja esto */ },
                    onNavigateToRegisterStep2 = { name, email, pass, imgUrl ->
                        backStack.add(Routes.RegisterStep2(name, email, pass, imgUrl))
                    },
                )
            }

            entry<Routes.RegisterStep2> { route ->
                RegisterStep2Screen(
                    route = route,
                    authViewModel = authViewModel,
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToHome = {
                        // El listener de sesión en el ViewModel manejará el cambio
                    }
                )
            }

            entry<Routes.Pending> {
                currentUser?.let { user ->
                    PendingApprovalScreen(
                        usuario = user,
                        onLogout = { authViewModel.signOut() }
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
                    onSetupComplete = { _ ->
                        // El listener de sesión en el ViewModel manejará el cambio
                    }
                )
            }

            entry<Routes.Home> {
                currentUser?.let { user ->
                    HomeScreen(
                        usuario = user,
                        targetAsignaturaId = targetAsignaturaId,
                        onNotificationHandled = onNotificationHandled,
                        onLogout = {
                            authViewModel.signOut()
                        }
                    )
                } ?: run {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            entry<Routes.Profile> {
                ProfileScreen(
                    usuario = currentUser,
                    onLogout = {
                        authViewModel.signOut()
                    },
                    onProfileUpdated = { _ ->
                        // El listener de sesión en el ViewModel actualizará el estado automáticamente
                    }
                )
            }
        }
    )

    AnimatedVisibility(
        visible = authState.isLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
