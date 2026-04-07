package samf.gestorestudiantil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import samf.gestorestudiantil.domain.toComposeColor
import samf.gestorestudiantil.domain.toComposeIcon

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
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 140.dp)
                ) {
                    items(commonColors) { hex ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(hex.toComposeColor())
                                .clickable {
                                    currentColor = hex.toComposeColor()
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ClassicColorPicker(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                    color = HsvColor.from(currentColor),
                    onColorChanged = { newHsvColor: HsvColor ->
                        currentColor = newHsvColor.toColor()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

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

fun Color.toHex(): String {
    val alpha = (this.alpha * 255).toInt()
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
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
