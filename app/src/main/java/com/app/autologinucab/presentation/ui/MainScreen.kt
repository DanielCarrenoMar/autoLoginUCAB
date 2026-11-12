package com.app.autologinucab.presentation.ui

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.app.autologinucab.R
import com.app.autologinucab.domain.model.LoginAttempt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: MainScreenState,
    actions: MainScreenActions,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    TextButton(onClick = actions.onManageNetworks) {
                        Text(stringResource(R.string.action_manage_networks))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CredentialsCard(state = state, actions = actions)
            ConnectCard(state = state, actions = actions)
            PermissionCard(issue = state.permissionIssue, actions = actions)
            ResultCard(attempt = state.lastAttempt)
            PortalCard()
        }
    }
}

@Composable
private fun CredentialsCard(
    state: MainScreenState,
    actions: MainScreenActions
) {
    var showClearDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.credentials_title),
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = state.username,
                onValueChange = actions.onUsernameChange,
                label = { Text(stringResource(R.string.credentials_username)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = actions.onPasswordChange,
                label = { Text(stringResource(R.string.credentials_password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                placeholder = {
                    if (state.hasSavedPassword) {
                        Text(stringResource(R.string.credentials_password_keep_hint))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = when {
                    state.savedUsername == null -> stringResource(R.string.credentials_required_for_login)
                    state.hasSavedPassword -> stringResource(R.string.credentials_saved_password)
                    else -> stringResource(R.string.credentials_no_saved_password)
                },
                style = MaterialTheme.typography.bodySmall
            )

            state.savedUsername?.let { saved ->
                Text(
                    text = stringResource(R.string.credentials_saved_as, saved),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = actions.onSave) {
                    Text(stringResource(R.string.action_save))
                }
                TextButton(onClick = { showClearDialog = true }) {
                    Text(stringResource(R.string.action_clear_credentials))
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_title)) },
            text = { Text(stringResource(R.string.clear_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        actions.onClearCredentials()
                    }
                ) {
                    Text(stringResource(R.string.action_clear_credentials))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun PermissionCard(
    issue: PermissionIssue,
    actions: MainScreenActions
) {
    if (issue == PermissionIssue.NONE) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.permission_title),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = when (issue) {
                    PermissionIssue.MISSING_PERMISSION -> stringResource(R.string.permission_missing)
                    PermissionIssue.LOCATION_DISABLED -> stringResource(R.string.permission_location_disabled)
                    PermissionIssue.NONE -> ""
                }
            )

            if (issue == PermissionIssue.MISSING_PERMISSION) {
                Button(onClick = actions.onRequestPermission) {
                    Text(stringResource(R.string.action_grant_permission))
                }
            } else {
                Button(onClick = actions.onOpenLocationSettings) {
                    Text(stringResource(R.string.action_open_location_settings))
                }
            }
        }
    }
}

@Composable
private fun ConnectCard(
    state: MainScreenState,
    actions: MainScreenActions
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = actions.onLogin,
                enabled = !state.busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (state.busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(R.string.action_connect_now),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            state.message?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ResultCard(attempt: LoginAttempt?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.result_title),
                style = MaterialTheme.typography.titleSmall
            )

            if (attempt == null) {
                Text(text = stringResource(R.string.result_never))
            } else {
                val context = LocalContext.current
                Text(text = LoginResultFormatter.text(context, attempt))
                Text(
                    text = DateUtils.getRelativeTimeSpanString(
                        attempt.timestamp,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    ).toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun PortalCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.portal_title),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = stringResource(R.string.portal_url),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.portal_transport_warning),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
