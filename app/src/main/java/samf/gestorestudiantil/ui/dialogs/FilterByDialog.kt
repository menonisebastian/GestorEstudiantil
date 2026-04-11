package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor

@OptIn(ExperimentalLayoutApi::class)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Filtrar búsqueda",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )

            // Filtro Principal (Depende del contexto)
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
                FilterChipGroup(
                    label = labelPrincipal,
                    options = opcionesPrincipales,
                    selectedOptions = tempFilters[keyPrincipal]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList(),
                    onSelectionChanged = { options ->
                        val newValue = options.joinToString(",")
                        tempFilters = if (newValue.isEmpty()) tempFilters - keyPrincipal else tempFilters + (keyPrincipal to newValue)
                    }
                )
            }

            // Filtros adicionales para Usuarios / Calificaciones
            if (state.tipo == "Usuario" || state.tipo == "Calificaciones") {
                if (cursoOptions.isNotEmpty()) {
                    FilterChipGroup(
                        label = "Curso",
                        options = cursoOptions,
                        selectedOptions = tempFilters["curso"]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList(),
                        onSelectionChanged = { options ->
                            val newValue = options.joinToString(",")
                            tempFilters = if (newValue.isEmpty()) tempFilters - "curso" else tempFilters + ("curso" to newValue)
                        }
                    )
                }

                if (state.tipo == "Usuario") {
                    FilterChipGroup(
                        label = "Ciclo",
                        options = cicloOptions,
                        selectedOptions = tempFilters["ciclo"]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList(),
                        onSelectionChanged = { options ->
                            val newValue = options.joinToString(",")
                            tempFilters = if (newValue.isEmpty()) tempFilters - "ciclo" else tempFilters + ("ciclo" to newValue)
                        }
                    )
                }
                
                if (state.tipo == "Calificaciones" && asignaturaOptions.isNotEmpty()) {
                    FilterChipGroup(
                        label = "Asignatura",
                        options = asignaturaOptions,
                        selectedOptions = tempFilters["asignatura"]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList(),
                        onSelectionChanged = { options ->
                            val newValue = options.joinToString(",")
                            tempFilters = if (newValue.isEmpty()) tempFilters - "asignatura" else tempFilters + ("asignatura" to newValue)
                        }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterChipGroup(
    label: String,
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChanged: (List<String>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = surfaceDimColor
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { option ->
                val isSelected = selectedOptions.contains(option)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newSelection = if (isSelected) {
                            selectedOptions - option
                        } else {
                            selectedOptions + option
                        }
                        onSelectionChanged(newSelection)
                    },
                    label = { Text(option) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = surfaceColor,
                        labelColor = textColor,
                        selectedContainerColor = primaryColor,
                        selectedLabelColor = textColor
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color.Transparent,
                        selectedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}
