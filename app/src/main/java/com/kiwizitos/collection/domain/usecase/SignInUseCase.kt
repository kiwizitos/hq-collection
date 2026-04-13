package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank())
            return Result.failure(IllegalArgumentException("E-mail e senha são obrigatórios"))
        return repo.signIn(email.trim(), password)
    }
}

