package com.app.autologinucab.service

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import com.app.autologinucab.domain.model.LoginTrigger

/**
 * Schedules one login attempt as a system job. Jobs give the work enough background time
 * without a foreground service, which is what keeps the notification bar free.
 *
 * The job is expedited: a regular job queued while the app is not in the foreground waits
 * for the app's standby-bucket quota (for example the RARE bucket), so the tile or the
 * automatic Wi-Fi callback could queue work that never started. An expedited job is not
 * subject to that quota and runs even while the device is in Doze.
 */
object LoginJobScheduler {

    private const val JOB_ID = 2001

    const val KEY_TRIGGER = "trigger"

    fun enqueue(context: Context, trigger: LoginTrigger) {
        val scheduler = context.getSystemService(JobScheduler::class.java) ?: return
        val pending = scheduler.getPendingJob(JOB_ID)

        if (pending != null) {
            // An explicit request must not wait behind an automatic attempt.
            if (trigger == LoginTrigger.AUTOMATIC) return
            scheduler.cancel(JOB_ID)
        }

        val extras = PersistableBundle().apply { putString(KEY_TRIGGER, trigger.name) }
        val builder = JobInfo.Builder(JOB_ID, ComponentName(context, LoginJobService::class.java))
            .setExtras(extras)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Expedited jobs ignore the standby-bucket quota and the deadline is not allowed
            // alongside them, so an explicit request cannot be deferred behind it.
            builder.setExpedited(true)
        } else {
            builder.setOverrideDeadline(0)
        }

        val info = builder.build()

        try {
            scheduler.schedule(info)
        } catch (_: IllegalStateException) {
            // The system refused the job. The durable status and the in-app button remain.
        }
    }
}
