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
import samf.gestorestudiantil.ui.screens.AuthScreen
import samf.gestorestudiantil.ui.screens.GoogleAcademicSetupScreen
import samf.gestorestudiantil.ui.screens.GooglePasswordSetupScreen
import samf.gestorestudiantil.ui.screens.HomeScreen
import samf.gestorestudiantil.ui.screens.PendingApprovalScreen
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
    
    val rootRoute = when {
        authState.isCheckingSession -> null
        authState.user == null && !authState.requireGooglePasswordSetup -> Routes.Auth
        authState.requireGooglePasswordSetup -> Routes.GooglePasswordSetup
        authState.user?.estado == "PENDIENTE" -> Routes.Pending
        else -> Routes.Home
    }

    val backStack = remember { mutableStateListOf<Any>() }

    LaunchedEffect(rootRoute) {
        if (rootRoute != null) {
            val currentRoot = backStack.firstOrNull()
            
            if (currentRoot != rootRoute) {
                backStack.clear()
                backStack.add(rootRoute)
            }
        }
    }

    if (rootRoute == null || backStack.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val currentUser = authState.user

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            (slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(150)
            ) + fadeIn(animationSpec = tween(150))) togetherWith
                    (slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(150)
                    ) + fadeOut(animationSpec = tween(150)))
        },
        popTransitionSpec = {
            (slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(150)
            ) + fadeIn(animationSpec = tween(150))) togetherWith
                    (slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(150)
                    ) + fadeOut(animationSpec = tween(150)))
        },
        entryProvider = entryProvider {
            entry<Routes.Auth> {
                AuthScreen(
                    authViewModel = authViewModel,
                    darkTheme = darkTheme,
                    onAuthSuccess = { },
                    onRequireGoogleSetup = { },
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
                        authViewModel.signOut()
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
