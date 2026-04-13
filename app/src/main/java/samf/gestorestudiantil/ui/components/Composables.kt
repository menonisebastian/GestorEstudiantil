package samf.gestorestudiantil.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import samf.gestorestudiantil.domain.obtenerInicialesDeNombre
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.ui.theme.textColor
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import samf.gestorestudiantil.R
import samf.gestorestudiantil.domain.formatearFechaParaMostrar
import samf.gestorestudiantil.domain.toComposeColor
import samf.gestorestudiantil.domain.toComposeIcon
import samf.gestorestudiantil.data.interfaces.ChipOption
import samf.gestorestudiantil.data.models.Asignatura
import java.util.Locale
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.domain.uploadToCloudinary
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.errorColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.tertiaryColor
import samf.gestorestudiantil.ui.theme.whiteColor

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun BottomNavBar(
    items: Map<String, ImageVector>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    hazeState: HazeState?,
    userImgUrl: String = "",
    userName: String = ""
) {

    val hazeStyle = CupertinoMaterials.thick()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 2.dp)
                .wrapContentSize()
                .clip(CircleShape)
                .hazeEffect(hazeState){style = hazeStyle},
            shape = CircleShape,
            color = surfaceColor.copy(alpha = 0.95f),
            tonalElevation = 8.dp,
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { (label, icon) ->
                    val isSelected = selectedItem == label

                    val animatedBgColor by animateColorAsState(
                        targetValue = if (isSelected) primaryColor.copy(alpha = 0.15f) else Color.Transparent,
                        animationSpec = tween(durationMillis = 150),
                        label = "nav_bg_color"
                    )

                    val animatedIconColor by animateColorAsState(
                        targetValue = if (isSelected) primaryColor else surfaceDimColor,
                        animationSpec = tween(durationMillis = 150),
                        label = "nav_icon_color"
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(animatedBgColor)
                            .clickable { onItemSelected(label) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (label == "Perfil") {
                                AccImg(
                                    userName = userName,
                                    imgUrl = userImgUrl,
                                    size = 24.dp,
                                    onClick = { onItemSelected(label) }
                                )
                            } else {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = animatedIconColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = fadeIn(animationSpec = tween(150)) + expandHorizontally(animationSpec = tween(150)),
                                exit = fadeOut(animationSpec = tween(150)) + shrinkHorizontally(animationSpec = tween(150))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
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
                    selectedContainerColor = primaryColor,
                    selectedLabelColor = whiteColor,
                    containerColor = surfaceColor,
                    labelColor = surfaceDimColor
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) Color.Transparent else surfaceDimColor.copy(alpha = 0.5f),
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
    userName: String = "",
    currentPhotoUrl: String,
    onPhotoUploaded: (String) -> Unit
) {
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {

            uploadToCloudinary(
                uri = it,
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
        AccImg(
            userName = userName,
            imgUrl = currentPhotoUrl,
            size = 100.dp,
            onClick = { if (!isUploading) photoPickerLauncher.launch("image/*") }
        )

        // Icono de Lápiz / Edición
        if (!isUploading) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(tertiaryColor)
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
fun CustomNotificationCard(
    recordatorio: Recordatorio,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val iconModifier = Modifier
        .size(16.dp)
        .padding(end = 4.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
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
                    Text(formatearFechaParaMostrar(recordatorio.fecha), color = surfaceDimColor, fontSize = 10.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(Icons.Outlined.AccessTime, "Hora", tint = surfaceDimColor, modifier = iconModifier)
                    Text(recordatorio.hora, color = surfaceDimColor, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TypeChip(option = recordatorio.tipo)

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = errorColor.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Outlined.Delete, "Eliminar", tint = errorColor, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun AsignaturaCard(
    asignatura: Asignatura,
    userRole: String, // "ADMIN", "PROFESOR" o "ESTUDIANTE"
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
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
                Text(text = asignatura.acronimo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = iconColor)
                Spacer(modifier = Modifier.height(2.dp))
                Text(asignatura.nombre, fontSize = 11.sp, color = surfaceDimColor, lineHeight = 14.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Indicador de Turno
                    val turnoSigla = if (asignatura.turno.lowercase().contains("matutino")) "M" else "V"
                    val turnoColor = if (turnoSigla == "M") Color(0xFFF59E0B) else Color(0xFF6366F1) // Ámbar vs Indigo

                    Surface(
                        color = turnoColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = turnoSigla,
                            color = turnoColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    Icon(Icons.Outlined.Person, "Profesor", tint = surfaceDimColor, modifier = iconModifier)
                    Text(
                        text = asignatura.profesorNombre.ifEmpty { "Sin asignar" },
                        color = surfaceDimColor,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(Icons.Outlined.AccessTime, "Horas", tint = surfaceDimColor, modifier = iconModifier)
                    Text(text = "${asignatura.horasSemanales}h", color = surfaceDimColor, fontSize = 10.sp)

                    // SOLO VISIBLE PARA ADMIN O PROFESOR
                    if (userRole != "ESTUDIANTE") {
                        Spacer(modifier = Modifier.width(16.dp))

                        Icon(Icons.Outlined.Groups, "Alumnos", tint = surfaceDimColor, modifier = iconModifier)
                        Text(text = "${asignatura.numEstudiantesCurso}", color = surfaceDimColor, fontSize = 10.sp)
                    }
                }
            }

            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = iconColor)
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
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TypeChip(option = evaluacion.tipoEvaluacion)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = evaluacion.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = String.format(Locale.getDefault(), "%.1f", evaluacion.nota),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = if (evaluacion.nota >= 5) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    readOnly: Boolean = false,
    isClickable: Boolean = false,
    isLast: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    onClick: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {

    val focusManager = LocalFocusManager.current

    Box (modifier = modifier.clip(RoundedCornerShape(16.dp))){
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (singleLine) Modifier.height(56.dp) else Modifier.heightIn(min = 56.dp))
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = surfaceColor,
                unfocusedContainerColor = surfaceColor,
                disabledContainerColor = surfaceColor,
                focusedLabelColor = primaryColor,
                unfocusedLabelColor = surfaceDimColor,
                focusedIndicatorColor = primaryColor.copy(alpha = 0.5f),
                unfocusedIndicatorColor = Color.Transparent,
            ),
            singleLine = singleLine,
            minLines = minLines,
            readOnly = readOnly,
            leadingIcon = icon?.let {
                { Icon(it, null, tint = Color.Gray) }
            },
            keyboardOptions = keyboardOptions.copy(
                imeAction = if (isLast) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = if (isLast) {
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
            focusedIndicatorColor = primaryColor.copy(alpha = 0.5f),
            unfocusedIndicatorColor = Color.Transparent,
            focusedLabelColor = primaryColor,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomOptionsTextField(
    texto: String,
    onValueChange: (String) -> Unit,
    opciones: List<String>,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    var expanded by remember { mutableStateOf(false) }

    // ExposedDropdownMenuBox gestiona el estado de expansión y el ancho del menú
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
    ) {
        TextField(
            value = texto,
            onValueChange = {}, // ReadOnly, el cambio se hace vía menú
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                // Updated: Specify the anchor type and enabled state
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = surfaceColor,
                unfocusedContainerColor = surfaceColor,
                disabledContainerColor = surfaceColor,
                focusedIndicatorColor = primaryColor.copy(alpha = 0.5f),
                unfocusedIndicatorColor = Color.Transparent,
                focusedLabelColor = primaryColor,
                unfocusedLabelColor = surfaceDimColor,
            ),
            singleLine = true,
            leadingIcon = icon?.let {
                { Icon(it, contentDescription = null, tint = Color.Gray) }
            },
            trailingIcon = {
                // Este componente rota la flecha automáticamente según el estado 'expanded'
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = surfaceColor, // Corregido para usar surfaceColor y ser consistente
            shape = RoundedCornerShape(20.dp)
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion, color = textColor) },
                    onClick = {
                        onValueChange(opcion)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )

                // Mantenemos tu divisor visual
                if (opcion != opciones.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        thickness = 0.5.dp,
                        color = surfaceDimColor.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSearchBar(
    textoBusqueda: String,
    onValueChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    filters: Map<String, String> = emptyMap(),
    onRemoveFilter: (String) -> Unit = {}
) {
    TextField(
        value = textoBusqueda,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp)),
        placeholder = { Text("Buscar", color = surfaceDimColor) },
        leadingIcon = {
            Icon(Icons.Default.Search, "Buscar", tint = Color.Gray)
        },
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    filters.forEach { (key, value) ->
                        val chips = value.split(",").filter { it.isNotEmpty() }
                        items(chips) { chipValue ->
                            androidx.compose.material3.InputChip(
                                selected = true,
                                onClick = { },
                                label = { Text(chipValue, fontSize = 11.sp, maxLines = 1) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Quitar filtro",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                if (chips.size == 1) {
                                                    onRemoveFilter(key)
                                                } else {
                                                    val newValue = chips.filter { it != chipValue }.joinToString(",")
                                                    onRemoveFilter("$key:$newValue")
                                                }
                                            }
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = androidx.compose.material3.InputChipDefaults.inputChipColors(
                                    selectedContainerColor = primaryColor.copy(alpha = 0.15f),
                                    selectedLabelColor = primaryColor
                                ),
                                border = null,
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                }

                if (textoBusqueda.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(Icons.Outlined.Close, "Cerrar", tint = Color.Gray)
                    }
                }

                IconButton(onClick = { onFilterClick() }) {
                    Icon(Icons.Outlined.FilterList, "Filtrar", tint = Color.Gray)
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = surfaceColor,
            unfocusedContainerColor = surfaceColor,
            disabledContainerColor = surfaceColor,
            focusedIndicatorColor = primaryColor.copy(alpha = 0.5f),
            unfocusedIndicatorColor = Color.Transparent,
            focusedLabelColor = primaryColor,
            unfocusedPlaceholderColor = surfaceDimColor
        ),
        singleLine = true
    )
}

// ============ TEXTFIELD DE FECHA ============ //
@Composable
fun CustomDateField(
    value: String,
    label: String,
    onShowDatePicker: () -> Unit
) {
    CustomTextField(value = formatearFechaParaMostrar(value),
        onValueChange = {  },
        label = label,
        icon = Icons.Default.DateRange,
        readOnly = true,
        isClickable = true,
        onClick = onShowDatePicker
    )
}

@Composable
fun CustomTimeField(
    value: String,
    label: String,
    onShowTimePicker: () -> Unit
) {
    CustomTextField(
        value = value,
        onValueChange = { },
        label = label,
        icon = Icons.Outlined.AccessTime,
        readOnly = true,
        isClickable = true,
        onClick = onShowTimePicker
    )
}

data class MenuItem(
    val text: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false,
    val iconTint: Color? = null
)

@Composable
fun CustomDropDownMenu(
    baseIcon: ImageVector = Icons.Default.MoreVert,
    iconTint: Color = surfaceDimColor,
    items: List<MenuItem>
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
    ) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = baseIcon,
                contentDescription = "Opciones",
                tint = iconTint
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(20.dp),
            offset = DpOffset(x = 0.dp, y = 0.dp),
            containerColor = surfaceColor,
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.text,
                            color = if (item.isDestructive) errorColor else textColor
                        )
                    },
                    onClick = {
                        item.onClick()
                        expanded = false
                    },
                    leadingIcon = item.icon?.let {
                        {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                tint = item.iconTint ?: if (item.isDestructive) errorColor else surfaceDimColor
                            )
                        }
                    }
                )

                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        thickness = 0.5.dp,
                        color = surfaceDimColor.copy(alpha = 0.2f)
                    )
                }
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
fun FloatingPill(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
fun AccImg(userName: String = "", imgUrl: String = "", onClick: () -> Unit = {}, size: Dp) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(tertiaryColor.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        if (imgUrl.isNotEmpty()) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(context)
                    .data(imgUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = obtenerInicialesDeNombre(userName),
                color = primaryColor,
                fontSize = (size.value * 0.35).sp,
                fontWeight = FontWeight.Bold
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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

// ============ BOTONES LOGOS ============ //
@Composable
fun IconLogo(
    width: Dp
) {
    Image(
        painter = painterResource(R.drawable.icononexopluscut),
        contentDescription = "IconLogo",
        modifier = Modifier
            .width(width)
            .padding(15.dp)
    )
}

// ============ BOTONES LOGOS ============ //
@Composable
fun TitleLogo(
    width: Dp
) {
    Image(
        painter = painterResource(R.drawable.logonexoplus),
        contentDescription = "IconLogo",
        modifier = Modifier
            .width(width)
            .padding(15.dp)
    )
}

@Composable
fun TextDivider(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Línea izquierda
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Texto central
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = surfaceDimColor
        )

        // Línea derecha
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
