package com.kiwizitos.collection.data.repository

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import javax.inject.Inject

private const val TAG = "SupabaseAuth"

class SupabaseAuthRepository @Inject constructor(
    private val client: SupabaseClient
) : AuthRepository {

    private val auth get() = client.auth

    override suspend fun restoreSession(): Result<Unit> {
        return try {
            auth.loadFromStorage()
            Log.d(TAG, "restoreSession: sessão restaurada — userId=${auth.currentUserOrNull()?.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "restoreSession: erro ao restaurar sessão", e)
            // Não é um erro fatal — o usuário simplesmente precisará logar novamente
            Result.success(Unit)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWith(Email) {
                this.email = email
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
                this.email = email
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

    override fun currentUserId(): String? = auth.currentUserOrNull()?.id
    override fun currentUserEmail(): String? = auth.currentUserOrNull()?.email
    override fun isLoggedIn(): Boolean = auth.currentUserOrNull() != null
}

