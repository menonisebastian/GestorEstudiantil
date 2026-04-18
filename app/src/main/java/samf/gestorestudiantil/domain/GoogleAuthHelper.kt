package samf.gestorestudiantil.domain

import android.content.Context
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

suspend fun signInWithGoogle(
    context: Context,
    credentialManager: CredentialManager,
    serverClientId: String
): String? {
    try {
        // 1. Configurar la opción de Google ID
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(true)
            .build()

        // 2. Crear la petición
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // 3. Llamar al Credential Manager (Esto suspende y muestra UI)
        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )

        // 4. Parsear el resultado
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return googleIdTokenCredential.idToken
        } else {
            return null
        }

    } catch (e: GetCredentialException) {
        return null
    } catch (e: GoogleIdTokenParsingException) {
        return null
    } catch (e: Exception) {
        return null
    }
}