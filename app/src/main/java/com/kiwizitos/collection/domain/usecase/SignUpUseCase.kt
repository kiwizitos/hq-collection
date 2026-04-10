package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.repository.AuthRepository

class SignUpUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank())
            return Result.failure(IllegalArgumentException("E-mail e senha são obrigatórios"))
        if (password.length < 6)
            return Result.failure(IllegalArgumentException("A senha deve ter pelo menos 6 caracteres"))
        return repo.signUp(email.trim(), password)
    }
}

