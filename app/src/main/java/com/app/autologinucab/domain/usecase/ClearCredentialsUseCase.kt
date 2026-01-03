package com.app.autologinucab.domain.usecase

import com.app.autologinucab.domain.repository.CredentialsRepository

class ClearCredentialsUseCase(
    private val repo: CredentialsRepository
) {
    operator fun invoke() = repo.clear()
}

