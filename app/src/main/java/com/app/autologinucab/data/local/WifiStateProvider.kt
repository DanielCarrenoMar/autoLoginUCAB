package com.app.autologinucab.data.local

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.app.autologinucab.domain.model.SsidRules
import com.app.autologinucab.domain.repository.WifiStatusProvider

class WifiStateProvider(context: Context) : WifiStatusProvider {

    private val appContext = context.applicationContext
    private val wifiManager: WifiManager? =
        appContext.getSystemService(WifiManager::class.java)
    private val locationManager: LocationManager? =
        appContext.getSystemService(LocationManager::class.java)

    override fun hasSsidPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED
        }

        return true
    }

    override fun isLocationEnabled(): Boolean {
        val manager = locationManager ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            manager.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    @Suppress("DEPRECATION")
    override fun currentSsid(): String? {
        if (!hasSsidPermission()) return null

        return try {
            val info = wifiManager?.connectionInfo ?: return null
            SsidRules.normalize(info.ssid)
        } catch (_: SecurityException) {
            null
        }
    }
}
