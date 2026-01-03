package com.app.autologinucab.domain.usecase

import com.app.autologinucab.domain.repository.CredentialsRepository

class HasCredentialsUseCase(
    private val repo: CredentialsRepository
) {
    operator fun invoke(): Boolean = repo.hasAny()
}

