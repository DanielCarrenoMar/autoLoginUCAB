package com.app.autologinucab

import android.app.Application
import com.app.autologinucab.di.AppContainer

class App : Application() {

    val container: AppContainer by lazy { AppContainer(this) }
}
