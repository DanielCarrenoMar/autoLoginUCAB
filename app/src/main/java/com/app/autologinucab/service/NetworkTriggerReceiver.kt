package com.app.autologinucab.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.autologinucab.domain.model.LoginTrigger

/**
 * Receives the system's Wi-Fi-available callback and schedules one automatic attempt.
 * The allowed-network filter is applied inside the job, where the SSID can be read.
 */
class NetworkTriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        LoginJobScheduler.enqueue(context, LoginTrigger.AUTOMATIC)
    }
}
