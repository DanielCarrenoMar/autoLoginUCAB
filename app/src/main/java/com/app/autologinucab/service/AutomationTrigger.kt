package com.app.autologinucab.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest

/**
 * Asks the system to wake the app whenever a Wi-Fi network is available. The callback lives
 * in the system instead of a permanent foreground service, so the automation survives with
 * the app closed and without a pinned notification.
 */
object AutomationTrigger {

    private const val ACTION_WIFI_AVAILABLE = "com.app.autologinucab.action.WIFI_AVAILABLE"

    private var registered = false

    fun register(context: Context) {
        if (registered) return
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        try {
            manager.registerNetworkCallback(request, callbackIntent(context))
            registered = true
        } catch (_: IllegalArgumentException) {
            // The system already holds this callback for the current process.
            registered = true
        } catch (_: SecurityException) {
            registered = false
        }
    }

    private fun callbackIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, NetworkTriggerReceiver::class.java).setAction(ACTION_WIFI_AVAILABLE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
}
