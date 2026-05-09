package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import samf.gestorestudiantil.R
import samf.gestorestudiantil.ui.theme.GestorEstudiantilTheme
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.secondaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.tertiaryColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.theme.whiteColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterByBottomSheet(
    state: DialogState.Filter,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = backgroundColor
    ) {
        FilterByContent(
            state = state,
            onDismissRequest = onDismissRequest,
            modifier = Modifier
                .padding(16.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterByContent(
    state: DialogState.Filter,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempFilters by remember { mutableStateOf(state.currentFilters) }

    val userOptions = listOf("Estudiante", "Admin", "Profesor")
    val recordatorioOptions = listOf("Examen", "Tarea", "Evento")
    val cicloOptions = listOf("1", "2")
    val turnoOptions = listOf("Matutino", "Vespertino")

    val cursoOptions = state.opcionesPersonalizadas["cursos"] ?: emptyList()
    val asignaturaOptions = state.opcionesPersonalizadas["asignaturas"] ?: emptyList()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.filter_search_title),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )

        val labelPrincipal = when (state.tipo) {
            "Usuario" -> "Tipo de Usuario"
            "Recordatorio" -> "Tipo de Recordatorio"
            "Asignatura" -> stringResource(R.string.label_subjects)
            "Asistencia" -> "Estado de Asistencia"
            else -> "Categoría"
        }
        val opcionesPrincipales = when (state.tipo) {
            "Usuario" -> userOptions
            "Recordatorio" -> recordatorioOptions
            "Asignatura" -> asignaturaOptions
            "Asistencia" -> state.opcionesPersonalizadas["estados"] ?: emptyList()
            else -> emptyList()
        }
        val keyPrincipal = when (state.tipo) {
            "Recordatorio" -> "tipo"
            "Asignatura" -> "asignatura"
            "Asistencia" -> "estado"
            else -> "rol"
        }

        if (opcionesPrincipales.isNotEmpty()) {
            FilterChipGroup(
                label = labelPrincipal,
                options = opcionesPrincipales,
                selectedOptions = tempFilters[keyPrincipal]?.split(",")?.filter { it.isNotEmpty() }
                    ?: emptyList(),
                onSelectionChanged = { options ->
                    val newValue = options.joinToString(",")
                    tempFilters =
                        if (newValue.isEmpty()) tempFilters - keyPrincipal else tempFilters + (keyPrincipal to newValue)
                }
            )
        }

        if (state.tipo == "Usuario" || state.tipo == "Calificaciones" || state.tipo == "Asignatura" || state.tipo == "Asistencia") {
            if (cursoOptions.isNotEmpty()) {
                FilterChipGroup(
                    label = stringResource(R.string.label_course),
                    options = cursoOptions,
                    selectedOptions = tempFilters["curso"]?.split(",")?.filter { it.isNotEmpty() }
                        ?: emptyList(),
                    onSelectionChanged = { options ->
                        val newValue = options.joinToString(",")
                        tempFilters =
                            if (newValue.isEmpty()) tempFilters - "curso" else tempFilters + ("curso" to newValue)
                    }
                )
            }

            if (state.tipo == "Usuario" || state.tipo == "Asignatura") {
                FilterChipGroup(
                    label = stringResource(R.string.label_cycle),
                    options = cicloOptions,
                    selectedOptions = tempFilters["ciclo"]?.split(",")?.filter { it.isNotEmpty() }
                        ?: emptyList(),
                    onSelectionChanged = { options ->
                        val newValue = options.joinToString(",")
                        tempFilters =
                            if (newValue.isEmpty()) tempFilters - "ciclo" else tempFilters + ("ciclo" to newValue)
                    }
                )

                FilterChipGroup(
                    label = stringResource(R.string.label_shift),
                    options = turnoOptions,
                    selectedOptions = tempFilters["turno"]?.split(",")?.filter { it.isNotEmpty() }
                        ?.map { opt -> opt.replaceFirstChar { it.uppercase() } } ?: emptyList(),
                    onSelectionChanged = { options ->
                        val newValue = options.joinToString(",") { it.lowercase() }
                        tempFilters =
                            if (newValue.isEmpty()) tempFilters - "turno" else tempFilters + ("turno" to newValue)
                    }
                )
            }

            if ((state.tipo == "Calificaciones" || state.tipo == "Asistencia") && asignaturaOptions.isNotEmpty()) {
                FilterChipGroup(
                    label = stringResource(R.string.label_subjects),
                    options = asignaturaOptions,
                    selectedOptions = tempFilters["asignatura"]?.split(",")?.filter { it.isNotEmpty() }
                        ?: emptyList(),
                    onSelectionChanged = { options ->
                        val newValue = options.joinToString(",")
                        tempFilters =
                            if (newValue.isEmpty()) tempFilters - "asignatura" else tempFilters + ("asignatura" to newValue)
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
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = tertiaryColor.copy(alpha = 0.15f),
                    contentColor = tertiaryColor
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.action_clear_filters))
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onDismissRequest,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = secondaryColor.copy(alpha = 0.15f),
                    contentColor = secondaryColor
                )
            ) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.label_cancel))
            }

            Button(
                onClick = {
                    state.onApply(tempFilters)
                    onDismissRequest()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = whiteColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.action_apply))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilterByContentPreview() {
    GestorEstudiantilTheme {
        FilterByContent(
            state = DialogState.Filter(
                tipo = "Usuario",
                opcionesPersonalizadas = mapOf(
                    "cursos" to listOf("1º ESO A", "2º ESO B"),
                    "asignaturas" to listOf("Matemáticas", "Lengua")
                ),
                onApply = {}
            ),
            onDismissRequest = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FilterByContentRecordatorioPreview() {
    GestorEstudiantilTheme {
        FilterByContent(
            state = DialogState.Filter(
                tipo = "Recordatorio",
                onApply = {}
            ),
            onDismissRequest = {},
            modifier = Modifier.padding(16.dp)
        )
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
                        labelColor = secondaryColor,
                        selectedContainerColor = primaryColor,
                        selectedLabelColor = whiteColor
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
