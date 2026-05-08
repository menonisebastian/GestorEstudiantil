package samf.gestorestudiantil.domain.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.data.models.Tarea
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object NotificationScheduler {

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleRecordatorioNotification(context: Context, recordatorio: Recordatorio) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", "Recordatorio: ${recordatorio.titulo}")
            putExtra("message", recordatorio.descripcion)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            recordatorio.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        try {
            val dateTimeString = "${recordatorio.fecha} ${recordatorio.hora}".replace("-", "/")
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            val date = sdf.parse(dateTimeString)
            if (date != null) {
                calendar.time = date
                
                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleTareaNotification(context: Context, tarea: Tarea) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", "Tarea pendiente: ${tarea.titulo}")
            putExtra("message", "La fecha límite es hoy")
            putExtra("target_asignatura_id", tarea.asignaturaId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            tarea.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        val limitDate = tarea.fechaLimiteEntrega.toDate()
        calendar.time = limitDate
        
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        var scheduleTime = calendar.timeInMillis
        val currentTime = System.currentTimeMillis()

        if (scheduleTime <= currentTime) {
            val today = Calendar.getInstance()
            val limitCal = Calendar.getInstance().apply { time = limitDate }
            
            val isSameDay = today.get(Calendar.YEAR) == limitCal.get(Calendar.YEAR) &&
                           today.get(Calendar.DAY_OF_YEAR) == limitCal.get(Calendar.DAY_OF_YEAR)
            
            if (isSameDay && limitDate.time > currentTime) {
                scheduleTime = currentTime + (2 * 60 * 1000)
            } else {
                return
            }
        }

        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                scheduleTime,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                scheduleTime,
                pendingIntent
            )
        }
    }

    fun cancelNotification(context: Context, id: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
