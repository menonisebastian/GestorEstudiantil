package samf.gestorestudiantil.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import samf.gestorestudiantil.data.models.Tarea
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TareaViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Estado del Formulario
    var titulo by mutableStateOf("")
    var descripcion by mutableStateOf("")
    var visible by mutableStateOf(true)
    var fechaLimite by mutableStateOf(Date(System.currentTimeMillis() + 86400000))
    
    var selectedFileUri by mutableStateOf<Uri?>(null)
    var selectedFileName by mutableStateOf("")
    var selectedFileSize by mutableLongStateOf(0L)

    private var tareaOriginal: Tarea? = null

    fun inicializarCon(tarea: Tarea?) {
        if (tareaOriginal != null && tareaOriginal?.id == tarea?.id) return
        
        tareaOriginal = tarea
        titulo = tarea?.titulo ?: ""
        descripcion = tarea?.descripcion ?: ""
        visible = tarea?.visible ?: true
        fechaLimite = tarea?.fechaLimiteEntrega?.toDate() ?: Date(System.currentTimeMillis() + 86400000)
        selectedFileName = tarea?.adjunto?.nombreArchivo ?: ""
        selectedFileSize = tarea?.adjunto?.pesoBytes ?: 0L
        selectedFileUri = null
    }

    fun onFileSelected(uri: Uri?) {
        uri?.let {
            selectedFileUri = it
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (nameIndex != -1 && c.moveToFirst()) {
                    selectedFileName = c.getString(nameIndex)
                }
                if (sizeIndex != -1) {
                    selectedFileSize = c.getLong(sizeIndex)
                }
            }
            if (selectedFileName.isEmpty()) {
                selectedFileName = it.lastPathSegment ?: "archivo"
            }
        }
    }

    fun removeFile() {
        selectedFileUri = null
        selectedFileName = tareaOriginal?.adjunto?.nombreArchivo ?: ""
        selectedFileSize = tareaOriginal?.adjunto?.pesoBytes ?: 0L
    }

    fun updateFecha(newDate: Date) {
        val cal = Calendar.getInstance()
        cal.time = newDate
        val currentCal = Calendar.getInstance()
        currentCal.time = fechaLimite
        currentCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        fechaLimite = currentCal.time
    }

    fun updateHora(hour: Int, minute: Int) {
        val cal = Calendar.getInstance()
        cal.time = fechaLimite
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        fechaLimite = cal.time
    }

    fun save(
        asignaturaId: String, 
        unidadId: String, 
        onComplete: (Tarea, ByteArray?, String?, String?) -> Unit
    ) {
        viewModelScope.launch {
            val nuevaTarea = (tareaOriginal ?: Tarea()).copy(
                titulo = titulo,
                descripcion = descripcion,
                asignaturaId = asignaturaId,
                unidadId = unidadId,
                profesorId = tareaOriginal?.profesorId ?: "",
                centroId = tareaOriginal?.centroId ?: "",
                fechaLimiteEntrega = Timestamp(fechaLimite),
                visible = visible
            )

            var fileData: ByteArray? = null
            var mimeType: String? = null

            if (selectedFileUri != null) {
                withContext(Dispatchers.IO) {
                    try {
                        fileData = context.contentResolver.openInputStream(selectedFileUri!!)?.use { it.readBytes() }
                        mimeType = context.contentResolver.getType(selectedFileUri!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            onComplete(nuevaTarea, fileData, selectedFileName, mimeType)
        }
    }
}
