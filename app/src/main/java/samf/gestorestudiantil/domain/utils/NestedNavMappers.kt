package samf.gestorestudiantil.domain.utils

import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.navigation.Routes

// Convierte el nombre del tab al NavKey correspondiente

// Helpers
fun authTabToRoute(tab: String): Routes.AuthRoutes = when (tab) {
    "Registrarse" -> Routes.AuthRoutes.Register
    else          -> Routes.AuthRoutes.Login
}

fun authRouteToTab(route: Any): String = when (route) {
    is Routes.AuthRoutes.Register -> "Registrarse"
    else                          -> "Ingresar"
}

fun tabToRoute(tab: String): Routes.HomeRoutes = when (tab) {
    "Asignaturas"     -> Routes.HomeRoutes.Materias
    "Horarios"        -> Routes.HomeRoutes.Horarios
    "Calendario"      -> Routes.HomeRoutes.Calendario
    "Calificaciones"  -> Routes.HomeRoutes.Calificaciones
    "Recordatorios",
    "Notificaciones"  -> Routes.HomeRoutes.Recordatorios
    "Usuarios"        -> Routes.HomeRoutes.Usuarios
    "Centros"         -> Routes.HomeRoutes.Centros
    "EditCentro"      -> Routes.HomeRoutes.EditCentro()
    "EditCurso"       -> Routes.HomeRoutes.EditCurso(null, "")
    "EditAsignatura"  -> Routes.HomeRoutes.EditAsignatura(null, "", "")
    "EditUser"        -> Routes.HomeRoutes.EditUser(User.Incompleto())
    "Perfil"          -> Routes.HomeRoutes.Perfil
    else              -> Routes.HomeRoutes.Materias
}

fun routeToTab(route: Any, rol: String): String = when (route) {
    is Routes.HomeRoutes.Materias        -> "Asignaturas"
    is Routes.HomeRoutes.Horarios        -> "Horarios"
    is Routes.HomeRoutes.Calendario      -> "Calendario"
    is Routes.HomeRoutes.Calificaciones  -> "Calificaciones"
    is Routes.HomeRoutes.Recordatorios   -> "Recordatorios"
    is Routes.HomeRoutes.Usuarios        -> "Usuarios"
    is Routes.HomeRoutes.Centros         -> "Centros"
    is Routes.HomeRoutes.EditCentro      -> "EditCentro"
    is Routes.HomeRoutes.EditCurso       -> "EditCurso"
    is Routes.HomeRoutes.EditAsignatura  -> "EditAsignatura"
    is Routes.HomeRoutes.EditUser        -> "EditUser"
    else                                  -> ""
}

fun isDetailRoute(route: Any): Boolean =
    route is Routes.HomeRoutes.CalificacionesDetalle ||
    route is Routes.HomeRoutes.MateriaDetalle ||
    route is Routes.HomeRoutes.EstudiantesAsignatura ||
    route is Routes.HomeRoutes.CalificacionesEstudianteDetalle ||
    route is Routes.HomeRoutes.EditCentro ||
    route is Routes.HomeRoutes.EditCurso ||
    route is Routes.HomeRoutes.EditAsignatura ||
    route is Routes.HomeRoutes.EditUser ||
    route is Routes.HomeRoutes.AdminTiposCurso ||
    route is Routes.HomeRoutes.AdminCursos ||
    route is Routes.HomeRoutes.AdminTurnos ||
    route is Routes.HomeRoutes.AdminCiclos ||
    route is Routes.HomeRoutes.AdminAsignaturas ||
    route is Routes.HomeRoutes.AdminHorarios
