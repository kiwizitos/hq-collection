package com.kiwizitos.collection.data.repository

import android.util.Log
import com.kiwizitos.collection.data.model.ItemStatus
import com.kiwizitos.collection.data.model.UserItem
import com.kiwizitos.collection.data.remote.SupabaseModule
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG   = "GalleryRepo"
private const val TABLE = "user_items"

/**
 * Implementação de [GalleryRepository] usando Supabase PostgREST v3.
 * Singleton — use [SupabaseGalleryRepository.instance].
 *
 * Mantém um cache reativo [_galleryCache] indexado por `guiaUrl → ItemStatus`
 * para consultas instantâneas nas telas de busca e capas sem requisições extras.
 *
 * Nota: o upsert no SDK v3 não suporta `returning` — o cache é atualizado
 * otimisticamente com os dados já disponíveis, sem round-trip extra.
 */
class SupabaseGalleryRepository private constructor() : GalleryRepository {

    companion object {
        val instance: SupabaseGalleryRepository by lazy { SupabaseGalleryRepository() }
    }

    private val _galleryCache = MutableStateFlow<Map<String, ItemStatus>>(emptyMap())
    override val galleryCache: StateFlow<Map<String, ItemStatus>> = _galleryCache.asStateFlow()

    override suspend fun loadGallery(userId: String): Result<List<UserItem>> {
        return try {
            val items = SupabaseModule.client
                .from(TABLE)
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<UserItem>()

            _galleryCache.value = items.associate { it.guiaUrl to it.toItemStatus() }
            Log.d(TAG, "loadGallery: ${items.size} itens carregados")
            Result.success(items)
        } catch (e: Exception) {
            Log.e(TAG, "loadGallery: erro", e)
            Result.failure(e)
        }
    }

    override suspend fun saveItem(item: UserItem): Result<UserItem> {
        return try {
            // Garante que user_id está preenchido com o usuário da sessão atual.
            // Sem isso o RLS INSERT policy rejeita a linha (auth.uid() != user_id).
            val userId = SupabaseModule.auth.currentUserOrNull()?.id
                ?: return Result.failure(IllegalStateException("Usuário não autenticado"))

            val itemWithUser = item.copy(userId = userId)

            SupabaseModule.client
                .from(TABLE)
                .upsert(itemWithUser) { onConflict = "user_id,guia_url" }

            _galleryCache.value = _galleryCache.value + (itemWithUser.guiaUrl to itemWithUser.toItemStatus())
            Log.d(TAG, "saveItem: ${itemWithUser.guiaUrl} → ${itemWithUser.toItemStatus()}")
            Result.success(itemWithUser)
        } catch (e: Exception) {
            Log.e(TAG, "saveItem: erro", e)
            Result.failure(e)
        }
    }

    override suspend fun updateStatus(guiaUrl: String, status: ItemStatus): Result<Unit> {
        return try {
            val userId = SupabaseModule.auth.currentUserOrNull()?.id
                ?: return Result.failure(IllegalStateException("Usuário não autenticado"))

            SupabaseModule.client
                .from(TABLE)
                .update({
                    set("ownership",   status.ownership?.name)
                    set("read_status", status.readStatus?.name)
                }) {
                    filter {
                        eq("guia_url", guiaUrl)
                        eq("user_id",  userId)
                    }
                }

            _galleryCache.value = _galleryCache.value + (guiaUrl to status)
            Log.d(TAG, "updateStatus: $guiaUrl → $status")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateStatus: erro", e)
            Result.failure(e)
        }
    }

    override suspend fun removeItem(guiaUrl: String): Result<Unit> {
        return try {
            val userId = SupabaseModule.auth.currentUserOrNull()?.id
                ?: return Result.failure(IllegalStateException("Usuário não autenticado"))

            SupabaseModule.client
                .from(TABLE)
                .delete {
                    filter {
                        eq("guia_url", guiaUrl)
                        eq("user_id",  userId)
                    }
                }

            _galleryCache.value = _galleryCache.value - guiaUrl
            Log.d(TAG, "removeItem: $guiaUrl removido")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "removeItem: erro", e)
            Result.failure(e)
        }
    }

    override fun clearCache() {
        _galleryCache.value = emptyMap()
        Log.d(TAG, "clearCache: cache limpo")
    }
}
