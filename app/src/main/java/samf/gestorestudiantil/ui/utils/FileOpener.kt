package samf.gestorestudiantil.ui.utils

import android.content.Context
import android.content.Intent
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

            val mimeType = context.contentResolver.getType(uri) ?: when (file.extension.lowercase()) {
                "pdf" -> "application/pdf"
                "doc", "docx" -> "application/msword"
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                else -> "*/*"
            }

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
}
