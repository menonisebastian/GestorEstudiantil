package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import samf.gestorestudiantil.ui.components.CustomOptionsTextField
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun FilterByDialog(
    state: DialogState.Filter,
    onDismissRequest: () -> Unit
) {
    // Estado local para manejar múltiples selecciones antes de aplicar
    var tempFilters by remember { mutableStateOf(state.currentFilters) }

    // Opciones estáticas básicas
    val userOptions = listOf("Estudiante", "Admin", "Profesor")
    val recordatorioOptions = listOf("Examen", "Tarea", "Evento")
    val cicloOptions = listOf("1", "2")

    // Opciones dinámicas pasadas desde el ViewModel
    val cursoOptions = state.opcionesPersonalizadas["cursos"] ?: emptyList()
    val asignaturaOptions = state.opcionesPersonalizadas["asignaturas"] ?: emptyList()

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .background(backgroundColor, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Filtrar búsqueda",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )

            // Filtro de Rol/Tipo (Depende del contexto)
            val labelPrincipal = when(state.tipo) {
                "Usuario" -> "Tipo de Usuario"
                "Recordatorio" -> "Tipo de Recordatorio"
                "Asignatura" -> "Asignatura"
                else -> "Categoría"
            }
            val opcionesPrincipales = when(state.tipo) {
                "Usuario" -> userOptions
                "Recordatorio" -> recordatorioOptions
                "Asignatura" -> asignaturaOptions
                else -> emptyList()
            }
            val keyPrincipal = when(state.tipo) {
                "Recordatorio" -> "tipo"
                "Asignatura" -> "asignatura"
                else -> "rol"
            }

            if (opcionesPrincipales.isNotEmpty()) {
                CustomOptionsTextField(
                    texto = tempFilters[keyPrincipal] ?: "",
                    label = labelPrincipal,
                    onValueChange = { tempFilters = tempFilters + (keyPrincipal to it) },
                    opciones = opcionesPrincipales,
                    icon = Icons.Default.FormatListNumbered
                )
            }

            // Filtros adicionales para Usuarios
            if (state.tipo == "Usuario" || state.tipo == "Calificaciones") {
                CustomOptionsTextField(
                    texto = tempFilters["curso"] ?: "",
                    label = "Curso",
                    onValueChange = { tempFilters = tempFilters + ("curso" to it) },
                    opciones = cursoOptions,
                    icon = Icons.Default.FormatListNumbered
                )

                if (state.tipo == "Usuario") {
                    CustomOptionsTextField(
                        texto = tempFilters["ciclo"] ?: "",
                        label = "Ciclo",
                        onValueChange = { tempFilters = tempFilters + ("ciclo" to it) },
                        opciones = cicloOptions,
                        icon = Icons.Default.FormatListNumbered
                    )
                }
                
                if (state.tipo == "Calificaciones" && asignaturaOptions.isNotEmpty()) {
                    CustomOptionsTextField(
                        texto = tempFilters["asignatura"] ?: "",
                        label = "Asignatura",
                        onValueChange = { tempFilters = tempFilters + ("asignatura" to it) },
                        opciones = asignaturaOptions,
                        icon = Icons.Default.FormatListNumbered
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = { tempFilters = emptyMap() },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = primaryColor)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Limpiar filtros")
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDismissRequest,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = surfaceDimColor)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar")
                }

                IconButton(
                    onClick = {
                        state.onApply(tempFilters)
                        onDismissRequest()
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = primaryColor,
                        contentColor = textColor
                    )
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Aplicar")
                }
            }
        }
    }
}
