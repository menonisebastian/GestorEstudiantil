package samf.gestorestudiantil

import android.app.Application
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GestorEstudiantilApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initCloudinary()
    }

    private fun initCloudinary() {
        try {
            val config = mapOf("cloud_name" to "dywawleqm")
            MediaManager.init(this, config)
        } catch (e: IllegalStateException) {
            // Ya inicializado
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
