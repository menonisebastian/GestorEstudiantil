package samf.gestorestudiantil.ui.dialogs

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.AccImg
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import androidx.core.net.toUri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawWithContent

@Composable
fun UserProfileDialog(
    state: DialogState.UserProfile,
    onDismissRequest: () -> Unit
) {
    val user = state.user
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = backgroundColor,
        tonalElevation = 8.dp,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cerrar", color = textColor)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:${user.email}".toUri()
                }
                context.startActivity(intent)
            }) {
                Text("Enviar Email", color = primaryColor)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Imagen de usuario
                AccImg(userName = user.nombre, imgUrl = user.imgUrl, size = 100.dp)

                Text(
                    text = user.nombre,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )

                HorizontalDivider(color = surfaceDimColor.copy(alpha = 0.2f))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileInfoRow(
                        icon = Icons.Outlined.Person,
                        label = "Rol",
                        value = user.rol
                    )
                    ProfileInfoRow(
                        icon = Icons.Outlined.School,
                        label = when (user) {
                            is User.Estudiante -> "Curso"
                            is User.Profesor -> "Departamento"
                            else -> "Curso / Área"
                        },
                        value = when (user) {
                            is User.Estudiante -> user.curso
                            is User.Profesor -> user.departamento
                            else -> ""
                        }
                    )
                    ProfileInfoRow(
                        icon = Icons.Outlined.Email,
                        label = "Email",
                        value = user.email,
                        isAutoResizing = true
                    )
                }
            }
        }
    )
}

@Composable
private fun AutoResizingText(
    text: String,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    color: Color,
    maxLines: Int = 1
) {
    var resizedFontSize by remember { mutableStateOf(fontSize) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        fontSize = resizedFontSize,
        fontWeight = fontWeight,
        color = color,
        maxLines = maxLines,
        softWrap = false,
        modifier = Modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth && resizedFontSize > 10.sp) {
                resizedFontSize *= 0.9f
            } else {
                readyToDraw = true
            }
        }
    )
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isAutoResizing: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = surfaceDimColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = surfaceDimColor
            )
            if (isAutoResizing) {
                AutoResizingText(
                    text = value.ifBlank { "No especificado" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            } else {
                Text(
                    text = value.ifBlank { "No especificado" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}
