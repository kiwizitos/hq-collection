package com.kiwizitos.collection.data.repository

/**
 * Contrato de autenticação.
 * A implementação concreta [SupabaseAuthRepository] usa o SDK do Supabase.
 */
interface AuthRepository {
    /**
     * Restaura a sessão persistida do armazenamento local.
     * Deve ser chamado no startup antes de qualquer verificação de estado de login.
     */
    suspend fun restoreSession(): Result<Unit>

    /** Autentica com e-mail e senha. */
    suspend fun signIn(email: String, password: String): Result<Unit>

    /** Cria uma nova conta com e-mail e senha. */
    suspend fun signUp(email: String, password: String): Result<Unit>

    /** Encerra a sessão atual. */
    suspend fun signOut(): Result<Unit>

    /** Retorna o ID do usuário logado, ou `null` se não houver sessão. */
    fun currentUserId(): String?

    /** Retorna o e-mail do usuário logado, ou `null`. */
    fun currentUserEmail(): String?

    /** `true` se há uma sessão ativa. */
    fun isLoggedIn(): Boolean
}

