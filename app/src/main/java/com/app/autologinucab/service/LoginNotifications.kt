package com.app.autologinucab.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.app.autologinucab.R
import com.app.autologinucab.domain.model.LoginResult
import com.app.autologinucab.presentation.MainActivity
import com.app.autologinucab.presentation.ui.LoginResultFormatter

/**
 * Event notifications for the automation: connecting, connected, or failed. They are ordinary
 * notifications (not ongoing), so the user can swipe them away at any moment, and nothing is
 * shown while no attempt is running.
 */
object LoginNotifications {

    private const val CHANNEL_ID = "auto_login_results"
    private const val LEGACY_CHANNEL_ID = "auto_login_service"
    private const val NOTIFICATION_ID = 1001

    /** Removes the channel the previous foreground-service notification created. */
    fun deleteLegacyChannel(context: Context) {
        context.getSystemService(NotificationManager::class.java)
            ?.deleteNotificationChannel(LEGACY_CHANNEL_ID)
    }

    fun showConnecting(context: Context) {
        show(context, context.getString(R.string.login_result_running))
    }

    fun showResult(context: Context, result: LoginResult) {
        show(context, LoginResultFormatter.text(context, result.type, result.httpCode))
    }

    fun cancel(context: Context) {
        context.getSystemService(NotificationManager::class.java)?.cancel(NOTIFICATION_ID)
    }

    private fun show(context: Context, text: String) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(channel(context))

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_contactless_24)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(text)
            .setContentIntent(contentIntent)
            .setOngoing(false)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun channel(context: Context) = NotificationChannel(
        CHANNEL_ID,
        context.getString(R.string.notification_channel_name),
        NotificationManager.IMPORTANCE_LOW
    ).apply {
        description = context.getString(R.string.notification_channel_description)
        setShowBadge(false)
    }
}
