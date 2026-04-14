package samf.gestorestudiantil.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import samf.gestorestudiantil.domain.tabToRoute

@Composable
fun rememberHomeState(
    tabs: List<String>,
    rol: String,
    initialTab: String = tabs.firstOrNull() ?: ""
): HomeState {
    return remember(tabs, rol) {
        HomeState(tabs, rol, initialTab)
    }
}

class HomeState(
    val tabs: List<String>,
    val rol: String,
    initialTab: String
) {
    // Estado de la pestaña actual
    var currentTab by mutableStateOf(initialTab)
        private set

    // Mapa de backstacks por cada pestaña
    val tabBackStacks = tabs.associateWith { tab ->
        mutableStateListOf<Any>(tabToRoute(tab, rol))
    }

    // Obtener el backstack activo
    val currentBackStack: MutableList<Any>
        get() = tabBackStacks[currentTab] ?: mutableStateListOf()

    // Cambiar de pestaña
    fun switchTab(tab: String) {
        if (currentTab != tab) {
            currentTab = tab
        } else {
            // Si toca la misma pestaña, volvemos a la raíz (comportamiento estándar de Android)
            val stack = currentBackStack
            if (stack.size > 1) {
                val root = stack.first()
                stack.clear()
                stack.add(root)
            }
        }
    }

    // Navegar dentro de la pestaña actual
    fun navigate(route: Routes.HomeRoutes) {
        currentBackStack.add(route)
    }

    // Manejar el "Atrás"
    fun popBackStack(): Boolean {
        return if (currentBackStack.size > 1) {
            currentBackStack.removeAt(currentBackStack.size - 1)
            true
        } else {
            // Si estamos en la raíz y no es la primera pestaña, volvemos a la primera
            if (tabs.isNotEmpty() && currentTab != tabs.first()) {
                currentTab = tabs.first()
                true
            } else {
                false // Deja que el sistema cierre/minimice la app
            }
        }
    }
}
