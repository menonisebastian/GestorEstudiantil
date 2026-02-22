package samf.gestorestudiantil.domain

import android.net.Uri
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
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

// Función que maneja la lógica de subida con el SDK de Cloudinary
fun uploadToCloudinary(
    uri: Uri,
    onStart: () -> Unit,
    onProgress: (Float) -> Unit,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    MediaManager.get().upload(uri)
        // RECUERDA: Cambiar esto por el nombre de tu Upload Preset Unsigned en Cloudinary
        .unsigned("gestor_perfiles")
        .callback(object : UploadCallback {
            override fun onStart(requestId: String) {
                onStart()
            }

            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                onProgress(bytes.toFloat() / totalBytes.toFloat())
            }

            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                val secureUrl = resultData["secure_url"] as String
                onSuccess(secureUrl)
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
 * Mapea el nombre guardado en Firebase a un icono real de Compose.
 */
fun String.toComposeIcon(): ImageVector {
    return when (this) {
        "PhoneIphone" -> Icons.Default.PhoneIphone
        "Build" -> Icons.Default.Build
        "DataObject" -> Icons.Default.DataObject
        "AllInbox" -> Icons.Default.AllInbox
        "BusinessCenter" -> Icons.Default.BusinessCenter
        "Devices" -> Icons.Default.Devices
        "Engineering" -> Icons.Default.Engineering
        "Nature" -> Icons.Default.Nature
        "Laptop" -> Icons.Default.Laptop
        "Web" -> Icons.Default.Web
        "Wifi" -> Icons.Default.Wifi
        "Security" -> Icons.Default.Security
        "Storage" -> Icons.Default.Storage
        else -> Icons.Default.Class // Icono por defecto
    }
}

// ============ FORMATEO DE FECHA ============ //
fun formatearFechaParaMostrar(fechaIso: String): String {
    if (fechaIso.isBlank()) return ""
    return try {
        // Divide "2025/01/01" y reordena
        val partes = fechaIso.split("/")
        "${partes[2]}/${partes[1]}/${partes[0]}" // Retorna "01/01/2025"
    } catch (e: Exception)
    {
        e.printStackTrace()
        fechaIso // Si falla, devuelve la original
    }
}

fun compararFechaActual(fecha: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val fechaActual = LocalDate.now()
    return LocalDate.parse(fecha, formatter) <= fechaActual
}