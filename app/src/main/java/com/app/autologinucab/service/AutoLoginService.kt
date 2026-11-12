package com.app.autologinucab.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.app.autologinucab.App
import com.app.autologinucab.R
import com.app.autologinucab.domain.model.LoginResult
import com.app.autologinucab.domain.model.LoginResultType
import com.app.autologinucab.domain.model.LoginTrigger
import com.app.autologinucab.domain.model.SsidRules
import com.app.autologinucab.domain.usecase.RepeatedConnectionGuard
import com.app.autologinucab.presentation.MainActivity
import com.app.autologinucab.presentation.ui.LoginResultFormatter
import java.util.concurrent.Executors

/**
 * Keeps the Wi-Fi automation alive while the app is closed. It listens for Wi-Fi
 * networks, runs the shared coordinator when an allowed SSID connects, and exposes
 * the last non-sensitive result both in its notification and the durable status store.
 */
class AutoLoginService : Service() {

    private val container by lazy { (application as App).container }
    private val worker = Executors.newSingleThreadExecutor()
    private val connectionGuard = RepeatedConnectionGuard()

    private val connectivityManager: ConnectivityManager? by lazy {
        applicationContext.getSystemService(ConnectivityManager::class.java)
    }
    private val wifiManager: WifiManager? by lazy {
        applicationContext.getSystemService(WifiManager::class.java)
    }

    private var callbackRegistered = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            inspectNetwork(network, null)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            inspectNetwork(network, networkCapabilities)
        }

        override fun onLost(network: Network) {
            connectionGuard.reset()
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        clearStaleRunningStatus()
        updateNotificationFromStatus()
        registerWifiCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_RUN_MANUAL -> runLogin(LoginTrigger.MANUAL)
            ACTION_RUN_TILE -> runLogin(LoginTrigger.TILE)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        unregisterWifiCallback()
        worker.shutdownNow()
        super.onDestroy()
    }

    private fun registerWifiCallback() {
        if (callbackRegistered) return
        val manager = connectivityManager ?: return

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        try {
            manager.registerNetworkCallback(request, networkCallback)
            callbackRegistered = true
        } catch (_: SecurityException) {
            callbackRegistered = false
        } catch (_: IllegalArgumentException) {
            callbackRegistered = false
        }
    }

    private fun unregisterWifiCallback() {
        if (!callbackRegistered) return
        val manager = connectivityManager ?: return

        try {
            manager.unregisterNetworkCallback(networkCallback)
        } catch (_: IllegalArgumentException) {
            // Already unregistered.
        } finally {
            callbackRegistered = false
        }
    }

    private fun inspectNetwork(network: Network, capabilities: NetworkCapabilities?) {
        val resolved = capabilities
            ?: connectivityManager?.getNetworkCapabilities(network)
            ?: return
        if (!resolved.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return

        val ssid = readSsid(resolved) ?: return
        if (!container.allowedNetworksRepository.isAllowed(ssid)) return
        if (!connectionGuard.shouldAttempt(ssid, System.currentTimeMillis())) return

        runLogin(LoginTrigger.AUTOMATIC)
    }

    private fun readSsid(capabilities: NetworkCapabilities): String? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val info = capabilities.transportInfo as? WifiInfo
            SsidRules.normalize(info?.ssid)
        } else {
            @Suppress("DEPRECATION")
            SsidRules.normalize(wifiManager?.connectionInfo?.ssid)
        }
    } catch (_: SecurityException) {
        null
    }

    private fun runLogin(trigger: LoginTrigger) {
        startOrUpdateForeground(getString(R.string.service_notification_running))

        worker.execute {
            container.loginCoordinator.run(trigger)
            updateNotificationFromStatus()
        }
    }

    private fun updateNotificationFromStatus() {
        val attempt = container.loginStatusRepository.current()
        val text = if (attempt == null) {
            getString(R.string.service_notification_idle)
        } else {
            LoginResultFormatter.text(this, attempt)
        }
        startOrUpdateForeground(text)
    }

    /**
     * A process death during an attempt leaves RUNNING persisted forever, which would
     * keep the UI busy and disabled. The service owns the attempt, so on a fresh
     * instance the stale marker is replaced with an honest interrupted result.
     */
    private fun clearStaleRunningStatus() {
        if (container.loginStatusRepository.current()?.type == LoginResultType.RUNNING) {
            container.loginStatusRepository.record(LoginResult(LoginResultType.INTERRUPTED))
        }
    }

    private fun startOrUpdateForeground(text: String) {
        val notification = buildNotification(text)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(text: String): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_contactless_24)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(text)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.service_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.service_channel_description)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "auto_login_service"
        private const val NOTIFICATION_ID = 1001

        private const val ACTION_ENSURE_RUNNING = "com.app.autologinucab.action.ENSURE_RUNNING"
        private const val ACTION_RUN_MANUAL = "com.app.autologinucab.action.RUN_MANUAL"
        private const val ACTION_RUN_TILE = "com.app.autologinucab.action.RUN_TILE"

        /** Starts (or keeps) the automation service without running a login. */
        fun ensureRunning(context: Context) {
            startService(context, ACTION_ENSURE_RUNNING)
        }

        /** Runs one login on behalf of the UI or the quick-settings tile. */
        fun startManual(context: Context, trigger: LoginTrigger = LoginTrigger.MANUAL) {
            val action = if (trigger == LoginTrigger.TILE) ACTION_RUN_TILE else ACTION_RUN_MANUAL
            startService(context, action)
        }

        private fun startService(context: Context, action: String) {
            val intent = Intent(context, AutoLoginService::class.java).setAction(action)
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (_: IllegalStateException) {
                // The OS refused a background foreground-service start. The durable
                // status and the in-app button remain available.
            }
        }
    }
}
