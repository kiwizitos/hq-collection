package com.kiwizitos.collection.data.repository

import android.util.Log
import com.kiwizitos.collection.data.remote.SupabaseModule
import io.github.jan.supabase.auth.providers.builtin.Email

private const val TAG = "SupabaseAuth"

/**
 * Implementação de [AuthRepository] usando o SDK do Supabase v3.
 * Singleton — use [SupabaseAuthRepository.instance].
 */
class SupabaseAuthRepository private constructor() : AuthRepository {

    companion object {
        val instance: SupabaseAuthRepository by lazy { SupabaseAuthRepository() }
    }

    private val auth get() = SupabaseModule.auth

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWith(Email) {
                this.email    = email
                this.password = password
            }
            Log.d(TAG, "signIn: sucesso para $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "signIn: erro", e)
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            auth.signUpWith(Email) {
                this.email    = email
                this.password = password
            }
            Log.d(TAG, "signUp: sucesso para $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "signUp: erro", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Log.d(TAG, "signOut: sessão encerrada")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "signOut: erro", e)
            Result.failure(e)
        }
    }

    override fun currentUserId(): String?    = auth.currentUserOrNull()?.id
    override fun currentUserEmail(): String? = auth.currentUserOrNull()?.email
    override fun isLoggedIn(): Boolean       = auth.currentSessionOrNull() != null
}

