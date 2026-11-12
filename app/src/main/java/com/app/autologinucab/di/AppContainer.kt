package com.app.autologinucab.di

import android.content.Context
import com.app.autologinucab.data.local.SharedPreferencesLoginStatusRepository
import com.app.autologinucab.data.local.SharedPreferencesSsidSettingsStore
import com.app.autologinucab.data.local.WifiStateProvider
import com.app.autologinucab.data.remote.ConnectivityInternetValidator
import com.app.autologinucab.data.remote.HttpPortalTransport
import com.app.autologinucab.data.remote.WifiNetworkSelector
import com.app.autologinucab.data.repository.AllowedNetworksRepositoryImpl
import com.app.autologinucab.data.repository.CredentialsRepositoryImpl
import com.app.autologinucab.domain.repository.AllowedNetworksRepository
import com.app.autologinucab.domain.repository.CredentialsRepository
import com.app.autologinucab.domain.repository.LoginStatusRepository
import com.app.autologinucab.domain.repository.WifiStatusProvider
import com.app.autologinucab.domain.usecase.AutomaticLoginGate
import com.app.autologinucab.domain.usecase.ClearCredentialsUseCase
import com.app.autologinucab.domain.usecase.GetCredentialsUseCase
import com.app.autologinucab.domain.usecase.LoginCoordinator
import com.app.autologinucab.domain.usecase.LoginExecutor
import com.app.autologinucab.domain.usecase.LoginSingleFlight
import com.app.autologinucab.domain.usecase.SaveCredentialsUseCase

/**
 * Manual dependency graph shared by the activity, the tile and the background service.
 * The coordinator is a singleton so the single-flight guard spans every trigger.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val credentialsRepository: CredentialsRepository =
        CredentialsRepositoryImpl(appContext)

    val getCredentialsUseCase = GetCredentialsUseCase(credentialsRepository)
    val saveCredentialsUseCase = SaveCredentialsUseCase(credentialsRepository)
    val clearCredentialsUseCase = ClearCredentialsUseCase(credentialsRepository)

    val loginStatusRepository: LoginStatusRepository =
        SharedPreferencesLoginStatusRepository(appContext)

    val allowedNetworksRepository: AllowedNetworksRepository =
        AllowedNetworksRepositoryImpl(SharedPreferencesSsidSettingsStore(appContext))

    val wifiStatusProvider: WifiStatusProvider = WifiStateProvider(appContext)

    val loginCoordinator: LoginCoordinator = LoginCoordinator(
        singleFlight = LoginSingleFlight(),
        automaticGate = AutomaticLoginGate(allowedNetworksRepository, wifiStatusProvider),
        loginStatusRepository = loginStatusRepository,
        executorFactory = {
            val wifiNetwork = WifiNetworkSelector(appContext).currentWifiNetwork()
            LoginExecutor(
                credentialsRepository = credentialsRepository,
                portalTransport = HttpPortalTransport(networkProvider = { wifiNetwork }),
                internetValidator = ConnectivityInternetValidator(appContext, wifiNetwork),
                loginStatusRepository = loginStatusRepository
            )
        }
    )
}
