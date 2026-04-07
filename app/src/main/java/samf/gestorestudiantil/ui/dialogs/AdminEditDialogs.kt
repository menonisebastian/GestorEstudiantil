package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.domain.toComposeColor
import samf.gestorestudiantil.domain.toComposeIcon
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor

@Composable
fun EditCentroDialog(
    state: DialogState.EditCentro,
    onDismissRequest: () -> Unit
) {
    var nombre by remember { mutableStateOf(state.centro?.nombre ?: "") }
    var direccion by remember { mutableStateOf(state.centro?.direccion ?: "") }
    var tipo by remember { mutableStateOf(state.centro?.tipo ?: "") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (state.centro == null) "Añadir Centro" else "Editar Centro") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tipo,
                    onValueChange = { tipo = it },
                    label = { Text("Tipo (ej. Instituto de Educación Secundaria)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val centro = state.centro?.copy(nombre = nombre, direccion = direccion, tipo = tipo)
                    ?: Centro(nombre = nombre, direccion = direccion, tipo = tipo)
                state.onSave(centro)
                onDismissRequest()
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EditCursoDialog(
    state: DialogState.EditCurso,
    onDismissRequest: () -> Unit
) {
    var acronimo by remember { mutableStateOf(state.curso?.acronimo ?: "") }
    var nombre by remember { mutableStateOf(state.curso?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(state.curso?.descripcion ?: "") }
    var tipo by remember { mutableStateOf(state.curso?.tipo ?: "") }
    var modalidad by remember { mutableStateOf(state.curso?.modalidad ?: "presencial") }
    var urlInfo by remember { mutableStateOf(state.curso?.urlInfo ?: "") }
    var horasTotalesCurso by remember { mutableStateOf(state.curso?.horasTotalesCurso?.toString() ?: "0") }
    var iconoName by remember { mutableStateOf(state.curso?.iconoName ?: "School") }
    var colorFondoHex by remember { mutableStateOf(state.curso?.colorFondoHex ?: "#D0E1FF") }
    var colorIconoHex by remember { mutableStateOf(state.curso?.colorIconoHex ?: "#2563EB") }
    var turnosStr by remember { mutableStateOf(state.curso?.turnosDisponibles?.joinToString(", ") ?: "") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (state.curso == null) "Añadir Curso" else "Editar Curso") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = acronimo, onValueChange = { acronimo = it }, label = { Text("Acrónimo (ej. DAM)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tipo, onValueChange = { tipo = it }, label = { Text("Tipo (ej. FP Grado Superior)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = modalidad, onValueChange = { modalidad = it }, label = { Text("Modalidad (presencial/dual)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = urlInfo, onValueChange = { urlInfo = it }, label = { Text("URL Info") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = horasTotalesCurso, onValueChange = { if (it.all { c -> c.isDigit() }) horasTotalesCurso = it }, label = { Text("Horas Totales") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = turnosStr, onValueChange = { turnosStr = it }, label = { Text("Turnos (separados por coma)") }, modifier = Modifier.fillMaxWidth())

                IconPickerField(value = iconoName, onValueChange = { iconoName = it })
                ColorPickerField(label = "Color Fondo Hex", value = colorFondoHex, onValueChange = { colorFondoHex = it })
                ColorPickerField(label = "Color Icono Hex", value = colorIconoHex, onValueChange = { colorIconoHex = it })
            }
        },
        confirmButton = {
            Button(onClick = {
                val turnosList = turnosStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val curso = state.curso?.copy(
                    acronimo = acronimo, nombre = nombre, descripcion = descripcion, centroId = state.centroId, tipo = tipo,
                    modalidad = modalidad, urlInfo = urlInfo, horasTotalesCurso = horasTotalesCurso.toIntOrNull() ?: 0,
                    iconoName = iconoName, colorFondoHex = colorFondoHex, colorIconoHex = colorIconoHex,
                    turnosDisponibles = turnosList
                ) ?: Curso(
                    centroId = state.centroId, acronimo = acronimo, nombre = nombre, descripcion = descripcion, tipo = tipo,
                    modalidad = modalidad, urlInfo = urlInfo, horasTotalesCurso = horasTotalesCurso.toIntOrNull() ?: 0,
                    iconoName = iconoName, colorFondoHex = colorFondoHex, colorIconoHex = colorIconoHex,
                    turnosDisponibles = turnosList
                )
                state.onSave(curso)
                onDismissRequest()
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancelar") }
        }
    )
}

@Composable
fun EditAsignaturaDialog(
    state: DialogState.EditAsignatura,
    onDismissRequest: () -> Unit
) {
    var acronimo by remember { mutableStateOf(state.asignatura?.acronimo ?: "") }
    var nombre by remember { mutableStateOf(state.asignatura?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(state.asignatura?.descripcion ?: "") }
    var profesorId by remember { mutableStateOf(state.asignatura?.profesorId ?: "") }
    var ciclo by remember { mutableStateOf(state.asignatura?.ciclo ?: "1") }
    var cicloNum by remember { mutableStateOf(state.asignatura?.cicloNum?.toString() ?: "1") }
    var horasTotales by remember { mutableStateOf(state.asignatura?.horasTotales?.toString() ?: "0") }
    var horasSemanales by remember { mutableStateOf(state.asignatura?.horasSemanales?.toString() ?: "0") }
    var iconoName by remember { mutableStateOf(state.asignatura?.iconoName ?: "Class") }
    var colorFondoHex by remember { mutableStateOf(state.asignatura?.colorFondoHex ?: "#E8E8E8") }
    var colorIconoHex by remember { mutableStateOf(state.asignatura?.colorIconoHex ?: "#6B7280") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (state.asignatura == null) "Añadir Asignatura" else "Editar Asignatura") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = acronimo, onValueChange = { acronimo = it }, label = { Text("Acrónimo (ej. AD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = profesorId, onValueChange = { profesorId = it }, label = { Text("ID Profesor") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ciclo, onValueChange = { ciclo = it }, label = { Text("Ciclo (ej. 1, 2, único)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cicloNum, onValueChange = { if (it.all { c -> c.isDigit() }) cicloNum = it }, label = { Text("Número de Ciclo (1 o 2)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = horasTotales, onValueChange = { if (it.all { c -> c.isDigit() }) horasTotales = it }, label = { Text("Horas Totales") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = horasSemanales, onValueChange = { if (it.all { c -> c.isDigit() }) horasSemanales = it }, label = { Text("Horas Semanales") }, modifier = Modifier.fillMaxWidth())

                IconPickerField(value = iconoName, onValueChange = { iconoName = it })
                ColorPickerField(label = "Color Fondo Hex", value = colorFondoHex, onValueChange = { colorFondoHex = it })
                ColorPickerField(label = "Color Icono Hex", value = colorIconoHex, onValueChange = { colorIconoHex = it })
            }
        },
        confirmButton = {
            Button(onClick = {
                val asignatura = state.asignatura?.copy(
                    acronimo = acronimo, nombre = nombre, descripcion = descripcion, profesorId = profesorId,
                    cursoId = state.cursoId, centroId = state.centroId,
                    ciclo = ciclo, cicloNum = cicloNum.toIntOrNull() ?: 1, horasTotales = horasTotales.toIntOrNull() ?: 0,
                    horasSemanales = horasSemanales.toIntOrNull() ?: 0, iconoName = iconoName,
                    colorFondoHex = colorFondoHex, colorIconoHex = colorIconoHex
                ) ?: Asignatura(
                    cursoId = state.cursoId, centroId = state.centroId, acronimo = acronimo, nombre = nombre,
                    descripcion = descripcion, profesorId = profesorId, ciclo = ciclo, cicloNum = cicloNum.toIntOrNull() ?: 1,
                    horasTotales = horasTotales.toIntOrNull() ?: 0, horasSemanales = horasSemanales.toIntOrNull() ?: 0,
                    iconoName = iconoName, colorFondoHex = colorFondoHex, colorIconoHex = colorIconoHex
                )
                state.onSave(asignatura)
                onDismissRequest()
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancelar") }
        }
    )
}

@Composable
fun ColorPickerField(label: String, value: String, onValueChange: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(value.toComposeColor())
                .clickable { showDialog = true }
        )
    }

    if (showDialog) {
        SimpleColorPickerDialog(
            initialColor = value,
            onColorSelected = { 
                onValueChange(it)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun IconPickerField(value: String, onValueChange: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Icono Name") },
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { showDialog = true }) {
            Icon(
                imageVector = value.toComposeIcon(),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showDialog) {
        SimpleIconPickerDialog(
            onIconSelected = { 
                onValueChange(it)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun SimpleColorPickerDialog(
    initialColor: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // 1. Estado local para unificar el Grid y el Picker
    var currentColor by remember { mutableStateOf(initialColor.toComposeColor()) }

    val commonColors = listOf(
        "#D0E1FF", "#2563EB", "#DDE1FF", "#4338CA", "#D0F4FF", "#0891B2",
        "#FFE0F0", "#DB2777", "#FFFBD0", "#CA8A04", "#D0F5E1", "#16A34A",
        "#FFE8D8", "#EA580C", "#EDE0FF", "#7C3AED", "#FFD8D8", "#DC2626",
        "#E8E8E8", "#6B7280", "#000000", "#FFFFFF"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Color") },
        text = {
            // 2. Agrupamos todo en un Column
            Column(modifier = Modifier.fillMaxWidth()) {

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    // Limitamos la altura para que el diálogo no se desborde en pantallas pequeñas
                    modifier = Modifier.heightIn(max = 140.dp)
                ) {
                    items(commonColors) { hex ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(hex.toComposeColor())
                                .clickable {
                                    // Al hacer clic en el grid, actualizamos el estado y notificamos
                                    currentColor = hex.toComposeColor()
                                    //onColorSelected(hex)
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. El Color Picker de la librería
                ClassicColorPicker(
                    modifier = Modifier
                        .height(200.dp) // Altura reducida para que encaje bien en el Dialog
                        .fillMaxWidth(),
                    color = HsvColor.from(currentColor), // Lee del estado local
                    onColorChanged = { newHsvColor: HsvColor ->
                        // Al mover el picker, actualizamos el estado y notificamos
                        currentColor = newHsvColor.toColor()
                        //onColorSelected(newColor.toHex()) // <-- Sintaxis corregida
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Mostrar el código hexadecimal usando la variable de estado
                Text(
                    text = "Color Hex: ${currentColor.toHex()}",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        confirmButton = {
            TextButton(onClick ={
                onColorSelected(currentColor.toHex())
                onDismiss()
            }) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// --- Funciones de extensión requeridas ---

// Asegúrate de tener estas funciones en el mismo archivo o importadas

//fun String.toComposeColor(): Color {
//    return try {
//        Color(Color.parseColor(this))
//    } catch (e: Exception) {
//        Color.Black // Fallback en caso de que un código hex esté mal escrito
//    }
//}

fun Color.toHex(): String {
    val alpha = (this.alpha * 255).toInt()
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()

    // Si no necesitas el Alpha (transparencia), puedes cambiarlo a: "#%02X%02X%02X" pasándole red, green, blue
    return String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
}

@Composable
fun SimpleIconPickerDialog(
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val iconNames = listOf(
        "Code", "DataObject", "Computer", "Terminal", "CodeOff", "Build", 
        "PhoneIphone", "AppsOutlined", "LightbulbOutlined", "Brush",
        "LanguageOutlined", "Web", "DesktopWindows", "CloudOutlined", 
        "Palette", "DrawOutlined", "MergeTypeOutlined", "RouterOutlined", 
        "Wifi", "Dns", "Storage", "MemoryOutlined", "SettingsOutlined", 
        "LanOutlined", "CableOutlined", "ElectricalServices", "AllInbox", 
        "GridView", "Devices", "Engineering", "Security", "GppGoodOutlined", 
        "GppBadOutlined", "ShieldOutlined", "LockOutlined", "BugReportOutlined", 
        "SearchOutlined", "GavelOutlined", "PsychologyOutlined", "SmartToyOutlined", 
        "ModelTraining", "SportsEsports", "Vrpano", "CampaignOutlined", 
        "BarChartOutlined", "BroadcastOnHome", "RocketLaunchOutlined", 
        "SupportAgentOutlined", "PeopleOutlined", "AccountBalance", 
        "CalculateOutlined", "ReceiptOutlined", "DescriptionOutlined", 
        "FolderOpenOutlined", "BadgeOutlined", "BusinessCenter", "Laptop",
        "StorefrontOutlined", "ShoppingCartOutlined", "ShoppingBagOutlined", 
        "PointOfSaleOutlined", "SellOutlined", "Inventory2Outlined", 
        "LocalShippingOutlined", "PublicOutlined", "HandshakeOutlined", 
        "CreditCardOutlined", "TranslateOutlined", "WorkOutlineOutlined", 
        "ApartmentOutlined", "SchoolOutlined", "ScienceOutlined"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Icono") },
        text = {
            Box(modifier = Modifier.height(300.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(iconNames) { name ->
                        IconButton(onClick = { onIconSelected(name) }) {
                            Icon(
                                imageVector = name.toComposeIcon(),
                                contentDescription = name,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
