package com.app.autologinucab.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.app.autologinucab.domain.repository.InternetValidator

/**
 * Checks Android's validated-network state for the Wi-Fi network used by the POST.
 * When no Wi-Fi network was bound (manual fallback) it checks the active network.
 */
class ConnectivityInternetValidator(
    context: Context,
    private val boundWifiNetwork: Network?
) : InternetValidator {

    private val connectivityManager: ConnectivityManager? =
        context.applicationContext.getSystemService(ConnectivityManager::class.java)

    override fun awaitValidated(timeoutMillis: Long): Boolean {
        val manager = connectivityManager ?: return false

        return PollingInternetValidator(
            isInternetAvailable = {
                val target = boundWifiNetwork ?: manager.activeNetwork
                val capabilities = target?.let { manager.getNetworkCapabilities(it) }
                capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
            }
        ).awaitValidated(timeoutMillis)
    }
}
