package com.app.autologinucab.domain.usecase

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Allows at most one login attempt at a time across the UI, the tile and the
 * background network callback.
 */
class LoginSingleFlight {

    private val running = AtomicBoolean(false)

    fun tryAcquire(): Boolean = running.compareAndSet(false, true)

    fun release() {
        running.set(false)
    }
}
