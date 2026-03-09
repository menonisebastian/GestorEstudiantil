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
import samf.gestorestudiantil.data.models.listaRecordatorios
import samf.gestorestudiantil.ui.components.CustomNotificationCard
import samf.gestorestudiantil.ui.components.CustomSearchBar
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun RecordatoriosEstudiantePanel(
    paddingValues: PaddingValues,
    onOpenDialog: (DialogState) -> Unit
)
{
    var textoBusqueda by remember { mutableStateOf("") }
    //var showFilterDialog by remember { mutableStateOf(false) }


    val recordatoriosFiltrados = remember(textoBusqueda) {
        if (textoBusqueda.isBlank())
            listaRecordatorios
        else listaRecordatorios.filter { it.titulo.contains(textoBusqueda, ignoreCase = true) }
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
                textoBusqueda,
                onValueChange = { textoBusqueda = it },
                onFilterClick = {
                    // ABRIR DIÁLOGO DE FILTRO
                    onOpenDialog(
                        DialogState.Filter(
                            tipo = "Recordatorio",
                            onApply = { filtroSeleccionado ->
                                // Aquí implementarías la lógica real de filtrado
                                // Por ahora solo actualizamos la búsqueda como ejemplo
                                textoBusqueda = filtroSeleccionado
                            }
                        )
                    )
                }
            )

            if (recordatoriosFiltrados.isEmpty()) {
                Text(text = "No hay recordatorios", color = surfaceDimColor, fontSize = 16.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(recordatoriosFiltrados) { notificacion ->
                        CustomNotificationCard(notificacion)
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}