package samf.gestorestudiantil.ui.dialogs

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import samf.gestorestudiantil.R
import samf.gestorestudiantil.ui.components.IconLogo
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AppViewModel
import androidx.core.net.toUri

@Composable
fun HelpDialog(
    onDismissRequest: () -> Unit,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val githubLink = stringResource(R.string.github_link)
    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.8.0"
    } catch (_: Exception) {
        "0.7.6"
    }

    val isChecking by appViewModel.isCheckingUpdate.collectAsState()
    val updateMessage by appViewModel.updateMessage.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            appViewModel.clearUpdateMessage()
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { IconLogo(60.dp) },
        title = {
            Text(
                text = stringResource(R.string.help_title),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.help_version, versionName),
                    fontSize = 14.sp,
                    color = textColor
                )

                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, githubLink.toUri())
                    context.startActivity(intent)
                }) {
                    Text(text = stringResource(R.string.help_github_repo), color = primaryColor)
                }

                Button(
                    onClick = {
                        appViewModel.checkLatestVersion(versionName)
                    },
                    enabled = !isChecking,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = androidx.compose.ui.graphics.Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.help_check_update))
                    }
                }

                updateMessage?.let {
                    Text(
                        text = it,
                        color = if (it == stringResource(R.string.help_up_to_date)) textColor else primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.help_developed_by),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = surfaceDimColor
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.label_close), color = primaryColor)
            }
        },
        containerColor = samf.gestorestudiantil.ui.theme.surfaceColor
    )
}
