package com.app.autologinucab.domain.usecase

import com.app.autologinucab.domain.model.LoginResult
import com.app.autologinucab.domain.model.LoginTrigger
import com.app.autologinucab.domain.repository.LoginStatusRepository

/**
 * Single entry point for every login trigger. It serializes attempts, applies the
 * allowed-SSID gate only to automatic triggers, and persists blocked results so the
 * durable status always explains why nothing was sent.
 */
class LoginCoordinator(
    private val singleFlight: LoginSingleFlight,
    private val automaticGate: AutomaticLoginGate,
    private val loginStatusRepository: LoginStatusRepository,
    private val executorFactory: () -> LoginExecutor
) {

    /** Returns null when another attempt is already running. */
    fun run(trigger: LoginTrigger): LoginResult? {
        if (!singleFlight.tryAcquire()) return null

        return try {
            val blocked = if (trigger == LoginTrigger.AUTOMATIC) {
                automaticGate.blockedResult()
            } else {
                null
            }

            if (blocked != null) {
                loginStatusRepository.record(blocked)
                blocked
            } else {
                executorFactory().execute()
            }
        } finally {
            singleFlight.release()
        }
    }
}
