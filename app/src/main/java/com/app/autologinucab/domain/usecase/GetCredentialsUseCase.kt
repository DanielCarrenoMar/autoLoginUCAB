package com.app.autologinucab.domain.usecase

import com.app.autologinucab.domain.model.Credentials
import com.app.autologinucab.domain.repository.CredentialsRepository

class GetCredentialsUseCase(
    private val repo: CredentialsRepository
) {
    operator fun invoke(): Credentials? = repo.get()
}

