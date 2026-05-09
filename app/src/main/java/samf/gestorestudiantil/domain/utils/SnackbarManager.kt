package samf.gestorestudiantil.domain.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

data class SnackbarEvent(
    val message: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

@Singleton
class SnackbarManager @Inject constructor() {
    private val _events = MutableSharedFlow<SnackbarEvent>()
    val events: SharedFlow<SnackbarEvent> = _events.asSharedFlow()

    suspend fun showSnackbar(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        _events.emit(SnackbarEvent(message, actionLabel, onAction))
    }
}
