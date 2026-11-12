package com.app.autologinucab.domain.usecase

/**
 * Prevents repeated automatic attempts for the same connection. A reconnect, a
 * different SSID, or the retry window reopening allows a new attempt.
 */
class RepeatedConnectionGuard(
    private val retryWindowMillis: Long = DEFAULT_WINDOW_MILLIS
) {

    private var lastSsid: String? = null
    private var lastAttemptAt: Long = 0L

    @Synchronized
    fun shouldAttempt(ssid: String, nowMillis: Long): Boolean {
        val sameConnection = lastSsid == ssid && nowMillis - lastAttemptAt < retryWindowMillis
        if (sameConnection) return false

        lastSsid = ssid
        lastAttemptAt = nowMillis
        return true
    }

    @Synchronized
    fun reset() {
        lastSsid = null
        lastAttemptAt = 0L
    }

    companion object {
        const val DEFAULT_WINDOW_MILLIS = 60_000L
    }
}
