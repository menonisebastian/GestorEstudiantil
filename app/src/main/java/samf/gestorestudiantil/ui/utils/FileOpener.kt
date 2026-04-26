package samf.gestorestudiantil.ui.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import samf.gestorestudiantil.R

object FileOpener {
    fun openFile(context: Context, bytes: ByteArray, nombreArchivo: String) {
        try {
            val file = File(context.cacheDir, nombreArchivo)
            FileOutputStream(file).use { it.write(bytes) }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val mimeType = context.contentResolver.getType(uri) ?: getMimeType(nombreArchivo)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, context.getString(R.string.open_with))
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun downloadFile(context: Context, bytes: ByteArray, nombreArchivo: String) {
        try {
            val mimeType = getMimeType(nombreArchivo)
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            val outputStream = uri?.let { context.contentResolver.openOutputStream(it) }

            outputStream?.use { it.write(bytes) }
            Toast.makeText(context, "Archivo guardado en Descargas", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al descargar: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMimeType(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "pdf" -> "application/pdf"
            "doc", "docx" -> "application/msword"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "zip" -> "application/zip"
            else -> "*/*"
        }
    }
}
