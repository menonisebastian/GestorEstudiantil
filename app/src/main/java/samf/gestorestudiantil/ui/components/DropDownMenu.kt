package samf.gestorestudiantil.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.textColor


@Composable
fun DropDownMenu(
    onNavigateSettings: () -> Unit,
    onLogout: () -> Unit,
    onNavigateProfile: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    // Box es el ancla. wrapContentSize asegura que sea del tamaño del icono.
    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
    ) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "Opciones",
                tint = textColor
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(30.dp),
            // offset: Ajuste fino por si se sale de la pantalla (opcional)
            offset = DpOffset(x = 0.dp, y = 0.dp),
            containerColor = backgroundColor,
        ) {
            DropdownMenuItem(
                text = { Text("Cuenta") },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = textColor) },
                onClick = {
                    expanded = false
                    onNavigateProfile()
                }
            )
            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("Preferencias") },
                leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null, tint = textColor) },
                onClick = {
                    expanded = false
                    onNavigateSettings()
                }
            )
            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("Ayuda") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = textColor) },
                onClick = {
                    expanded = false
                }
            )
            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("Cerrar sesión") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = textColor) },
                onClick = {
                    expanded = false
                    onLogout()
                }
            )
        }
    }
}