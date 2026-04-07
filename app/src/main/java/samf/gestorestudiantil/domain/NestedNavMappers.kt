package samf.gestorestudiantil.domain

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

fun tabToRoute(tab: String, rol: String): Routes.HomeRoutes = when (tab) {
    "Materias"        -> Routes.HomeRoutes.Materias
    "Horarios"        -> Routes.HomeRoutes.Horarios
    "Calificaciones"  -> Routes.HomeRoutes.Calificaciones
    "Recordatorios",
    "Notificaciones"  -> Routes.HomeRoutes.Recordatorios
    "Usuarios"        -> Routes.HomeRoutes.Usuarios
    "Centros"         -> Routes.HomeRoutes.Centros
    "EditCentro"      -> Routes.HomeRoutes.EditCentro()
    "EditCurso"       -> Routes.HomeRoutes.EditCurso(null, "")
    "EditAsignatura"  -> Routes.HomeRoutes.EditAsignatura(null, "", "")
    "EditUser"        -> Routes.HomeRoutes.EditUser(samf.gestorestudiantil.data.models.User())
    else              -> Routes.HomeRoutes.Materias
}

// Convierte un NavKey al nombre del tab
fun routeToTab(route: Any, rol: String): String = when (route) {
    is Routes.HomeRoutes.Materias        -> "Materias"
    is Routes.HomeRoutes.Horarios        -> "Horarios"
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

// Devuelve true si es una ruta de detalle (no un tab raíz)
fun isDetailRoute(route: Any): Boolean =
    route is Routes.HomeRoutes.CalificacionesDetalle ||
    route is Routes.HomeRoutes.EditCentro ||
    route is Routes.HomeRoutes.EditCurso ||
    route is Routes.HomeRoutes.EditAsignatura ||
    route is Routes.HomeRoutes.EditUser


//## Resumen visual de la arquitectura
//
//AppNavigation (NavDisplay raíz)
//├── Routes.Auth       → AuthScreen (con su propio HorizontalPager)
//├── Routes.Home       → HomeScreen
//│   └── HorizontalPager
//│       ├── Página 0 → NavDisplay(pageBackStack) → Materias
//│       ├── Página 1 → NavDisplay(pageBackStack) → Horarios
//│       ├── Página 2 → NavDisplay(pageBackStack) → Calificaciones
//│       │                                        ↘ CalificacionesDetalle ✅
//│       └── Página 3 → NavDisplay(pageBackStack) → Recordatorios
//├── Routes.Profile    → ProfileScreen
//└── Routes.Settings   → SettingsScreen