package com.kiwizitos.collection.data.repository

import com.kiwizitos.collection.data.model.ItemStatus
import com.kiwizitos.collection.data.model.UserItem
import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato da galeria do usuário.
 *
 * O cache em memória [galleryCache] é um [StateFlow] indexado por `guiaUrl`,
 * consultado pelas telas de busca, capas e detalhes para exibir badges sem
 * requisições adicionais ao Supabase.
 */
interface GalleryRepository {

    /**
     * Mapa reativo: `guiaUrl → ItemStatus`.
     * Atualizado automaticamente após cada operação de escrita.
     */
    val galleryCache: StateFlow<Map<String, ItemStatus>>

    /** Carrega todos os itens do usuário e popula [galleryCache]. */
    suspend fun loadGallery(userId: String): Result<List<UserItem>>

    /** Insere ou atualiza (upsert) um item. Atualiza [galleryCache]. */
    suspend fun saveItem(item: UserItem): Result<UserItem>

    /** Altera o status (ownership e/ou readStatus) de um item existente. Atualiza [galleryCache]. */
    suspend fun updateStatus(guiaUrl: String, status: ItemStatus): Result<Unit>

    /** Remove um item da galeria. Atualiza [galleryCache]. */
    suspend fun removeItem(guiaUrl: String): Result<Unit>

    /** Limpa o cache (ex: ao fazer logout). */
    fun clearCache()
}
