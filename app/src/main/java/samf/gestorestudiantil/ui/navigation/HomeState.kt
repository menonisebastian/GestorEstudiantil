package samf.gestorestudiantil.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.serialization.json.Json
import samf.gestorestudiantil.domain.utils.tabToRoute

@Composable
fun rememberHomeState(
    tabs: List<String>,
    rol: String,
): HomeState {
    return rememberSaveable(tabs, rol, saver = HomeState.saver(tabs, rol)) {
        HomeState(tabs, rol)
    }
}

class HomeState(
    tabs: List<String>,
    val rol: String,
    initialBackStacks: Map<String, List<Routes.HomeRoutes>>? = null,
) {
    // Backstacks por pestaña
    val tabBackStacks = tabs.associateWith { tab ->
        val initial = initialBackStacks?.get(tab) ?: listOf(tabToRoute(tab))
        mutableStateListOf<Routes.HomeRoutes>().apply { addAll(initial) }
    }

    fun getStack(tab: String?) = tabBackStacks[tab]

    fun navigate(tab: String, route: Routes.HomeRoutes) {
        val stack = getStack(tab)
        if (stack?.lastOrNull() != route) {
            stack?.add(route)
        }
    }

    fun pop(tab: String): Boolean {
        val stack = getStack(tab) ?: return false
        return if (stack.size > 1) {
            stack.removeAt(stack.size - 1)
            true
        } else {
            false
        }
    }

    // Volver a la raíz
    fun popToRoot(tab: String) {
        val stack = getStack(tab) ?: return
        if (stack.size > 1) {
            val root = stack.first()
            stack.clear()
            stack.add(root)
        }
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun saver(tabs: List<String>, rol: String) = Saver<HomeState, Map<String, List<String>>>(
            save = { state ->
                state.tabBackStacks.mapValues { entry ->
                    entry.value.map { json.encodeToString(Routes.HomeRoutes.serializer(), it) }
                }
            },
            restore = { savedValue ->
                val restoredStacks = try {
                    savedValue.mapValues { entry ->
                        entry.value.map { json.decodeFromString(Routes.HomeRoutes.serializer(), it) }
                    }
                } catch (_: Exception) {
                    null
                }
                HomeState(tabs, rol, restoredStacks)
            }
        )
    }
}
