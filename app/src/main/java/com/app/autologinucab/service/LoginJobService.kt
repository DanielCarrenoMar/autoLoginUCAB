package com.app.autologinucab.service

import android.app.job.JobParameters
import android.app.job.JobService
import com.app.autologinucab.App
import com.app.autologinucab.domain.model.LoginTrigger
import com.app.autologinucab.domain.usecase.RepeatedConnectionGuard
import java.util.concurrent.Executors

/**
 * Runs one login attempt under the system job scheduler. Automatic attempts only run on an
 * allowed network and never twice for the same connection inside the retry window, which is
 * the same filter the previous foreground service applied.
 */
class LoginJobService : JobService() {

    private val container by lazy { (application as App).container }
    private val worker = Executors.newSingleThreadExecutor()
    private val connectionGuard = RepeatedConnectionGuard()

    override fun onStartJob(params: JobParameters): Boolean {
        val trigger = triggerOf(params)

        worker.execute {
            if (shouldRun(trigger)) {
                LoginNotifications.showConnecting(this)
                val result = container.loginCoordinator.run(trigger)
                if (result != null) {
                    LoginNotifications.showResult(this, result)
                } else {
                    LoginNotifications.cancel(this)
                }
            }
            jobFinished(params, false)
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        // Do not reschedule: the next Wi-Fi event triggers a new attempt.
        return false
    }

    override fun onDestroy() {
        worker.shutdownNow()
        super.onDestroy()
    }

    private fun triggerOf(params: JobParameters): LoginTrigger {
        val name = params.extras.getString(LoginJobScheduler.KEY_TRIGGER)
        return LoginTrigger.entries.firstOrNull { it.name == name } ?: LoginTrigger.AUTOMATIC
    }

    private fun shouldRun(trigger: LoginTrigger): Boolean {
        if (trigger != LoginTrigger.AUTOMATIC) return true

        val ssid = container.wifiStatusProvider.currentSsid() ?: return false
        if (!container.allowedNetworksRepository.isAllowed(ssid)) return false
        return connectionGuard.shouldAttempt(ssid, System.currentTimeMillis())
    }
}
