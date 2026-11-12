package com.app.autologinucab.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Restarts the foreground automation after a reboot or an app update so the
 * allowed-SSID callback is active without opening the UI.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> AutoLoginService.ensureRunning(context)
        }
    }
}
