package com.app.autologinucab.tile

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.app.autologinucab.presentation.WebLoginActivity

class AutoLoginTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, WebLoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityPlus34(intent)
        } else {
            startActivityBellow34(intent)
        }
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    fun startActivityBellow34(intent: Intent) {
        startActivityAndCollapse(intent)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun startActivityPlus34(intent: Intent) {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        startActivityAndCollapse(pendingIntent)
    }
}

