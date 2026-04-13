package com.kiwizitos.collection.data.repository

import android.util.Log
import com.kiwizitos.collection.data.model.ItemStatus
import com.kiwizitos.collection.data.model.UserItem
import com.kiwizitos.collection.data.model.UserSeries
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

private const val TAG = "GalleryRepo"
private const val TABLE_EDITIONS = "user_editions"
private const val TABLE_SERIES = "user_series"

/**
 * Implementação de [GalleryRepository] usando Supabase PostgREST v3.
 *
 * • `user_editions` — volumes com status de posse/leitura
 * • `user_series`   — séries salvas para acesso rápido (sem status)
 */
class SupabaseGalleryRepository @Inject constructor(
    private val client: SupabaseClient
) : GalleryRepository {

    private val _editionsCache = MutableStateFlow<Map<String, ItemStatus>>(emptyMap())
    override val editionsCache: StateFlow<Map<String, ItemStatus>> = _editionsCache.asStateFlow()

    private val _editionsFull = MutableStateFlow<Map<String, UserItem>>(emptyMap())
    override val editionsFull: StateFlow<Map<String, UserItem>> = _editionsFull.asStateFlow()

    private val _seriesCache = MutableStateFlow<Map<String, UserSeries>>(emptyMap())
    override val seriesCache: StateFlow<Map<String, UserSeries>> = _seriesCache.asStateFlow()

    // ── Carregar galeria completa ─────────────────────────────────────────────

    override suspend fun loadGallery(userId: String): Result<Unit> {
        return try {
            coroutineScope {
                val editionsDeferred = async {
                    client
                        .from(TABLE_EDITIONS)
                        .select { filter { eq("user_id", userId) } }
                        .decodeList<UserItem>()
                }
                val seriesDeferred = async {
                    client
                        .from(TABLE_SERIES)
                        .select { filter { eq("user_id", userId) } }
                        .decodeList<UserSeries>()
                }
                val editions = editionsDeferred.await()
                val seriesList = seriesDeferred.await()

                _editionsCache.value = editions.associate { it.guiaUrl to it.toItemStatus() }
                _editionsFull.value = editions.associateBy { it.guiaUrl }
                _seriesCache.value = seriesList.associate { it.seriesUrl to it }
                Log.d(TAG, "loadGallery: ${editions.size} edições, ${seriesList.size} séries")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "loadGallery: erro", e)
            Result.failure(e)
        }
    }

    // ── Edições ───────────────────────────────────────────────────────────────

    override suspend fun saveItem(item: UserItem): Result<UserItem> {
        return try {
            val userId = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(IllegalStateException("Usuário não autenticado"))

            val withUser = item.copy(userId = userId)
            client
                .from(TABLE_EDITIONS)
                .upsert(withUser) { onConflict = "user_id,guia_url" }

            _editionsCache.value =
                _editionsCache.value + (withUser.guiaUrl to withUser.toItemStatus())
            _editionsFull.value = _editionsFull.value + (withUser.guiaUrl to withUser)
            Log.d(TAG, "saveItem: ${withUser.guiaUrl} → ${withUser.toItemStatus()}")
            Result.success(withUser)
        } catch (e: Exception) {
            Log.e(TAG, "saveItem: erro", e)
            Result.failure(e)
        }
    }

    override suspend fun updateStatus(guiaUrl: String, status: ItemStatus): Result<Unit> {
        return try {
            val userId = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(IllegalStateException("Usuário não autenticado"))

            client
                .from(TABLE_EDITIONS)
                .update({
                    set("ownership", status.ownership?.name)
                    set("read_status", status.readStatus?.name)
                }) {
                    filter {
                        eq("guia_url", guiaUrl)
                        eq("user_id", userId)
                    }
                }

            _editionsCache.value = _editionsCache.value + (guiaUrl to status)
            // Atualiza o item completo preservando os campos de metadados
            _editionsFull.value = _editionsFull.value[guiaUrl]?.let { existing ->
                _editionsFull.value + (guiaUrl to existing.copy(
                    ownership = status.ownership,
                    readStatus = status.readStatus
                ))
            } ?: _editionsFull.value
            Log.d(TAG, "updateStatus: $guiaUrl → $status")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateStatus: erro", e)
            Result.failure(e)
        }
    }

    override suspend fun removeItem(guiaUrl: String): Result<Unit> {
        return try {
            val userId = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(IllegalStateException("Usuário não autenticado"))

            client
                .from(TABLE_EDITIONS)
                .delete { filter { eq("guia_url", guiaUrl); eq("user_id", userId) } }

            _editionsCache.value = _editionsCache.value - guiaUrl
            _editionsFull.value = _editionsFull.value - guiaUrl
            Log.d(TAG, "removeItem: $guiaUrl removido")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "removeItem: erro", e)
            Result.failure(e)
        }
    }

    // ── Séries ────────────────────────────────────────────────────────────────

    override suspend fun saveSeries(series: UserSeries): Result<UserSeries> {
        return try {
            val userId = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(IllegalStateException("Usuário não autenticado"))

            val withUser = series.copy(userId = userId)
            client
                .from(TABLE_SERIES)
                .upsert(withUser) { onConflict = "user_id,series_url" }

            _seriesCache.value = _seriesCache.value + (withUser.seriesUrl to withUser)
            Log.d(TAG, "saveSeries: ${withUser.seriesUrl}")
            Result.success(withUser)
        } catch (e: Exception) {
            Log.e(TAG, "saveSeries: erro", e)
            Result.failure(e)
        }
    }

    override suspend fun removeSeries(seriesUrl: String): Result<Unit> {
        return try {
            val userId = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(IllegalStateException("Usuário não autenticado"))

            client
                .from(TABLE_SERIES)
                .delete { filter { eq("series_url", seriesUrl); eq("user_id", userId) } }

            _seriesCache.value = _seriesCache.value - seriesUrl
            Log.d(TAG, "removeSeries: $seriesUrl removida")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "removeSeries: erro", e)
            Result.failure(e)
        }
    }

    override fun clearCache() {
        _editionsCache.value = emptyMap()
        _editionsFull.value = emptyMap()
        _seriesCache.value = emptyMap()
        Log.d(TAG, "clearCache: caches limpos")
    }
}
