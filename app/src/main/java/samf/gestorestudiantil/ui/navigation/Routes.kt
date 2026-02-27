package samf.gestorestudiantil.ui.navigation

import kotlinx.serialization.Serializable

// Definición de rutas Type-Safe
sealed interface Routes {
    @Serializable
    data object Auth : Routes

    @Serializable
    data object Pending : Routes

    @Serializable
    data object GoogleSetup : Routes

    @Serializable
    data object Home : Routes

    @Serializable
    data object Profile : Routes

    @Serializable
    data object Settings : Routes
}
