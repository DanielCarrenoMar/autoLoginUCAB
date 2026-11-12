package com.app.autologinucab.presentation

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.app.autologinucab.App
import com.app.autologinucab.R
import com.app.autologinucab.di.AppContainer
import com.app.autologinucab.domain.model.Credentials
import com.app.autologinucab.domain.model.LoginAttempt
import com.app.autologinucab.domain.model.LoginResult
import com.app.autologinucab.domain.model.LoginResultType
import com.app.autologinucab.presentation.ui.MainScreen
import com.app.autologinucab.presentation.ui.MainScreenActions
import com.app.autologinucab.presentation.ui.MainScreenState
import com.app.autologinucab.presentation.ui.NetworksScreen
import com.app.autologinucab.presentation.ui.NetworksScreenActions
import com.app.autologinucab.presentation.ui.NetworksScreenState
import com.app.autologinucab.presentation.ui.PermissionIssue
import com.app.autologinucab.presentation.ui.theme.AutoLoginUCABTheme
import com.app.autologinucab.service.AutoLoginService
import java.security.GeneralSecurityException
import kotlinx.coroutines.delay

private const val SCREEN_HOME = "home"
private const val SCREEN_NETWORKS = "networks"
private const val STATUS_REFRESH_MILLIS = 3_000L
private const val STALE_RUNNING_MILLIS = 60_000L

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as App).container
        AutoLoginService.ensureRunning(this)

        setContent {
            AutoLoginUCABTheme {
                AutoLoginApp(container = container)
            }
        }
    }
}

@Composable
private fun AutoLoginApp(container: AppContainer) {
    val context = LocalContext.current

    var screenName by rememberSaveable { mutableStateOf(SCREEN_HOME) }
    var username by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var savedUsername by rememberSaveable { mutableStateOf<String?>(null) }
    var hasSavedPassword by rememberSaveable { mutableStateOf(false) }
    var ssids by remember { mutableStateOf(container.allowedNetworksRepository.allowedSsids()) }
    var newSsid by rememberSaveable { mutableStateOf("") }
    var networksMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var lastAttempt by remember {
        mutableStateOf<LoginAttempt?>(container.loginStatusRepository.current())
    }
    var permissionIssue by remember { mutableStateOf(permissionIssueOf(container)) }

    fun readStoredCredentials(): Credentials? = try {
        container.getCredentialsUseCase()
    } catch (_: GeneralSecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

    fun loadStoredState(): Credentials? {
        val stored = readStoredCredentials()
        savedUsername = stored?.username?.takeIf { it.isNotBlank() }
        hasSavedPassword = stored?.password?.isNotBlank() == true
        if (username.isBlank() && stored != null) {
            username = stored.username
        }
        return stored
    }

    fun saveCredentials(onSaved: () -> Unit = {}) {
        val stored = readStoredCredentials()
        val trimmedUsername = username.trim()
        val typedPassword = password

        if (trimmedUsername.isBlank()) {
            message = context.getString(R.string.error_username_required)
            return
        }
        if (typedPassword.isBlank() && stored?.password.isNullOrBlank()) {
            message = context.getString(R.string.error_password_required)
            return
        }

        val finalPassword = typedPassword.ifBlank { stored?.password.orEmpty() }

        try {
            container.saveCredentialsUseCase(
                Credentials(
                    username = trimmedUsername,
                    password = finalPassword,
                    url = stored?.url.orEmpty()
                )
            )
        } catch (_: GeneralSecurityException) {
            message = context.getString(R.string.error_credentials_storage)
            return
        } catch (_: IllegalArgumentException) {
            message = context.getString(R.string.error_credentials_storage)
            return
        }

        username = trimmedUsername
        password = ""
        message = context.getString(R.string.message_credentials_saved)
        loadStoredState()
        onSaved()
    }

    val requestedPermissions = remember {
        buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissionIssue = permissionIssueOf(container)
    }

    LaunchedEffect(Unit) {
        loadStoredState()
    }

    LaunchedEffect(Unit) {
        while (true) {
            permissionIssue = permissionIssueOf(container)

            val attempt = container.loginStatusRepository.current()
            if (attempt != null &&
                attempt.type == LoginResultType.RUNNING &&
                System.currentTimeMillis() - attempt.timestamp > STALE_RUNNING_MILLIS
            ) {
                container.loginStatusRepository.record(LoginResult(LoginResultType.INTERRUPTED))
            }
            lastAttempt = container.loginStatusRepository.current()

            delay(STATUS_REFRESH_MILLIS)
        }
    }

    val busy = lastAttempt?.type == LoginResultType.RUNNING

    val mainActions = MainScreenActions(
        onUsernameChange = { username = it },
        onPasswordChange = { password = it },
        onSave = { saveCredentials() },
        onLogin = {
            saveCredentials {
                AutoLoginService.startManual(context)
            }
        },
        onClearCredentials = {
            container.clearCredentialsUseCase()
            username = ""
            password = ""
            savedUsername = null
            hasSavedPassword = false
            message = context.getString(R.string.message_credentials_cleared)
        },
        onManageNetworks = {
            ssids = container.allowedNetworksRepository.allowedSsids()
            networksMessage = null
            screenName = SCREEN_NETWORKS
        },
        onRequestPermission = {
            permissionLauncher.launch(requestedPermissions)
        },
        onOpenLocationSettings = {
            try {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } catch (_: ActivityNotFoundException) {
                message = context.getString(R.string.permission_location_disabled)
            }
        }
    )

    val networkActions = NetworksScreenActions(
        onNewSsidChange = { newSsid = it },
        onAdd = {
            if (container.allowedNetworksRepository.add(newSsid)) {
                newSsid = ""
                networksMessage = null
                ssids = container.allowedNetworksRepository.allowedSsids()
            } else {
                networksMessage = context.getString(R.string.networks_add_error)
            }
        },
        onRemove = { ssid ->
            container.allowedNetworksRepository.remove(ssid)
            ssids = container.allowedNetworksRepository.allowedSsids()
        },
        onBack = { screenName = SCREEN_HOME }
    )

    if (screenName == SCREEN_NETWORKS) {
        BackHandler {
            screenName = SCREEN_HOME
        }
        NetworksScreen(
            state = NetworksScreenState(
                ssids = ssids,
                newSsid = newSsid,
                message = networksMessage
            ),
            actions = networkActions
        )
    } else {
        MainScreen(
            state = MainScreenState(
                username = username,
                password = password,
                savedUsername = savedUsername,
                hasSavedPassword = hasSavedPassword,
                busy = busy,
                permissionIssue = permissionIssue,
                message = message,
                lastAttempt = lastAttempt
            ),
            actions = mainActions
        )
    }
}

private fun permissionIssueOf(container: AppContainer): PermissionIssue = when {
    !container.wifiStatusProvider.hasSsidPermission() -> PermissionIssue.MISSING_PERMISSION
    !container.wifiStatusProvider.isLocationEnabled() -> PermissionIssue.LOCATION_DISABLED
    else -> PermissionIssue.NONE
}
