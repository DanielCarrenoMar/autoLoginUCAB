package com.app.autologinucab.domain.model

/**
 * Who asked for the login. Manual actions from the UI and the quick-settings tile
 * are always honored; the allowed-SSID gate only applies to automatic triggers.
 */
enum class LoginTrigger {
    MANUAL,
    TILE,
    AUTOMATIC
}
