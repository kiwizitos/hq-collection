package com.kiwizitos.collection.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kiwizitos.collection.data.model.ItemStatus
import com.kiwizitos.collection.data.model.UserItem
import com.kiwizitos.collection.data.repository.GalleryRepository
import com.kiwizitos.collection.data.repository.SupabaseGalleryRepository
import com.kiwizitos.collection.domain.usecase.GetGalleryUseCase
import com.kiwizitos.collection.domain.usecase.RemoveItemUseCase
import com.kiwizitos.collection.domain.usecase.SaveItemUseCase
import com.kiwizitos.collection.domain.usecase.UpdateItemStatusUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val galleryRepository:       GalleryRepository,
    private val getGalleryUseCase:       GetGalleryUseCase,
    private val saveItemUseCase:         SaveItemUseCase,
    private val updateItemStatusUseCase: UpdateItemStatusUseCase,
    private val removeItemUseCase:       RemoveItemUseCase
) : ViewModel() {

    /**
     * Mapa reativo `guiaUrl → ItemStatus` compartilhado via cache do repositório.
     * Coletado diretamente nas telas para exibir badges.
     */
    val galleryMap: StateFlow<Map<String, ItemStatus>> = galleryRepository.galleryCache

    /** Carrega a galeria do usuário e popula o cache. Chamar após o login. */
    fun loadGallery(userId: String) {
        viewModelScope.launch {
            getGalleryUseCase(userId)
        }
    }

    /** Salva (ou atualiza via upsert) um item na galeria. */
    fun saveItem(item: UserItem) {
        viewModelScope.launch {
            saveItemUseCase(item)
        }
    }

    /** Altera o status (ownership e/ou readStatus) de um item já salvo. */
    fun updateStatus(guiaUrl: String, status: ItemStatus) {
        viewModelScope.launch {
            updateItemStatusUseCase(guiaUrl, status)
        }
    }

    /** Remove um item da galeria. */
    fun removeItem(guiaUrl: String) {
        viewModelScope.launch {
            removeItemUseCase(guiaUrl)
        }
    }
}

class GalleryViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = SupabaseGalleryRepository.instance
        return GalleryViewModel(
            galleryRepository       = repo,
            getGalleryUseCase       = GetGalleryUseCase(repo),
            saveItemUseCase         = SaveItemUseCase(repo),
            updateItemStatusUseCase = UpdateItemStatusUseCase(repo),
            removeItemUseCase       = RemoveItemUseCase(repo)
        ) as T
    }
}
