package com.app.autologinucab.data.remote

import com.app.autologinucab.domain.repository.InternetValidator

/**
 * Bounded polling around an injected availability probe. The clock and sleep are
 * injectable so the wait behavior is deterministic in JVM tests.
 */
class PollingInternetValidator(
    private val isInternetAvailable: () -> Boolean,
    private val intervalMillis: Long = DEFAULT_INTERVAL_MILLIS,
    private val nowMillis: () -> Long = System::currentTimeMillis,
    private val sleep: (Long) -> Unit = { millis -> Thread.sleep(millis) }
) : InternetValidator {

    override fun awaitValidated(timeoutMillis: Long): Boolean {
        val start = nowMillis()
        while (true) {
            if (isInternetAvailable()) return true

            val elapsed = nowMillis() - start
            if (elapsed >= timeoutMillis) return false

            sleep(intervalMillis.coerceAtMost(timeoutMillis - elapsed))
        }
    }

    companion object {
        const val DEFAULT_INTERVAL_MILLIS = 500L
    }
}
