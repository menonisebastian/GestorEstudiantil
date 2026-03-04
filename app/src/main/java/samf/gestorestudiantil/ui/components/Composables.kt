package samf.gestorestudiantil.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults.colors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import samf.gestorestudiantil.domain.formatearFechaParaMostrar
import samf.gestorestudiantil.domain.toComposeColor
import samf.gestorestudiantil.domain.toComposeIcon
import samf.gestorestudiantil.data.interfaces.ChipOption
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.domain.uploadToCloudinary
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TopBarRow(
    name: String,
    role: String,
    curso: String,
    imgUrl: String = "",
    onNavigateProfile: () -> Unit,
    onNavigateSettings: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        AccBox(name, role, curso, imgUrl, onClick = { onNavigateProfile() })

        Spacer(modifier = Modifier.weight(1f))

        DropDownMenu(
            onNavigateProfile = { onNavigateProfile() },
            onNavigateSettings = { onNavigateSettings() },
            onLogout = { onLogout() }
        )
    }
}

@Composable
fun BottomNavBar(
    items: Map<String, ImageVector>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = surfaceColor,
        contentColor = textColor,
        modifier = Modifier
            .graphicsLayer(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            clip = true
        )
    ) {
        items.forEach { (label, icon) ->
            val isSelected = selectedItem == label

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(label) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = colors(
                    selectedIconColor = textColor,
                    selectedTextColor = textColor,
                    indicatorColor = surfaceDimColor.copy(alpha = 0.3f),
                    unselectedIconColor = surfaceDimColor,
                    unselectedTextColor = surfaceDimColor
                )
            )
        }
    }
}

@Composable
fun WeekNavBar(selectedItem: String, onItemSelected: (String) -> Unit) {
    val weekDays = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        weekDays.forEach { day ->
            val isSelected = selectedItem == day

            FilterChip(
                selected = isSelected,
                onClick = { onItemSelected(day) },
                label = {
                    Text(
                        text = day.take(3),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.padding(horizontal = 4.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = textColor,
                    selectedLabelColor = backgroundColor,
                    containerColor = Color.Transparent,
                    labelColor = surfaceDimColor
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) Color.Transparent else surfaceDimColor,
                    borderWidth = 1.dp
                )
            )
        }
    }
}

