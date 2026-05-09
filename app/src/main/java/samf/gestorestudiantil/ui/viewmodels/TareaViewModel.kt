package samf.gestorestudiantil.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.widget.Toast
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
import samf.gestorestudiantil.domain.repositories.NotificationRepository
import samf.gestorestudiantil.domain.repositories.TareaRepository
import samf.gestorestudiantil.domain.utils.FileOpener
import samf.gestorestudiantil.domain.utils.SnackbarManager
import samf.gestorestudiantil.R
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TareaViewModel @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val notificationRepository: NotificationRepository,
    private val snackbarManager: SnackbarManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var titulo by mutableStateOf("")
    var descripcion by mutableStateOf("")
    var visible by mutableStateOf(true)
    var fechaLimite by mutableStateOf(Date(System.currentTimeMillis() + 86400000))
    
    var selectedFileUri by mutableStateOf<Uri?>(null)
    var selectedFileName by mutableStateOf("")
    var selectedFileSize by mutableLongStateOf(0L)
    var selectedFileData by mutableStateOf<ByteArray?>(null)
    var selectedMimeType by mutableStateOf("")

    var isLoading by mutableStateOf(false)

    private var tareaOriginal: Tarea? = null

    fun inicializarCon(tarea: Tarea?) {
        tareaOriginal = tarea
        titulo = tarea?.titulo ?: ""
        descripcion = tarea?.descripcion ?: ""
        visible = tarea?.visible ?: true
        fechaLimite = tarea?.fechaLimiteEntrega?.toDate() ?: Date(System.currentTimeMillis() + 86400000)
        selectedFileName = tarea?.adjunto?.nombreArchivo ?: ""
        selectedFileSize = tarea?.adjunto?.pesoBytes ?: 0L
        selectedFileUri = null
        selectedFileData = null
        selectedMimeType = ""
    }

    fun onFileSelected(uri: Uri?) {
        uri?.let {
            selectedFileUri = it
            val contentResolver = context.contentResolver
            
            try {
                val cursor = contentResolver.query(it, null, null, null, null)
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
            } catch (_: Exception) { }

            if (selectedFileName.isEmpty()) {
                selectedFileName = it.lastPathSegment ?: "archivo"
            }

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val initialMimeType = contentResolver.getType(it) ?: FileOpener.getMimeType(selectedFileName)
                    
                    val bytes = if (initialMimeType.contains("google-apps")) {
                        val streamTypes = contentResolver.getStreamTypes(it, "*/*")
                        val hasDocx = streamTypes?.any { type -> type.contains("wordprocessingml.document") } == true
                        
                        val exportType = when {
                            initialMimeType.contains("document") && hasDocx -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            initialMimeType.contains("document") -> "application/msword"
                            initialMimeType.contains("spreadsheet") -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            initialMimeType.contains("presentation") -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                            else -> "application/pdf"
                        }
                        
                        contentResolver.openTypedAssetFileDescriptor(it, exportType, null)?.use { ad ->
                            selectedMimeType = exportType
                            ad.createInputStream().use { stream -> stream.readBytes() }
                        }
                    } else {
                        selectedMimeType = initialMimeType
                        contentResolver.openInputStream(it)?.use { stream -> stream.readBytes() }
                    }

                    if (bytes != null) {
                        selectedFileData = bytes
                        if (selectedFileSize <= 0) selectedFileSize = bytes.size.toLong()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al cargar archivo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun removeFile() {
        selectedFileUri = null
        selectedFileData = null
        selectedMimeType = ""
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
        acronimoAsignatura: String,
        profesorId: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                val nuevaTarea = (tareaOriginal ?: Tarea()).copy(
                    titulo = titulo,
                    descripcion = descripcion,
                    asignaturaId = asignaturaId,
                    unidadId = unidadId,
                    profesorId = profesorId.ifBlank { tareaOriginal?.profesorId ?: "" },
                    centroId = tareaOriginal?.centroId ?: "",
                    fechaLimiteEntrega = Timestamp(fechaLimite),
                    visible = visible
                )

                if (tareaOriginal == null) {
                    val tareaId = tareaRepository.crearTarea(nuevaTarea, selectedFileData, selectedFileName, selectedMimeType)
                    
                    snackbarManager.showSnackbar(
                        message = context.getString(R.string.success_task_created),
                        actionLabel = context.getString(R.string.label_undo),
                        onAction = {
                            viewModelScope.launch {
                                tareaRepository.eliminarTarea(nuevaTarea.copy(id = tareaId))
                            }
                        }
                    )
                    
                    if (visible) {
                        enviarNotificacion(asignaturaId, titulo, acronimoAsignatura, profesorId)
                    }
                } else {
                    tareaRepository.editarTarea(nuevaTarea, selectedFileData, selectedFileName, selectedMimeType)
                    snackbarManager.showSnackbar(context.getString(R.string.success_update))
                }
                
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al guardar la tarea", Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    private fun enviarNotificacion(asignaturaId: String, tituloTarea: String, acronimoAsignatura: String, profesorId: String) {
        viewModelScope.launch {
            try {
                val topic = "asignatura_${asignaturaId}_estudiantes"
                val title = "Nueva Tarea en $acronimoAsignatura"
                val data = mapOf(
                    "target_asignatura_id" to asignaturaId,
                    "sender_id" to profesorId
                )
                notificationRepository.sendTopicNotification(topic, title, tituloTarea, data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
