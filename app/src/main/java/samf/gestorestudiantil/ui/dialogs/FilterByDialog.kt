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
    tipo: String,
    onDismissRequest: () -> Unit
)
{
    var tipoUsuario by remember { mutableStateOf("") }
    val userOptions = listOf("Estudiante", "Admin", "Profesor")

    var tipoAsignatura by remember { mutableStateOf("") }
    val asignaturaOptions = listaAsignaturas.map { it.nombre }

    var tipoRecordatorios by remember { mutableStateOf("") }
    val recordatorioOptions = listOf("Examen", "Tarea", "Evento")

    var tipoCurso by remember { mutableStateOf("") }
    val cursoOptions = listaCursos.map { it.nombre }

    Dialog(onDismissRequest = onDismissRequest)
    {
        Column(
            modifier = Modifier.background(backgroundColor, RoundedCornerShape(16.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            Text("Filtrar búsqueda",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor)

            when(tipo)
            {
                "Usuario" ->
                CustomOptionsTextField(
                    texto = tipoUsuario,
                    label = "Tipo",
                    onValueChange = { tipoUsuario = it },
                    opciones = userOptions,
                    icon = Icons.Default.FormatListNumbered
                )

                "Asignatura" ->
                CustomOptionsTextField(
                    texto = tipoAsignatura,
                    label = "Asignatura",
                    onValueChange = { tipoAsignatura = it },
                    opciones = asignaturaOptions,
                    icon = Icons.Default.FormatListNumbered
                )

                "Recordatorio" ->
                CustomOptionsTextField(
                    texto = tipoRecordatorios,
                    label = "Tipo",
                    onValueChange = { tipoRecordatorios = it },
                    opciones = recordatorioOptions,
                    icon = Icons.Default.FormatListNumbered
                )

                "Estudiante" ->
                Column{
                    CustomOptionsTextField(
                        texto = tipoAsignatura,
                        label = "Asignatura",
                        onValueChange = { tipoAsignatura = it },
                        opciones = asignaturaOptions,
                        icon = Icons.Default.FormatListNumbered
                    )

                    CustomOptionsTextField(
                        texto = tipoCurso,
                        label = "Curso",
                        onValueChange = { tipoCurso = it },
                        opciones = cursoOptions,
                        icon = Icons.Default.FormatListNumbered
                    )
                }
            }

            // Botones de confirmar/cancelar
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

                        onDismissRequest()
                    }) { Text("Filtrar", color = textColor) }
            }
        }
    }
}