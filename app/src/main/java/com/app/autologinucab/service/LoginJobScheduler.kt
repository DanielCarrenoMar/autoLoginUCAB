package com.app.autologinucab.service

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import com.app.autologinucab.domain.model.LoginTrigger

/**
 * Schedules one login attempt as a system job. Jobs give the work enough background time
 * without a foreground service, which is what keeps the notification bar free.
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
        val info = JobInfo.Builder(JOB_ID, ComponentName(context, LoginJobService::class.java))
            .setExtras(extras)
            .setOverrideDeadline(0)
            .build()

        try {
            scheduler.schedule(info)
        } catch (_: IllegalStateException) {
            // The system refused the job. The durable status and the in-app button remain.
        }
    }
}
