package com.app.autologinucab.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

/**
 * Selects the currently connected Wi-Fi network so the portal POST is bound to it
 * instead of whichever network Android considers the default route.
 */
class WifiNetworkSelector(context: Context) {

    private val connectivityManager: ConnectivityManager? =
        context.applicationContext.getSystemService(ConnectivityManager::class.java)

    fun currentWifiNetwork(): Network? {
        val manager = connectivityManager ?: return null

        return try {
            manager.allNetworks.firstOrNull { network ->
                manager.getNetworkCapabilities(network)
                    ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            }
        } catch (_: SecurityException) {
            null
        }
    }
}
