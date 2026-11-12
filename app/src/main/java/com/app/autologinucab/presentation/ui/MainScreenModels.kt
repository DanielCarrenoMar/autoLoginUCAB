package com.app.autologinucab.presentation.ui

import com.app.autologinucab.domain.model.LoginAttempt

enum class PermissionIssue {
    NONE,
    MISSING_PERMISSION,
    LOCATION_DISABLED
}

data class MainScreenState(
    val username: String = "",
    val password: String = "",
    val savedUsername: String? = null,
    val hasSavedPassword: Boolean = false,
    val busy: Boolean = false,
    val permissionIssue: PermissionIssue = PermissionIssue.NONE,
    val message: String? = null,
    val lastAttempt: LoginAttempt? = null
)

data class MainScreenActions(
    val onUsernameChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onSave: () -> Unit = {},
    val onLogin: () -> Unit = {},
    val onClearCredentials: () -> Unit = {},
    val onManageNetworks: () -> Unit = {},
    val onRequestPermission: () -> Unit = {},
    val onOpenLocationSettings: () -> Unit = {}
)

data class NetworksScreenState(
    val ssids: List<String> = emptyList(),
    val newSsid: String = "",
    val message: String? = null
)

data class NetworksScreenActions(
    val onNewSsidChange: (String) -> Unit = {},
    val onAdd: () -> Unit = {},
    val onRemove: (String) -> Unit = {},
    val onBack: () -> Unit = {}
)
