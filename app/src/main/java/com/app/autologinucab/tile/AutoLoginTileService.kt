package com.app.autologinucab.tile

import android.service.quicksettings.TileService
import com.app.autologinucab.domain.model.LoginTrigger
import com.app.autologinucab.service.AutoLoginService

/**
 * Quick-settings entry point. It enqueues the same direct login as the main button
 * instead of opening the removed WebView flow.
 */
class AutoLoginTileService : TileService() {

    override fun onClick() {
        super.onClick()
        AutoLoginService.startManual(this, LoginTrigger.TILE)
    }
}
