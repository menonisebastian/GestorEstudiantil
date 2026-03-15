package samf.gestorestudiantil.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Definición de rutas Type-Safe
sealed interface Routes {
    @Serializable
    data object Auth : Routes, NavKey

    @Serializable
    data class RegisterStep2(
        val name: String,
        val email: String,
        val pass: String,
        val fotoUrl: String
    ) : Routes, NavKey

    @Serializable
    data object Pending : Routes, NavKey

    @Serializable
    data object GoogleSetup : Routes, NavKey

    @Serializable
    data object Home : Routes, NavKey

    @Serializable
    data object Profile : Routes, NavKey

    @Serializable
    data object Settings : Routes, NavKey
}
