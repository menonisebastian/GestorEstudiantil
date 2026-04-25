package samf.gestorestudiantil.domain

import android.net.Uri
import samf.gestorestudiantil.data.models.User
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.toColorInt
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

// ==========================================
// MAPPERS PARA LA INTERFAZ (UI Mappers)
// ==========================================

// =========================================================
// FUNCIÓN UNIVERSAL DE CLOUDINARY
// =========================================================

fun uploadToCloudinary(
    uri: Uri,
    onStart: () -> Unit = {},
    onProgress: (Float) -> Unit = {},
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val request = MediaManager.get().upload(uri).unsigned("gestor_perfiles")

    request.callback(object : UploadCallback {
        override fun onStart(requestId: String) { onStart() }

        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
            val progress = if (totalBytes > 0) bytes.toFloat() / totalBytes.toFloat() else 0f
            onProgress(progress)
        }

        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
            val secureUrl = resultData["secure_url"] as String

            val antiCacheUrl = "$secureUrl?v=${System.currentTimeMillis()}"

            onSuccess(antiCacheUrl)
        }

        override fun onError(requestId: String, error: ErrorInfo) {
            onError(error.description)
        }

        override fun onReschedule(requestId: String, error: ErrorInfo) {}
    })
        .dispatch()
}
/**
 * Convierte un String Hex (ej: "#D0E1FF") a un objeto Color de Compose.
 */
fun String.toComposeColor(): Color {
    return try {
        Color(this.toColorInt())
    } catch (e: Exception) {
        e.printStackTrace()
        Color.Gray // Color por defecto de seguridad
    }
}

/**
 * Mapea el nombre guardado en Firestore a un ImageVector de Material Icons.
 * Los nombres corresponden exactamente a los usados en el JSONL y los modelos.
 */
fun String.toComposeIcon(): ImageVector {
    return when (this) {
        // ── Programación / Desarrollo ──────────────────────────────
        "Code"                  -> Icons.Default.Code
        "DataObject"            -> Icons.Default.DataObject
        "Computer"              -> Icons.Default.Computer
        "Terminal"              -> Icons.Default.Terminal
        "CodeOff"               -> Icons.Default.CodeOff
        "Build"                 -> Icons.Default.Build
        "BuildOutlined"         -> Icons.Default.Build
        "PhoneIphone"           -> Icons.Default.PhoneIphone
        "AppsOutlined"          -> Icons.Default.Apps
        "LightbulbOutlined"     -> Icons.Default.Lightbulb
        "Brush"                 -> Icons.Default.Brush
        // ── Web ────────────────────────────────────────────────────
        "LanguageOutlined"      -> Icons.Default.Language
        "Web"                   -> Icons.Default.Web
        "DesktopWindows"        -> Icons.Default.DesktopWindows
        "CloudOutlined"         -> Icons.Default.Cloud
        "CloudUpload"           -> Icons.Default.CloudUpload
        "Palette"               -> Icons.Default.Palette
        "DrawOutlined"          -> Icons.Default.Draw
        "MergeTypeOutlined"     -> Icons.AutoMirrored.Filled.MergeType
        // ── Redes / Sistemas ───────────────────────────────────────
        "RouterOutlined"        -> Icons.Default.Router
        "Wifi"                  -> Icons.Default.Wifi
        "WifiOutlined"          -> Icons.Default.Wifi
        "Dns"                   -> Icons.Default.Dns
        "DnsOutlined"           -> Icons.Default.Dns
        "Storage"               -> Icons.Default.Storage
        "MemoryOutlined"        -> Icons.Default.Memory
        "SettingsOutlined"      -> Icons.Default.Settings
        "LanOutlined"           -> Icons.Default.Lan
        "CableOutlined"         -> Icons.Default.Cable
        "ElectricalServices"    -> Icons.Default.ElectricalServices
        "AllInbox"              -> Icons.Default.AllInbox
        "GridView"              -> Icons.Default.GridView
        "Devices"               -> Icons.Default.Devices
        "DevicesOutlined"       -> Icons.Default.Devices
        "Engineering"           -> Icons.Default.Engineering
        // ── Ciberseguridad ─────────────────────────────────────────
        "Security"              -> Icons.Default.Security
        "GppGoodOutlined"       -> Icons.Default.GppGood
        "GppBadOutlined"        -> Icons.Default.GppBad
        "ShieldOutlined"        -> Icons.Default.Shield
        "LockOutlined"          -> Icons.Default.Lock
        "BugReportOutlined"     -> Icons.Default.BugReport
        "SearchOutlined"        -> Icons.Default.Search
        "GavelOutlined"         -> Icons.Default.Gavel
        // ── IA / Big Data ──────────────────────────────────────────
        "PsychologyOutlined"    -> Icons.Default.Psychology
        "SmartToyOutlined"      -> Icons.Default.SmartToy
        "ModelTraining"         -> Icons.Default.ModelTraining
        // ── Videojuegos / VR ───────────────────────────────────────
        "SportsEsports"         -> Icons.Default.SportsEsports
        "Vrpano"                -> Icons.Default.Vrpano
        // ── Marketing / Comunicación ───────────────────────────────
        "CampaignOutlined"      -> Icons.Default.Campaign
        "TrendingUp"            -> Icons.AutoMirrored.Filled.TrendingUp
        "BarChartOutlined"      -> Icons.Default.BarChart
        "BroadcastOnHome"       -> Icons.Default.BroadcastOnHome
        "RocketLaunchOutlined"  -> Icons.Default.RocketLaunch
        "SupportAgentOutlined"  -> Icons.Default.SupportAgent
        "PeopleOutlined"        -> Icons.Default.People
        // ── Administración / Finanzas ──────────────────────────────
        "AccountBalance"        -> Icons.Default.AccountBalance
        "AccountBalanceOutlined"-> Icons.Default.AccountBalance
        "AccountBalanceWallet"  -> Icons.Default.AccountBalanceWallet
        "CalculateOutlined"     -> Icons.Default.Calculate
        "ReceiptOutlined"       -> Icons.Default.Receipt
        "DescriptionOutlined"   -> Icons.Default.Description
        "FolderOpenOutlined"    -> Icons.Default.FolderOpen
        "BadgeOutlined"         -> Icons.Default.Badge
        "BusinessCenter"        -> Icons.Default.BusinessCenter
        "BusinessOutlined"      -> Icons.Default.Business
        "Laptop"                -> Icons.Default.Laptop
        // ── Comercio / Logística ───────────────────────────────────
        "StorefrontOutlined"    -> Icons.Default.Storefront
        "ShoppingCartOutlined"  -> Icons.Default.ShoppingCart
        "ShoppingBagOutlined"   -> Icons.Default.ShoppingBag
        "PointOfSaleOutlined"   -> Icons.Default.PointOfSale
        "SellOutlined"          -> Icons.Default.Sell
        "Inventory2Outlined"    -> Icons.Default.Inventory2
        "LocalShippingOutlined" -> Icons.Default.LocalShipping
        "PublicOutlined"        -> Icons.Default.Public
        "HandshakeOutlined"     -> Icons.Default.Handshake
        "CreditCardOutlined"    -> Icons.Default.CreditCard
        // ── Transversales ──────────────────────────────────────────
        "TranslateOutlined"     -> Icons.Default.Translate
        "WorkOutlineOutlined"   -> Icons.Default.WorkOutline
        "ApartmentOutlined"     -> Icons.Default.Apartment
        "SchoolOutlined"        -> Icons.Default.School
        "MenuBookOutlined"      -> Icons.AutoMirrored.Filled.MenuBook
        "ScienceOutlined"       -> Icons.Default.Science
        "AssignmentOutlined"    -> Icons.AutoMirrored.Filled.Assignment
        "NatureOutlined"        -> Icons.Default.Nature
        // ── Default ────────────────────────────────────────────────
        else                    -> Icons.Default.Class
    }
}

