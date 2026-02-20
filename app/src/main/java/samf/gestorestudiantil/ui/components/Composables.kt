package samf.gestorestudiantil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults.colors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.models.Materia
import samf.gestorestudiantil.models.Modulo
import samf.gestorestudiantil.models.Recordatorio



import samf.gestorestudiantil.models.tipoRecordatorio
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach

@Composable
fun TopBarRow(name: String, role: String, curso: String) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        AccBox(name, role, curso)

        Spacer(modifier = Modifier.weight(1f))

        DropDownMenu()
    }
}

@Composable
fun BottomNavBar(
    items: Map<String, ImageVector>,
    selectedItem: String,          // Nuevo: Recibe cuál está seleccionado
    onItemSelected: (String) -> Unit
)
{
    // Componente oficial de Material3
    NavigationBar(
        containerColor = backgroundColor, // O el color que desees para el fondo
        contentColor = textColor
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
                // Personalización de colores para que coincida con tu tema
                colors = colors(
                    selectedIconColor = textColor,
                    selectedTextColor = textColor,
                    indicatorColor = surfaceDimColor.copy(alpha = 0.3f), // El color de la "píldora" de fondo
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

    // Usamos un Row con scroll horizontal por si la pantalla es muy pequeña
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly, // Intenta distribuirlos
        verticalAlignment = Alignment.CenterVertically
    ) {
        weekDays.forEach { day ->
            val isSelected = selectedItem == day

            FilterChip(
                selected = isSelected,
                onClick = { onItemSelected(day) },
                label = {
                    Text(
                        // Mostramos las primeras 3 letras para que quepa bien (Lun, Mar...)
                        text = day.take(3),
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.padding(horizontal = 4.dp),
                // Personalizamos los colores para tu tema
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

@Composable
fun CustomNotificationCard(recordatorio: Recordatorio)
{
    val iconModifier = Modifier
        .size(16.dp)
        .padding(end = 4.dp)
    val label = recordatorio.titulo
    val description = recordatorio.descripcion
    val date = recordatorio.fecha
    val time = recordatorio.hora
    val tipo = recordatorio.tipo
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
    )
    {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
        {
            Column{
                Column()
                {
                    Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                    Text(description, textAlign = TextAlign.Justify, fontSize = 10.sp, color = surfaceDimColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Icon(Icons.Outlined.DateRange,
                        "Fecha",
                        tint = surfaceDimColor,
                        modifier = iconModifier)
                    Text(date,
                        color = surfaceDimColor,
                        fontSize = 10.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(Icons.Outlined.AccessTime,
                        "Hora",
                        tint = surfaceDimColor,
                        modifier = iconModifier)
                    Text(time,
                        color = surfaceDimColor,
                        fontSize = 10.sp)

                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column (horizontalAlignment = Alignment.CenterHorizontally) {

                IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.Delete, "Eliminar",
                        tint = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                TypeChip(tipoRecordatorio = tipo)
            }
        }
    }
}

@Composable
fun MateriaCard(materia: Materia, onClick:() -> Unit )
{
    val iconModifier = Modifier
        .size(16.dp)
        .padding(end = 4.dp)
    val label = materia.nombre
    val description = materia.descripcion
    val qtyHours = materia.horas
    val prof = materia.profesor
    val icon = materia.icono
    val iconColor = materia.colorIcono
    val fondoColor = materia.colorFondo

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = fondoColor),
        onClick = {onClick()}
    )
    {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
        {
            Column{
                Column()
                {
                    Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = iconColor)
                    Text(description, textAlign = TextAlign.Justify, fontSize = 10.sp, color = surfaceDimColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Icon(Icons.Outlined.Person,
                        "Fecha",
                        tint = surfaceDimColor,
                        modifier = iconModifier)
                    Text(prof,
                        color = surfaceDimColor,
                        fontSize = 10.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(Icons.Outlined.AccessTime,
                        "Hora",
                        tint = surfaceDimColor,
                        modifier = iconModifier)
                    Text(qtyHours,
                        color = surfaceDimColor,
                        fontSize = 10.sp)

                }
            }


            Spacer(modifier = Modifier.weight(1f))

            Icon(icon,
                null,
                tint = iconColor)
        }
    }
}

@Composable
fun ModuloCard(modulo: Modulo)
{
    val label = modulo.nombre
    val materia = modulo.materia
    val nota = modulo.nota

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    )
    {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
        {
            Column{
                Column()
                {
                    Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                    Text(materia, textAlign = TextAlign.Justify, fontSize = 10.sp, color = surfaceDimColor)

                }
//                Row(verticalAlignment = Alignment.CenterVertically) {
//
//                    Icon(Icons.Outlined.Person,
//                        "Fecha",
//                        tint = surfaceDimColor,
//                        modifier = iconModifier)
//                    Text(prof,
//                        color = surfaceDimColor,
//                        fontSize = 10.sp)
//
//                    Spacer(modifier = Modifier.width(16.dp))
//
//                    Icon(Icons.Outlined.AccessTime,
//                        "Hora",
//                        tint = surfaceDimColor,
//                        modifier = iconModifier)
//                    Text(qtyHours,
//                        color = surfaceDimColor,
//                        fontSize = 10.sp)
//
//                }
            }


            Spacer(modifier = Modifier.weight(1f))

            Text("Nota: $nota", textAlign = TextAlign.Justify, fontSize = 10.sp, color = surfaceDimColor)
        }
    }
}

@Composable
fun CustomTextField(
    texto: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector? = null,
    label: String
)
{
    OutlinedTextField(
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
        leadingIcon = {
            if (icon != null) {
                Icon(icon, null, tint = Color.Gray)
            }
        }
    )
}

@Composable
fun CustomSearchBar(textoBusqueda: String, onValueChange: (String) -> Unit)
{
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

@Composable
fun TypeChip(tipoRecordatorio: tipoRecordatorio) {
    Box(
        modifier = Modifier
            .background(
                color = tipoRecordatorio
                    .color.copy(alpha = 0.2f), // Fondo suave
                shape = RoundedCornerShape(16.dp)
            )
            .padding(3.dp)
            .width(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tipoRecordatorio.label,
            color = tipoRecordatorio.color, // Texto del color fuerte
            fontWeight = FontWeight.Bold,
            fontSize = 8.sp
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
fun AccBox(name: String, role: String, curso: String) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    )
    {
        AccImg()
        Spacer(modifier = Modifier.width(16.dp))
        Column{
            Text("Hola, $name", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            Text("$role - $curso", fontSize = 12.sp, color = surfaceDimColor)
        }
    }
}

@Composable
fun AccImg() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Blue), // El color oscuro del icono
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}