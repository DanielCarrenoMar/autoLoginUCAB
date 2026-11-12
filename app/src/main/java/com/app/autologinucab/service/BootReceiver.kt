package com.app.autologinucab.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Re-registers the system Wi-Fi callback after a reboot or an app update so the
 * allowed-SSID automation stays active without opening the UI.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> AutomationTrigger.register(context)
        }
    }
}