// ============ FORMATEO DE FECHA ============ //
fun formatearFechaParaMostrar(fechaIso: String, prettyDate: Boolean = false): String
{

    if (fechaIso.isBlank()) return ""
    return try {
        // Soporta tanto "2025/01/01" como "2025-01-01"
        val partes = if (fechaIso.contains("/")) fechaIso.split("/") else fechaIso.split("-")
        val mes = partes[1].toInt()
        val dia = partes[2].toInt()
        if (partes.size < 3) return fechaIso
        if (prettyDate) {
            // Guardamos el resultado del when en una nueva variable (nombreMes)
            val nombreMes = when (mes) {
                1 -> "Enero"
                2 -> "Febrero"
                3 -> "Marzo"
                4 -> "Abril"
                5 -> "Mayo"
                6 -> "Junio"
                7 -> "Julio"
                8 -> "Agosto"
                9 -> "Septiembre"
                10 -> "Octubre"   // Añadidos los meses faltantes
                11 -> "Noviembre"
                12 -> "Diciembre"
                else -> ""
            }
            // Usamos 'nombreMes' en lugar de 'mes'
            "$dia de $nombreMes de ${partes[0]}"
        }
        else "${partes[2]}/${partes[1]}/${partes[0]}" // Retorna "01/01/2025"
    } catch (e: Exception) {
        e.printStackTrace()
        fechaIso
    }
}

fun compararFechaActual(fecha: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val fechaActual = LocalDate.now()
    return LocalDate.parse(fecha, formatter) <= fechaActual
}

/**
 * Obtiene la primera letra del nombre y del apellido de un usuario.
 * Si el nombre tiene varias partes, toma la primera y la última (asumiendo que es el apellido).
 */
fun obtenerInicialesDeNombre(nombre: String?): String {
    val nombreCompleto = nombre?.trim() ?: return ""
    if (nombreCompleto.isEmpty()) return ""

    val partes = nombreCompleto.split("\\s+".toRegex())
    return if (partes.size >= 2) {
        "${partes.first().first()}${partes.last().first()}".uppercase()
    } else {
        "${partes.first().first()}".uppercase()
    }
}

/**
 * Capitaliza la primera letra de un texto (ej: "matutino" -> "Matutino").
 */
fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
