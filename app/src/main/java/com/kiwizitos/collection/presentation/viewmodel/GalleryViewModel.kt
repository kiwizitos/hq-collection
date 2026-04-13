package com.kiwizitos.collection.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiwizitos.collection.data.model.ItemStatus
import com.kiwizitos.collection.data.model.UserItem
import com.kiwizitos.collection.data.model.UserSeries
import com.kiwizitos.collection.data.repository.GalleryRepository
import com.kiwizitos.collection.domain.usecase.GetGalleryUseCase
import com.kiwizitos.collection.domain.usecase.RemoveItemUseCase
import com.kiwizitos.collection.domain.usecase.RemoveSeriesUseCase
import com.kiwizitos.collection.domain.usecase.SaveItemUseCase
import com.kiwizitos.collection.domain.usecase.SaveSeriesUseCase
import com.kiwizitos.collection.domain.usecase.UpdateItemStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val getGalleryUseCase: GetGalleryUseCase,
    private val saveItemUseCase: SaveItemUseCase,
    private val updateItemStatusUseCase: UpdateItemStatusUseCase,
    private val removeItemUseCase: RemoveItemUseCase,
    private val saveSeriesUseCase: SaveSeriesUseCase,
    private val removeSeriesUseCase: RemoveSeriesUseCase,
    galleryRepository: GalleryRepository
) : ViewModel() {

    /** `guiaUrl → ItemStatus` para edições. Usado para badges em busca/capas. */
    val galleryMap: StateFlow<Map<String, ItemStatus>> = galleryRepository.editionsCache

    /** `guiaUrl → UserItem` — edições completas com title, coverUrl, etc. Usado na Home. */
    val editionsFull: StateFlow<Map<String, UserItem>> = galleryRepository.editionsFull

    /** `seriesUrl → UserSeries` para séries salvas. Usado na home/biblioteca. */
    val seriesMap: StateFlow<Map<String, UserSeries>> = galleryRepository.seriesCache

    fun loadGallery(userId: String) {
        viewModelScope.launch { getGalleryUseCase(userId) }
    }

    // ── Edições (com status) ──────────────────────────────────────────────────

    fun saveItem(item: UserItem) {
        viewModelScope.launch { saveItemUseCase(item) }
    }

    fun updateStatus(guiaUrl: String, status: ItemStatus) {
        viewModelScope.launch { updateItemStatusUseCase(guiaUrl, status) }
    }

    fun removeItem(guiaUrl: String) {
        viewModelScope.launch { removeItemUseCase(guiaUrl) }
    }

    // ── Séries (sem status — só referência) ───────────────────────────────────

    fun saveSeries(series: UserSeries) {
        viewModelScope.launch { saveSeriesUseCase(series) }
    }

    fun removeSeries(seriesUrl: String) {
        viewModelScope.launch { removeSeriesUseCase(seriesUrl) }
    }
}

