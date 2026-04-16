package samf.gestorestudiantil.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import samf.gestorestudiantil.domain.tabToRoute

@Composable
fun rememberHomeState(
    tabs: List<String>,
    rol: String
): HomeState {
    return remember(tabs, rol) {
        HomeState(tabs, rol)
    }
}

class HomeState(
    val tabs: List<String>,
    val rol: String
) {
    // Mapa de backstacks por cada pestaña
    val tabBackStacks = tabs.associateWith { tab ->
        mutableStateListOf<Any>(tabToRoute(tab, rol))
    }

    // Obtener el backstack de una pestaña específica
    fun getStack(tab: String?) = tabBackStacks[tab]

    // Navegar dentro de una pestaña específica
    fun navigate(tab: String, route: Routes.HomeRoutes) {
        val stack = getStack(tab)
        // Evitamos agregar la misma ruta consecutivamente
        if (stack?.lastOrNull() != route) {
            stack?.add(route)
        }
    }

    // Hacer Pop en una pestaña específica
    fun pop(tab: String): Boolean {
        val stack = getStack(tab) ?: return false
        return if (stack.size > 1) {
            stack.removeAt(stack.size - 1)
            true
        } else {
            false
        }
    }

    // Volver a la raíz de una pestaña (útil cuando el usuario toca el BottomNav 2 veces)
    fun popToRoot(tab: String) {
        val stack = getStack(tab) ?: return
        if (stack.size > 1) {
            val root = stack.first()
            stack.clear()
            stack.add(root)
        }
    }
}
