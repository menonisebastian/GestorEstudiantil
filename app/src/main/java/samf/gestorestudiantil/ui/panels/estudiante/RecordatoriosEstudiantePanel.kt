package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.ui.components.CustomNotificationCard
import samf.gestorestudiantil.ui.components.CustomSearchBar
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AppViewModel

@Composable
fun RecordatoriosEstudiantePanel(
    recordatorios: List<Recordatorio>,
    paddingValues: PaddingValues,
    onOpenDialog: (DialogState) -> Unit,
    onDelete: (Recordatorio) -> Unit,
    onUpdate: (Recordatorio) -> Unit
)
{
    var textoBusqueda by remember { mutableStateOf("") }
    var filtroTipo by remember { mutableStateOf("") }
    //var showFilterDialog by remember { mutableStateOf(false) }


    val recordatoriosFiltrados = remember(textoBusqueda, filtroTipo, recordatorios) {
        val tiposSeleccionados = filtroTipo.split(",").filter { it.isNotEmpty() }
        recordatorios.filter {
            val coincideTexto = it.titulo.contains(textoBusqueda, ignoreCase = true) ||
                    it.descripcion.contains(textoBusqueda, ignoreCase = true)
            val coincideTipo = if (tiposSeleccionados.isEmpty()) true else {
                tiposSeleccionados.any { tipo -> it.tipo.name.equals(tipo, ignoreCase = true) }
            }
            
            coincideTexto && coincideTipo
        }
    }
    Column(modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Mis Recordatorios",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(top = 16.dp)
            )

            CustomSearchBar(
                textoBusqueda = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                onFilterClick = {
                    onOpenDialog(
                        DialogState.Filter(
                            tipo = "Recordatorio",
                            currentFilters = if (filtroTipo.isNotEmpty()) mapOf("tipo" to filtroTipo) else emptyMap(),
                            opcionesPersonalizadas = emptyMap(),
                            onApply = { seleccion -> filtroTipo = seleccion["tipo"] ?: "" }
                        )
                    )
                },
                filters = if (filtroTipo.isNotEmpty()) mapOf("tipo" to filtroTipo) else emptyMap(),
                onRemoveFilter = { keyPlusValue ->
                    if (keyPlusValue.contains(":")) {
                        val (_, newValue) = keyPlusValue.split(":")
                        filtroTipo = newValue
                    } else {
                        filtroTipo = ""
                    }
                }
            )

            if (recordatoriosFiltrados.isEmpty()) {
                Text(text = "No hay recordatorios", color = surfaceDimColor, fontSize = 16.sp)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(recordatoriosFiltrados) { recordatorio ->
                        CustomNotificationCard(
                            recordatorio = recordatorio,
                            onClick = {
                                onOpenDialog(
                                    DialogState.EditRecordatorio(
                                        recordatorio = recordatorio,
                                        onSave = onUpdate
                                    )
                                )
                            },
                            onDelete = { 
                                onDelete(recordatorio)
                            }
                        )
                    }
                }
            }
        }
    }
}