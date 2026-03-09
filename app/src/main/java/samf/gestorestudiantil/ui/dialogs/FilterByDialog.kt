package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import samf.gestorestudiantil.data.models.listaAsignaturas
import samf.gestorestudiantil.data.models.listaCursos
import samf.gestorestudiantil.ui.components.CustomOptionsTextField
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun FilterByDialog(
    state: DialogState.Filter, // <-- CAMBIO AQUÍ
    onDismissRequest: () -> Unit
) {
    // Variables locales para manejar la selección temporal
    var seleccionActual by remember { mutableStateOf("") }

    // Datos estáticos (podrías pasarlos en el state si quisieras hacerlos dinámicos en el futuro)
    val userOptions = listOf("Estudiante", "Admin", "Profesor")
    val asignaturaOptions = listaAsignaturas.map { it.nombre }
    val recordatorioOptions = listOf("Examen", "Tarea", "Evento")
    val cursoOptions = listaCursos.map { it.nombre }

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

            // Lógica simplificada usando el state.tipo
            when (state.tipo) {
                "Usuario" -> CustomOptionsTextField(
                    texto = seleccionActual,
                    label = "Tipo de Usuario",
                    onValueChange = { seleccionActual = it },
                    opciones = userOptions,
                    icon = Icons.Default.FormatListNumbered
                )
                "Asignatura" -> CustomOptionsTextField(
                    texto = seleccionActual,
                    label = "Asignatura",
                    onValueChange = { seleccionActual = it },
                    opciones = asignaturaOptions,
                    icon = Icons.Default.FormatListNumbered
                )
                "Recordatorio" -> CustomOptionsTextField(
                    texto = seleccionActual,
                    label = "Tipo de Recordatorio",
                    onValueChange = { seleccionActual = it },
                    opciones = recordatorioOptions,
                    icon = Icons.Default.FormatListNumbered
                )
                // Puedes agregar más casos aquí
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismissRequest) { Text("Cancelar") }
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    onClick = {
                        state.onApply(seleccionActual) // Devolvemos el valor
                        onDismissRequest()
                    }
                ) { Text("Filtrar", color = textColor) }
            }
        }
    }
}