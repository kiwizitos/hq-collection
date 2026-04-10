package com.kiwizitos.collection.data.remote

import com.kiwizitos.collection.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

/**
 * Singleton do cliente Supabase.
 *
 * Segue o mesmo padrão de [NetworkModule] — instância lazy criada uma única vez.
 * As credenciais são lidas de `BuildConfig`, que as recebe de `local.properties`
 * em tempo de build, sem expô-las no controle de versão.
 *
 * Uso:
 * ```kotlin
 * val user = SupabaseModule.auth.currentUserOrNull()
 * val items = SupabaseModule.postgrest["user_items"].select().decodeList<UserItem>()
 * ```
 */
object SupabaseModule {

    val client by lazy {
        createSupabaseClient(
            supabaseUrl  = BuildConfig.SUPABASE_URL,
            supabaseKey  = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    /** Atalho para o plugin de autenticação. */
    val auth get() = client.auth

    /** Atalho para o plugin PostgREST. */
    val postgrest get() = client.postgrest
}

