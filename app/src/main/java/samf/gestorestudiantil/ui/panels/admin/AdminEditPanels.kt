package samf.gestorestudiantil.ui.panels.admin

import androidx.compose.runtime.Composable
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.screens.admin.EditAsignaturaScreen
import samf.gestorestudiantil.ui.screens.admin.EditCentroScreen
import samf.gestorestudiantil.ui.screens.admin.EditCursoScreen
import samf.gestorestudiantil.ui.screens.admin.EditUserScreen

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
