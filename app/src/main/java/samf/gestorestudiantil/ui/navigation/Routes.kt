package samf.gestorestudiantil.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.User

// Definición de rutas Type-Safe
sealed interface Routes {
    @Serializable
    data object Auth : Routes, NavKey

    sealed interface AuthRoutes : NavKey {
        @Serializable
        data object Login : AuthRoutes

        @Serializable
        data object Register : AuthRoutes
    }

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
    data object GooglePasswordSetup : Routes, NavKey

    @Serializable
    data class GoogleAcademicSetup(val password: String) : Routes, NavKey

    @Serializable
    data object Home : Routes, NavKey

    // Sub-rutas de HomeScreen
    sealed interface HomeRoutes : NavKey {
        @Serializable
        data object Materias : HomeRoutes

        @Serializable
        data class MateriaDetalle(
            val asignatura: Asignatura
        ) : HomeRoutes

        @Serializable
        data object Horarios : HomeRoutes

        @Serializable
        data object Calificaciones : HomeRoutes

        // Routes.kt
        @Serializable
        data class CalificacionesDetalle(
            val asignatura: Asignatura  // objeto completo
        ) : HomeRoutes

        // TODO : Cambiar a Firebase

//        @Serializable
//        data class CalificacionesDetalle(
//            val asignaturaId: String,
//            val asignaturaNombre: String
//        ) : HomeRoutes

        @Serializable
        data object Recordatorios : HomeRoutes

        // Admin
        @Serializable
        data object Usuarios : HomeRoutes

        @Serializable
        data object Centros : HomeRoutes

        @Serializable
        data class EditCentro(val centro: Centro? = null) : HomeRoutes

        @Serializable
        data class EditCurso(val curso: Curso? = null, val centroId: String) : HomeRoutes

        @Serializable
        data class EditAsignatura(val asignatura: Asignatura? = null, val cursoId: String, val centroId: String) : HomeRoutes

        @Serializable
        data class EditUser(val user: User) : HomeRoutes
    }

    @Serializable
    data object Profile : Routes, NavKey

    @Serializable
    data object Settings : Routes, NavKey
}