// =========================================================
// COMPONENTE: SELECCIONAR Y SUBIR LA FOTO DE PERFIL
// =========================================================
@Composable
fun ProfileImagePicker(
    currentPhotoUrl: String,
    userId: String? = null,
    onPhotoUploaded: (String) -> Unit
) {
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val nombreArchivo = userId ?: java.util.UUID.randomUUID().toString()

            uploadToCloudinary(
                uri = it,
                fileName = nombreArchivo,
                onStart = { isUploading = true },
                onSuccess = { secureUrl ->
                    isUploading = false
                    onPhotoUploaded(secureUrl)
                },
                onError = { error ->
                    isUploading = false
                    Toast.makeText(context, "Error al subir: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .size(100.dp)
            .clickable(enabled = !isUploading) { photoPickerLauncher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        // Fondo / Imagen del usuario
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(primaryColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (currentPhotoUrl.isNotEmpty()) {
                AsyncImage(
                    model = currentPhotoUrl,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(50.dp), tint = primaryColor)
            }
        }

        // Icono de Lápiz / Edición
        if (!isUploading) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
                    .border(2.dp, backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = "Editar foto", tint = backgroundColor, modifier = Modifier.size(16.dp))
            }
        }

        // Capa oscura de carga
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

// ==========================================
// TARJETAS DE RECORDATORIOS
// ==========================================

@Composable
fun CustomNotificationCard(recordatorio: Recordatorio) {
    val iconModifier = Modifier
        .size(16.dp)
        .padding(end = 4.dp)

    val softRed = Color(0xDDD23B3B)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = recordatorio.titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                Spacer(modifier = Modifier.height(2.dp))
                Text(recordatorio.descripcion, fontSize = 11.sp, color = surfaceDimColor, lineHeight = 14.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.DateRange, "Fecha", tint = surfaceDimColor, modifier = iconModifier)
                    Text(recordatorio.fecha, color = surfaceDimColor, fontSize = 10.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(Icons.Outlined.AccessTime, "Hora", tint = surfaceDimColor, modifier = iconModifier)
                    Text(recordatorio.hora, color = surfaceDimColor, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TypeChip(option = recordatorio.tipo)

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Delete, "Eliminar", tint = softRed, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun AsignaturaCard(asignatura: Asignatura, onClick:() -> Unit ) {
    val iconModifier = Modifier
        .size(16.dp)
        .padding(end = 4.dp)

    val icon = asignatura.iconoName.toComposeIcon()
    val iconColor = asignatura.colorIconoHex.toComposeColor()
    val fondoColor = asignatura.colorFondoHex.toComposeColor()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = fondoColor),
        onClick = { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = asignatura.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = iconColor)
                Spacer(modifier = Modifier.height(2.dp))
                Text(asignatura.descripcion, fontSize = 11.sp, color = surfaceDimColor, lineHeight = 14.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, "Profesor", tint = surfaceDimColor, modifier = iconModifier)
                    Text(asignatura.profesorId, color = surfaceDimColor, fontSize = 10.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(Icons.Outlined.AccessTime, "Hora", tint = surfaceDimColor, modifier = iconModifier)
                    Text(asignatura.horas, color = surfaceDimColor, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun EvaluacionCard(evaluacion: Evaluacion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TypeChip(option = evaluacion.tipoEvaluacion)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = evaluacion.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text("Nota: ${evaluacion.nota}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector? = null,
    label: String,
    readOnly: Boolean,
    isClickable: Boolean,
    isLast: Boolean? = false,
    onClick: (() -> Unit)? = null
) {

    val focusManager = LocalFocusManager.current

    Box (modifier = Modifier.clip(RoundedCornerShape(16.dp))){
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = surfaceColor,
                unfocusedContainerColor = surfaceColor,
                disabledContainerColor = surfaceColor,
                focusedLabelColor = surfaceDimColor,
                unfocusedLabelColor = surfaceDimColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            singleLine = true,
            readOnly = readOnly,
            leadingIcon = {
                if (icon != null) {
                    Icon(icon, null, tint = Color.Gray)
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = if (isLast == true) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = if (isLast == true) {
                KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                )
            } else {
                KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                )
            }
        )
        if (readOnly && isClickable) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        onClick = {
                            onClick?.invoke()
                        }
                    )
            )
        }
    }
}

@Composable
fun CustomPasswordTextField(state: TextFieldState, isLast: Boolean? = false) {

    var isPasswordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    SecureTextField(
        state = state,
        shape = RoundedCornerShape(16.dp),
        label = { Text("Contraseña") },
        // Alternamos entre texto visible y ofuscado
        textObfuscationMode = if (isPasswordVisible) {
            TextObfuscationMode.Visible
        } else {
            TextObfuscationMode.RevealLastTyped // O TextObfuscationMode.Hidden
        },
        leadingIcon = { Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = surfaceDimColor) },
        trailingIcon = {
            if (state.text.isNotEmpty()) { // Solo dibuja si hay texto
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        // Corregido lógicamente: si no es visible, el icono debe invitar a "ver" (Visibility)
                        imageVector = if (!isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (!isPasswordVisible) "Mostrar contraseña" else "Ocultar contraseña"
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = surfaceColor,
            unfocusedContainerColor = surfaceColor,
            disabledContainerColor = surfaceColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedLabelColor = surfaceDimColor,
            unfocusedLabelColor = surfaceDimColor,
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = if (isLast == true) ImeAction.Done else ImeAction.Next
        ),
        onKeyboardAction = {
            if (isLast == true) {
                focusManager.clearFocus()
            } else {
                focusManager.moveFocus(FocusDirection.Down)
            }
        }
    )
}

@Composable
fun CustomOptionsTextField(
    texto: String,
    onValueChange: (String) -> Unit,
    opciones: List<String>,
    icon: ImageVector? = null,
    label: String
) {
    TextField(
        value = texto,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = surfaceColor,
            unfocusedContainerColor = surfaceColor,
            disabledContainerColor = surfaceColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        singleLine = true,
        readOnly = true,
        trailingIcon = {
            CustomDropDownMenu(
                baseIcon = Icons.Default.FilterList,
                optionList = opciones,
                onOptionSelected = { opcionSeleccionada ->
                    onValueChange(opcionSeleccionada)
                }
            )
        },
        leadingIcon = {
            if (icon != null) {
                Icon(icon, null, tint = Color.Gray)
            }
        }
    )
}

@Composable
fun CustomSearchBar(textoBusqueda: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = textoBusqueda,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text("Buscar", color = surfaceDimColor) },
        leadingIcon = { Icon(Icons.Default.Search, "Buscar", tint = Color.Gray) },
        trailingIcon = { Icon(Icons.Outlined.FilterList, "Filtrar", tint = Color.Gray) },
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = surfaceColor,
            unfocusedContainerColor = surfaceColor,
            disabledContainerColor = surfaceColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        singleLine = true
    )
}

// ============ TEXTFIELD DE FECHA ============ //
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (value.isNotBlank()) {
            try {
                val millis = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(value)?.time
                    ?: System.currentTimeMillis()

                if (millis < System.currentTimeMillis() - 86400000) {
                    System.currentTimeMillis()
                } else {
                    millis
                }
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        } else {
            System.currentTimeMillis()
        },
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis() - 86400000
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                IconButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                            onValueChange(sdf.format(millis))
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Seleccionar", tint = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                IconButton(onClick = { showDatePicker = false },colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.inversePrimary)) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.onPrimary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        "Seleccionar fecha",
                        modifier = Modifier.padding(
                            start = 24.dp,
                            end = 12.dp,
                            top = 16.dp,
                            bottom = 12.dp
                        )
                    )
                },
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.secondary,
                    headlineContentColor = MaterialTheme.colorScheme.onSurface,
                    subheadContentColor = MaterialTheme.colorScheme.onSurface,
                    selectedDayContainerColor = MaterialTheme.colorScheme.secondary,
                    navigationContentColor = MaterialTheme.colorScheme.onSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    }

    CustomTextField(value = formatearFechaParaMostrar(value),
        onValueChange = {  },
        label = label,
        icon = Icons.Default.DateRange,
        readOnly = true,
        isClickable = true,
        onClick = { showDatePicker = true }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    var showTimePicker by remember { mutableStateOf(false) }

    val (initialHour, initialMinute) = remember(value) {
        if (value.isNotBlank() && value.contains(":")) {
            try {
                val parts = value.split(":")
                parts[0].toInt() to parts[1].toInt()
            } catch (e: Exception) {
                val cal = java.util.Calendar.getInstance()
                cal.get(java.util.Calendar.HOUR_OF_DAY) to cal.get(java.util.Calendar.MINUTE)
            }
        } else {
            val cal = java.util.Calendar.getInstance()
            cal.get(java.util.Calendar.HOUR_OF_DAY) to cal.get(java.util.Calendar.MINUTE)
        }
    }

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                IconButton(
                    onClick = {
                        showTimePicker = false
                        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                        onValueChange(formattedTime)
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Seleccionar", tint = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                IconButton(
                    onClick = { showTimePicker = false },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.inversePrimary)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.onPrimary)
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Seleccionar hora",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )
                    TimePicker(state = timePickerState)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    CustomTextField(
        value = value,
        onValueChange = { },
        label = label,
        icon = Icons.Outlined.AccessTime,
        readOnly = true,
        isClickable = true,
        onClick = { showTimePicker = true }
    )
}

@Composable
fun CustomDropDownMenu(baseIcon: ImageVector, optionList: List<String>, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
    ) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = baseIcon,
                contentDescription = "Opciones",
                tint = surfaceDimColor
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(30.dp),
            offset = DpOffset(x = 0.dp, y = 0.dp),
            containerColor = backgroundColor,
        ) {
            optionList.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onOptionSelected(option); expanded = false })

                if (option != optionList.last())
                    HorizontalDivider()
            }
        }
    }
}


@Composable
fun TypeChip(option: ChipOption) {
    Box(
        modifier = Modifier
            .background(
                color = option.color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(3.dp)
            .widthIn(min = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = option.label,
            color = option.color,
            fontWeight = FontWeight.Bold,
            fontSize = 8.sp,
            maxLines = 1
        )
    }
}

@Composable
fun MensajeVacio() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("No se encontraron cursos", color = Color.Gray)
    }
}

@Composable
fun AccBox(name: String, role: String, curso: String, imgUrl: String = "", onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AccImg(imgUrl = imgUrl, onClick = onClick)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("Hola, $name", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            Text("$role - $curso", fontSize = 12.sp, color = surfaceDimColor)
        }
    }
}

@Composable
fun AccImg(imgUrl: String = "", onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(Color.Blue),
        contentAlignment = Alignment.Center
    ) {
        if (imgUrl.isNotEmpty()) {
            AsyncImage(
                model = imgUrl,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ============ BOTONES LOGOS ============ //
@Composable
fun SocialMediaButton(
    iconRes: Int,
    size: Dp,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(5.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .padding(15.dp) // Espacio entre tarjetas
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = "Logo",
            modifier = Modifier
                .size(size)
                .padding(15.dp)
        )
    }
}