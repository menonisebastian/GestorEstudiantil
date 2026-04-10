package samf.gestorestudiantil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.cloudinary.android.MediaManager
import dagger.hilt.android.AndroidEntryPoint
import samf.gestorestudiantil.ui.navigation.AppNavigation
import samf.gestorestudiantil.ui.theme.GestorEstudiantilTheme


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        try {
            val config = mapOf("cloud_name" to "dywawleqm")
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // Ignorar si ya está inicializado
        }
        setContent {
            GestorEstudiantilTheme {
                AppNavigation()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview()
{

}
