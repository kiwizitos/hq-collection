package com.kiwizitos.collection.data.repository

import com.kiwizitos.collection.data.model.ItemStatus
import com.kiwizitos.collection.data.model.UserItem
import com.kiwizitos.collection.data.model.UserSeries
import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato da galeria do usuário.
 *
 * Dois caches reativos:
 * - [editionsCache] `guiaUrl → ItemStatus` — edições com status de posse/leitura
 * - [seriesCache]   `seriesUrl → UserSeries` — séries salvas para acesso rápido
 */
interface GalleryRepository {

    /** `guiaUrl → ItemStatus` para edições. */
    val editionsCache: StateFlow<Map<String, ItemStatus>>

    /** `guiaUrl → UserItem` — mapa completo de edições, inclui title, coverUrl, etc. */
    val editionsFull: StateFlow<Map<String, UserItem>>

    /** `seriesUrl → UserSeries` para séries salvas. */
    val seriesCache: StateFlow<Map<String, UserSeries>>

    /** Carrega edições e séries do usuário, populando ambos os caches. */
    suspend fun loadGallery(userId: String): Result<Unit>

    // ── Edições ───────────────────────────────────────────────────────────────
    suspend fun saveItem(item: UserItem): Result<UserItem>
    suspend fun updateStatus(guiaUrl: String, status: ItemStatus): Result<Unit>
    suspend fun removeItem(guiaUrl: String): Result<Unit>

    // ── Séries ────────────────────────────────────────────────────────────────
    suspend fun saveSeries(series: UserSeries): Result<UserSeries>
    suspend fun removeSeries(seriesUrl: String): Result<Unit>

    fun clearCache()
}
