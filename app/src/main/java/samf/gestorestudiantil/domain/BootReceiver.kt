package samf.gestorestudiantil.domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.AuthRepository
import samf.gestorestudiantil.domain.repositories.RecordatorioRepository
import samf.gestorestudiantil.domain.repositories.TareaRepository
import samf.gestorestudiantil.domain.repositories.UserRepository
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var recordatorioRepository: RecordatorioRepository

    @Inject
    lateinit var tareaRepository: TareaRepository

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Re-scheduling notifications after boot")
            
            val userId = authRepository.getCurrentUserUid()
            if (userId != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    // Reschedule Recordatorios
                    recordatorioRepository.getRecordatorios(userId).first().forEach { recordatorio ->
                        NotificationScheduler.scheduleRecordatorioNotification(context, recordatorio)
                    }

                    // Reschedule Tareas if student
                    val user = userRepository.getUser(userId)
                    if (user is User.Estudiante) {
                        // For students, we'd need their asignatura IDs to fetch tasks.
                        // This might be more complex depending on how getTareasPorAsignaturas works.
                        // For now, let's at least handle recordatorios which are easier.
                    }
                }
            }
        }
    }
}
