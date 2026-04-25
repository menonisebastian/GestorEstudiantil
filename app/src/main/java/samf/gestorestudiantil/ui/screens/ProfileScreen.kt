package samf.gestorestudiantil.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.capitalize
import samf.gestorestudiantil.ui.components.ProfileImagePicker
import samf.gestorestudiantil.ui.dialogs.DialogOrchestrator
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.errorColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.theme.whiteColor
import samf.gestorestudiantil.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    usuario: User?,
    onLogout: () -> Unit = {},
    onProfileUpdated: (User) -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel()
)
{
    val context = LocalContext.current
    val themePreference by settingsViewModel.themePreference.collectAsState()

    var currentPhotoUrl by remember { mutableStateOf(usuario?.imgUrl ?: "") }
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState()

    val dialogStack = remember { mutableStateListOf<DialogState>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // === AVATAR ===
            ProfileImagePicker(
                userName = usuario?.nombre ?: "",
                currentPhotoUrl = currentPhotoUrl,
                onPhotoUploaded = { secureUrl ->
                    currentPhotoUrl = secureUrl
                    usuario?.id?.let { uid ->
                        settingsViewModel.updateProfileImage(uid, secureUrl) {
                            Toast.makeText(context, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
                            val updatedUser = when (usuario) {
                                is User.Estudiante -> usuario.copy(imgUrl = secureUrl)
                                is User.Profesor -> usuario.copy(imgUrl = secureUrl)
                                is User.Admin -> usuario.copy(imgUrl = secureUrl)
                                is User.Incompleto -> usuario.copy(imgUrl = secureUrl)
                            }
                            onProfileUpdated(updatedUser)
                        }
                    }
                }
            )

            // === DATOS DEL USUARIO ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Datos", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        
                        IconButton(
                            modifier = Modifier.size(20.dp),
                            onClick = {
                            if (usuario != null) {
                                dialogStack.add(
                                    DialogState.EditSelfProfile(
                                        user = usuario,
                                        onSave = { nuevoNombre ->
                                            settingsViewModel.updateName(usuario.id, nuevoNombre) {
                                                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                                                val updatedUser = when (usuario) {
                                                    is User.Estudiante -> usuario.copy(nombre = nuevoNombre)
                                                    is User.Profesor -> usuario.copy(nombre = nuevoNombre)
                                                    is User.Admin -> usuario.copy(nombre = nuevoNombre)
                                                    is User.Incompleto -> usuario.copy(nombre = nuevoNombre)
                                                }
                                                onProfileUpdated(updatedUser)
                                            }
                                        }
                                    )
                                )
                            }
                        }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Editar", tint = surfaceDimColor, modifier = Modifier.size(20.dp))
                        }
                    }

                    HorizontalDivider(color = surfaceDimColor.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 8.dp))

                    Text("Nombre: ${usuario?.nombre ?: ""}", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Email: ${usuario?.email ?: ""}", color = surfaceDimColor, fontSize = 14.sp)
                    Text("Rol: ${usuario?.rol ?: ""}", color = surfaceDimColor, fontSize = 14.sp)

                    when (usuario) {
                        is User.Estudiante -> {
                            Text("Curso: ${usuario.curso}", color = surfaceDimColor, fontSize = 14.sp)
                            Text("Turno: ${usuario.turno.capitalize()}", color = surfaceDimColor, fontSize = 14.sp)
                        }
                        is User.Profesor -> {
                            Text("Departamento: ${usuario.departamento}", color = surfaceDimColor, fontSize = 14.sp)
                            Text("Turno: ${usuario.turno.capitalize()}", color = surfaceDimColor, fontSize = 14.sp)
                            Text("Asignaturas impartidas: ${usuario.asignaturasImpartidas.size}", color = surfaceDimColor, fontSize = 14.sp)
                        }
                        is User.Admin -> {
                            Text("Administrador del centro", color = surfaceDimColor, fontSize = 14.sp)
                        }
                        else -> {}
                    }
                }
            }

            // === CONFIGURACIÓN ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Configuración", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    
                    HorizontalDivider(color = surfaceDimColor.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notificaciones Push", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text("Recibir avisos de tareas y anuncios", color = surfaceDimColor, fontSize = 12.sp)
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { isEnabled ->
                                settingsViewModel.setNotificationsEnabled(isEnabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = whiteColor,
                                checkedTrackColor = primaryColor,
                                uncheckedThumbColor = surfaceDimColor,
                                uncheckedTrackColor = surfaceDimColor.copy(alpha = 0.5f)
                            )
                        )
                    }

                    HorizontalDivider(color = surfaceDimColor.copy(alpha = 0.2f))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Tema de la aplicación", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        
                        val options = listOf("LIGHT" to "Blanco", "DARK" to "Oscuro", "SYSTEM" to "Automático")
                        
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            options.forEachIndexed { index, (value, label) ->
                                SegmentedButton(
                                    selected = themePreference == value,
                                    onClick = { settingsViewModel.setThemePreference(value) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = SegmentedButtonDefaults.colors(activeContainerColor = primaryColor)
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = CardDefaults.shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = errorColor.copy(alpha = 0.1f),
                    contentColor = errorColor
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Cerrar Sesión"
                    )
                }
            }
        }
    }

    DialogOrchestrator(
        states = dialogStack,
        onShowDialog = { dialogStack.add(it) },
        onDismiss = { dialogStack.remove(it) }
    )
}