package com.app.autologinucab.domain.repository

interface InternetValidator {
    fun awaitValidated(timeoutMillis: Long): Boolean
}
