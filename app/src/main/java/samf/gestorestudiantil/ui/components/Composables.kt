package samf.gestorestudiantil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach

@Composable
fun TopBarRow(name: String, role: String) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)) {
        AccBox(name, role)

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = {}) {
            Icon(
                Icons.Default.MoreHoriz,
                "Opciones",
                tint = textColor
            )
        }
    }
}

@Composable
fun BottomBar(items: Map<String, ImageVector>) {

    var selectedItem by remember { mutableStateOf(items.keys.first()) }

    // Usamos Row para distribuir los botones horizontalmente
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor), // Asegúrate de que esta variable sea accesible o usa MaterialTheme.colorScheme.background
        horizontalArrangement = Arrangement.SpaceEvenly, // Distribuye el espacio equitativamente
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Recorremos el mapa (key = nombre, value = icono)
        items.forEach { (label, icon) ->
            BottomBarButton(
                icon = icon,
                label = label,
                selected = selectedItem == label,
                onClick = {
                    // Aquí iría la lógica al pulsar cada botón
                    // Ejemplo: navController.navigate(route)
                }
            )
        }
    }
}

@Composable
fun BottomBarButton(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .clickable(onClick = { onClick() }),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally)
        {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) textColor else surfaceDimColor,
                //modifier = Modifier.size(24.dp)
            )
            Text(label, color = if (selected) textColor else surfaceDimColor, fontSize = 12.sp)
        }
    }
}


@Composable
fun AccBox(name: String, role: String) {
    Row (horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        //modifier = Modifier.padding(horizontal = 16.dp)
    )
    {
        AccImg()
        Spacer(modifier = Modifier.width(16.dp))
        Column{
            Text("Hola, $name", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            Text(role, fontSize = 12.sp, color = surfaceDimColor)
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