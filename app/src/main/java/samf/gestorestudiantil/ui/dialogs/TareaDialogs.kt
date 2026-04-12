package samf.gestorestudiantil.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import samf.gestorestudiantil.data.models.Tarea
import samf.gestorestudiantil.ui.components.CustomTextField
import samf.gestorestudiantil.ui.components.CustomDateField
import samf.gestorestudiantil.ui.components.CustomTimeField
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTareaDialog(
    state: DialogState.AddTarea,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val tarea = state.tareaExistente

    var titulo by remember { mutableStateOf(tarea?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(tarea?.descripcion ?: "") }
    var visible by remember { mutableStateOf(tarea?.visible ?: true) }
    var fechaLimite by remember { mutableStateOf(tarea?.fechaLimiteEntrega?.toDate() ?: Date(System.currentTimeMillis() + 86400000)) }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf(tarea?.adjunto?.nombreArchivo ?: "") }
    var selectedFileSize by remember { mutableLongStateOf(tarea?.adjunto?.pesoBytes ?: 0L) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (nameIndex != -1 && c.moveToFirst()) {
                    selectedFileName = c.getString(nameIndex)
                }
                if (sizeIndex != -1) {
                    selectedFileSize = c.getLong(sizeIndex)
                }
            }
            if (selectedFileName.isEmpty()) {
                selectedFileName = it.lastPathSegment ?: "archivo"
            }
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = fechaLimite.time
    )
    var showDatePicker by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().apply { time = fechaLimite }.get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().apply { time = fechaLimite }.get(Calendar.MINUTE)
    )
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = it
                        val dateCal = Calendar.getInstance()
                        dateCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                        
                        val currentCal = Calendar.getInstance().apply { time = fechaLimite }
                        dateCal.set(Calendar.HOUR_OF_DAY, currentCal.get(Calendar.HOUR_OF_DAY))
                        dateCal.set(Calendar.MINUTE, currentCal.get(Calendar.MINUTE))
                        fechaLimite = dateCal.time
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val cal = Calendar.getInstance()
                    cal.time = fechaLimite
                    cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    cal.set(Calendar.MINUTE, timePickerState.minute)
                    fechaLimite = cal.time
                    showTimePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (tarea == null) "Nueva Tarea" else "Editar Tarea") },
        containerColor = backgroundColor,
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CustomTextField(value = titulo, onValueChange = { titulo = it }, label = "Título")
                CustomTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = "Instrucciones",
                    singleLine = false,
                    minLines = 3
                )

                // Selectores de Fecha y Hora
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomDateField(
                        value = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(fechaLimite),
                        label = "Fecha Límite",
                        onShowDatePicker = { showDatePicker = true }
                    )
                    CustomTimeField(
                        value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(fechaLimite),
                        label = "Hora Límite",
                        onShowTimePicker = { showTimePicker = true }
                    )
                }

                // Selector de Archivo
                OutlinedCard(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = backgroundColor)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Documento Adjunto (opcional)", style = MaterialTheme.typography.labelSmall, color = surfaceDimColor)
                            val displaySize = if (selectedFileSize > 0) {
                                val kb = selectedFileSize / 1024.0
                                if (kb > 1024) String.format(Locale.getDefault(), "%.2f MB", kb / 1024.0)
                                else String.format(Locale.getDefault(), "%.2f KB", kb)
                            } else ""
                            
                            Text(
                                text = if (selectedFileName.isEmpty()) "Seleccionar archivo..." 
                                       else if (displaySize.isNotEmpty()) "$selectedFileName ($displaySize)"
                                       else selectedFileName,
                                color = if (selectedFileName.isEmpty()) surfaceDimColor else textColor,
                                maxLines = 1
                            )
                        }
                        if (selectedFileName.isNotEmpty() && selectedFileUri != null) {
                            IconButton(onClick = {
                                selectedFileUri = null
                                selectedFileName = tarea?.adjunto?.nombreArchivo ?: ""
                                selectedFileSize = tarea?.adjunto?.pesoBytes ?: 0L
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { visible = !visible },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Visible para estudiantes")
                    Switch(checked = visible, onCheckedChange = { visible = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nuevaTarea = (tarea ?: Tarea()).copy(
                        titulo = titulo,
                        descripcion = descripcion,
                        asignaturaId = state.asignaturaId,
                        unidadId = state.unidadId,
                        profesorId = state.tareaExistente?.profesorId ?: "",
                        centroId = state.tareaExistente?.centroId ?: "",
                        fechaLimiteEntrega = Timestamp(fechaLimite),
                        visible = visible
                    )

                    var fileData: ByteArray? = null
                    var mimeType: String? = null
                    try {
                        selectedFileUri?.let { uri ->
                            fileData = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            mimeType = context.contentResolver.getType(uri)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        android.widget.Toast.makeText(context, "Error al leer el archivo: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                    }

                    state.onSave(nuevaTarea, fileData, selectedFileName, mimeType)
                    onDismissRequest()
                },
                enabled = titulo.isNotBlank()
            ) {
                Text(if (tarea == null) "Crear" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}
