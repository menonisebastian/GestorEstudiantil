package samf.gestorestudiantil.ui.utils

import android.content.Context
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import samf.gestorestudiantil.R

object ErrorMapper {
    fun getFriendlyMessage(context: Context, exception: Exception?): String {
        if (exception == null) return context.getString(R.string.error_generic)
        
        return when (exception) {
            is FirebaseNetworkException -> context.getString(R.string.error_network)
            is FirebaseAuthInvalidCredentialsException -> context.getString(R.string.error_auth_failed)
            is FirebaseAuthInvalidUserException -> context.getString(R.string.error_auth_failed)
            is FirebaseAuthUserCollisionException -> context.getString(R.string.error_email_already_in_use)
            else -> context.getString(R.string.error_generic)
        }
    }
}
