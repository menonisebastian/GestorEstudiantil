package samf.gestorestudiantil.ui.panels.admin

import androidx.compose.runtime.Composable
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.screens.EditAsignaturaScreen
import samf.gestorestudiantil.ui.screens.EditCentroScreen
import samf.gestorestudiantil.ui.screens.EditCursoScreen
import samf.gestorestudiantil.ui.screens.EditUserScreen

@Composable
fun EditCentroPanel(
    state: DialogState.EditCentro,
    onBack: () -> Unit
) {
    EditCentroScreen(state = state, onBack = onBack)
}

@Composable
fun EditCursoPanel(
    state: DialogState.EditCurso,
    onBack: () -> Unit
) {
    EditCursoScreen(state = state, onBack = onBack)
}

@Composable
fun EditAsignaturaPanel(
    state: DialogState.EditAsignatura,
    onBack: () -> Unit
) {
    EditAsignaturaScreen(state = state, onBack = onBack)
}

@Composable
fun EditUserPanel(
    state: DialogState.EditUser,
    onBack: () -> Unit
) {
    EditUserScreen(state = state, onBack = onBack)
}
